package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.live_commerce.payment.application.dto.request.PaymentReadyRequestDto;
import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.repository.PaymentRepository;
import com.live_commerce.payment.infrastructure.client.dto.KakaoPayReadyDto;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentCancelEventProducer;
import com.live_commerce.payment.infrastructure.kafka.producer.PaymentSuccessEventProducer;
import com.live_commerce.payment.infrastructure.security.RequestUserDetails;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceConcurrencyTest {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private PaymentRepository paymentRepository;


	@Autowired
	private RedissonClient redissonClient;

	@MockitoBean private KakaoPayClient kakaoPayClient;
	@MockitoBean private PaymentSuccessEventProducer paymentSuccessEventProducer;
	@MockitoBean private PaymentCancelEventProducer paymentCancelEventProducer;

	@BeforeEach
	void setUp() {
		// 카카오페이 요청을 mocking (결제 준비 요청 시 고정된 응답을 반환)
		KakaoPayReadyDto mockDto = new KakaoPayReadyDto(
			"T123456789",
			"https://kakao.pay.pc",
			"https://kakao.pay.mobile",
			"2025-04-17T21:20:00"
		);

		when(kakaoPayClient.requestKakaoPayReady(any(), any(), any(), any()))
			.thenReturn(mockDto);
	}

	/**
	 * Redisson 분산락을 활용한 결제 준비 로직에 대해 동시성 테스트를 수행합니다.
	 * 동일한 orderId에 대해 여러 스레드가 접근할 경우, 락으로 인해 단 1개의 스레드만 작업을 성공해야 합니다.
	 */
	@Test
	void readyPayment_should_be_synchronized_by_redisson_lock() throws InterruptedException {
		UUID userId  = UUID.randomUUID();
		UUID orderId = UUID.randomUUID(); // 모든 스레드가 동일한 orderId로 요청

		RequestUserDetails user = new RequestUserDetails(userId, null, Collections.emptyList());
		PaymentReadyRequestDto dto = new PaymentReadyRequestDto(orderId, BigDecimal.valueOf(5000), "테스트상품");

		int threadCount = 10;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);

		AtomicInteger successCount = new AtomicInteger(0);

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					paymentService.readyPayment(user, dto);
					successCount.incrementAndGet(); // 성공한 스레드 수 증가
				} catch (Exception e) {
					System.out.println("실패 스레드: " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();

		// DB에 저장된 결제 데이터가 1건인지 확인 (Optional 사용)
		Optional<Payment> saved = paymentRepository.findByOrderId(orderId);
		assertTrue(saved.isPresent(), "DB에 결제 데이터가 저장되지 않았습니다");
		// 성공한 스레드는 오직 1개여야 함 → 분산락이 잘 동작했다는 의미
		assertEquals(1, successCount.get());
	}

	/**
	 * 분산락의 leaseTime(만료시간) 이후, 다른 스레드가 락을 재획득할 수 있음을 확인하는 테스트.
	 * → 락이 시간이 지나면 풀리고, 다른 쓰레드가 락을 잡고 작업할 수 있음.
	 */
	@Test
	void testRedissonLockBreakByLeaseTime_withDB() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);
		UUID orderId1 = UUID.randomUUID();
		UUID orderId2 = UUID.randomUUID();

		Runnable task1 = () -> {
			try {
				if (lock.tryLock(100, 1000, TimeUnit.MILLISECONDS)) {
					Payment p1 = Payment.of(UUID.randomUUID(), orderId1, BigDecimal.valueOf(1000));
					paymentRepository.save(p1);
					Thread.sleep(2000); // leaseTime 초과
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					lock.unlock();
				} catch (Exception ignored) {}
			}
		};

		Runnable task2 = () -> {
			try {
				Thread.sleep(1500); // leaseTime 끝난 직후 시도
				if (lock.tryLock(100, 1000, TimeUnit.MILLISECONDS)) {
					Payment p2 = Payment.of(UUID.randomUUID(), orderId2, BigDecimal.valueOf(2000));
					paymentRepository.save(p2);
					lock.unlock();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(task1);
		executor.submit(task2);
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);

		assertTrue(paymentRepository.findByOrderId(orderId1).isPresent());
		assertTrue(paymentRepository.findByOrderId(orderId2).isPresent());
	}


	/**
	 * 락을 먼저 잡은 T1 쓰레드가 leaseTime보다 오래 작업하면
	 * → T2가 락을 다시 잡고 작업을 완료한 뒤, T1이 unlock()을 호출하면 예외 발생.
	 * → Redisson은 thread-id와 lock owner 정보를 비교하여 unlock 검증함.
	 */
	@Test
	void testRedissonLockLeak() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);

		// 락을 먼저 획득하는 T1
		Runnable thread1 = () -> {
			try {
				if (lock.tryLock(100, 1000, TimeUnit.MILLISECONDS)) {
					System.out.println("[T1] 락 획득");
					Thread.sleep(2000); // leaseTime 초과 → 락은 중간에 해제됨
					System.out.println("[T1] 작업 끝");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					lock.unlock(); // 이미 락은 T2에 의해 점유 중이거나 없을 수 있음
					System.out.println("[T1] 락 해제");
				} catch (Exception e) {
					System.out.println("[T1] 락 해제 실패: " + e.getMessage());
				}
			}
		};

		// leaseTime이 만료될 즈음 락을 시도하는 T2
		Runnable thread2 = () -> {
			try {
				Thread.sleep(1500); // 락이 자동으로 풀릴 시점에 접근
				if (lock.tryLock(100, 1000, TimeUnit.MILLISECONDS)) {
					System.out.println("[T2] 락 획득");
					System.out.println("[T2] 작업 중...");
					Thread.sleep(500);
					System.out.println("[T2] 작업 끝");
					lock.unlock();
				} else {
					System.out.println("[T2] 락 획득 실패");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(thread1);
		executor.submit(thread2);
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);
	}
	/**
	 * lock() 메서드는 leaseTime을 명시하지 않으면 Redisson의 watchdog(기본 30초)이 동작하여
	 * 락을 자동으로 연장합니다.
	 */
	@Test
	void testRedissonLockWithWatchdog() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);

		Runnable longTask = () -> {
			try {
				lock.lock(); // leaseTime 없이 락 획득 → watchdog이 내부적으로 연장함
				System.out.println("[T1] 락 획득 - watchdog");

				Thread.sleep(15000); // watchdog이 없다면 30초 전 TTL이 만료되어야 함
				System.out.println("[T1] 작업 완료");

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock(); // 정상 해제 가능해야 함
				System.out.println("[T1] 락 해제");
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(longTask);
		executor.shutdown();
		executor.awaitTermination(20, TimeUnit.SECONDS);
	}

	/**
	 * 락 획득에 실패했을 경우, 일정 횟수만큼 재시도하는 로직을 시뮬레이션합니다.
	 */
	@Test
	void testTryLockWithRetry() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);

		UUID retryOrderId = UUID.randomUUID();
		AtomicInteger attemptCount = new AtomicInteger(0);

		Runnable longTask = () -> {
			try {
				lock.lock();
				System.out.println("[LONG] 락 보유 중...");
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
				System.out.println("[LONG] 락 해제");
			}
		};

		Runnable retryingTask = () -> {
			int retry = 10;
			while (retry-- > 0) {
				attemptCount.incrementAndGet();
				try {
					if (lock.tryLock(100, 1000, TimeUnit.MILLISECONDS)) {
						System.out.println("[RETRY] 락 획득 성공");
						Payment p = Payment.of(UUID.randomUUID(), retryOrderId, BigDecimal.valueOf(7777));
						paymentRepository.save(p);
						lock.unlock();
						break;
					} else {
						System.out.println("[RETRY] 락 획득 실패, 재시도 남음: " + retry);
						Thread.sleep(400); // 락 해제 후 진입을 위해 넉넉히 대기
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(longTask);
		Thread.sleep(100); // 약간의 시간차
		executor.submit(retryingTask);
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);

		Optional<Payment> result = paymentRepository.findByOrderId(retryOrderId);
		System.out.println("[TEST] 시도 횟수: " + attemptCount.get());
		System.out.println("[TEST] 저장 여부: " + result.isPresent());

		assertTrue(result.isPresent(), "락 획득 실패로 저장되지 않았습니다");
	}



	/**
	 * 락을 획득한 상태에서 Redis에서 락 키를 외부적으로 삭제하면,
	 * 이후 unlock() 호출 시 예외가 발생하는지 확인하는 테스트입니다.
	 */
	@Test
	void testUnlockFailurePreventsReentry() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);

		lock.lock(); // 락 획득
		System.out.println("[TEST] 락 획득");

		// Redis에서 key를 강제로 삭제 → Redisson이 더 이상 락 소유자 정보를 모름
		redissonClient.getBucket(lockKey).delete();
		System.out.println("[TEST] Redis key 강제 삭제");

		// 이후 unlock() 호출 시 예외 발생 기대
		try {
			lock.unlock();
			fail("예외가 발생해야 합니다");
		} catch (Exception e) {
			System.out.println("[TEST] 예상된 락 해제 실패 발생: " + e.getMessage());
		}
	}

	@Test
	void duplicatePaymentRequest_should_create_only_one_payment() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		PaymentReadyRequestDto dto = new PaymentReadyRequestDto(orderId, BigDecimal.valueOf(9999), "중복 테스트");

		RequestUserDetails user = new RequestUserDetails(userId, null, Collections.emptyList());

		int threadCount = 5;
		CountDownLatch latch = new CountDownLatch(threadCount);
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					paymentService.readyPayment(user, dto);
				} catch (Exception ignored) {}
				finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		long count = paymentRepository.findAll().stream()
			.filter(p -> p.getOrderId().equals(orderId))
			.count();

		assertEquals(1, count, "중복 결제가 생성되면 안됩니다.");
	}



}
