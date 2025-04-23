package com.live_commerce.payment.application.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.live_commerce.payment.application.port.KakaoPayClient;
import com.live_commerce.payment.domain.model.Payment;
import com.live_commerce.payment.domain.repository.PaymentRepository;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentDistributedLockTest {

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private PaymentRepository paymentRepository;

	@MockitoBean
	private KakaoPayClient kakaoPayClient;


	/**
	 * 분산락의 leaseTime(만료시간) 이후, 다른 스레드가 락을 재획득할 수 있음을 확인하는 테스트.
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
					Thread.sleep(2000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					lock.unlock();
				} catch (Exception e) {
					System.err.println("unlock 실패: " + e.getMessage());
				}
			}
		};

		Runnable task2 = () -> {
			try {
				Thread.sleep(1500);
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
	 * leaseTime 초과로 락이 풀린 후 T2가 락을 잡고 작업한 경우, T1의 unlock()은 예외를 발생시킴.
	 */
	@Test
	void testRedissonLockLeak() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);

		Runnable thread1 = () -> {
			try {
				if (lock.tryLock(100, 1000, TimeUnit.MILLISECONDS)) {
					System.out.println("[T1] 락 획득");
					Thread.sleep(2000);
					System.out.println("[T1] 작업 끝");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					lock.unlock();
					System.out.println("[T1] 락 해제");
				} catch (Exception e) {
					System.out.println("[T1] 락 해제 실패: " + e.getMessage());
				}
			}
		};

		Runnable thread2 = () -> {
			try {
				Thread.sleep(1500);
				if (lock.tryLock(100, 1000, TimeUnit.MILLISECONDS)) {
					System.out.println("[T2] 락 획득");
					Thread.sleep(500);
					System.out.println("[T2] 작업 끝");
					lock.unlock();
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
	 * leaseTime을 생략하면 Redisson Watchdog이 자동 연장해줌 → 장시간 작업에도 안정적인 락 유지 확인.
	 */
	@Test
	void testRedissonLockWithWatchdog() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);

		Runnable longTask = () -> {
			try {
				lock.lock();
				System.out.println("[T1] 락 획득 - watchdog");
				Thread.sleep(15000);
				System.out.println("[T1] 작업 완료");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
				System.out.println("[T1] 락 해제");
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(longTask);
		executor.shutdown();
		executor.awaitTermination(20, TimeUnit.SECONDS);
	}

	/**
	 * 락 획득 실패 시 재시도 로직을 구현한 테스트. 여러 번 시도하여 eventually 성공을 유도함.
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
						Thread.sleep(400);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(longTask);
		Thread.sleep(100);
		executor.submit(retryingTask);
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);

		Optional<Payment> result = paymentRepository.findByOrderId(retryOrderId);
		System.out.println("[TEST] 시도 횟수: " + attemptCount.get());
		System.out.println("[TEST] 저장 여부: " + result.isPresent());

		assertTrue(result.isPresent(), "락 획득 실패로 저장되지 않았습니다");
	}

	/**
	 * 락이 걸린 상태에서 Redis의 key를 강제로 삭제했을 때 unlock 시 예외 발생을 확인.
	 */
	@Test
	void testUnlockFailurePreventsReentry() throws InterruptedException {
		String lockKey = "lock:test:" + UUID.randomUUID();
		RLock lock = redissonClient.getLock(lockKey);

		lock.lock();
		System.out.println("[TEST] 락 획득");

		redissonClient.getBucket(lockKey).delete();
		System.out.println("[TEST] Redis key 강제 삭제");

		try {
			lock.unlock();
			fail("예외가 발생해야 합니다");
		} catch (Exception e) {
			System.out.println("[TEST] 예상된 락 해제 실패 발생: " + e.getMessage());
		}
	}
}
