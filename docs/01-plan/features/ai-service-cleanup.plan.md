# Plan: AI 서비스 코드 품질 개선

**작성일**: 2026-05-05
**대상 서비스**: `ai/`
**티켓**: PDSH-5

---

## 배경

코드 분석(code-analyzer) 결과 AI 서비스에 누적된 기술 부채를 확인.
cleanup.md 정책에 따라 나쁜 패턴이 복제되기 전에 선제적으로 제거.

---

## 목표

- 성공 경로 INFO 로그 제거 (feedback_logging.md 룰 준수)
- 디버그용 `System.out.println` 제거
- 미사용 코드(dead code) 제거
- Slack 비동기 실패 무시 해결

---

## 범위

### PDSH-5-1: dead code 및 성공 경로 로그 제거 (완료)

| 파일 | 변경 내용 |
|------|----------|
| `infrastructure/slack/SlackSender.java` | `System.out.println` 제거, `WebClient` 필드로 올려 재사용, 실패 시 `log.warn` 추가 |
| `infrastructure/common/ResponseUtil.java` | INFO 로그 전부 제거, 미사용 메서드 3개 삭제 (`notFound`, `badRequest`, `internalServerError`) |
| `presentation/controller/AiController.java` | 미사용 `@Value` import 제거 |
| `domain/repository/AiQueryRepository.java` | 미사용 `UUID` import 제거 |
| `domain/repository/AiQueryRepositoryImpl.java` | 미사용 `UUID` import 제거 |

### 별도 티켓 예정

| 이슈 | 내용 |
|------|------|
| PDSH-? | 페이지네이션 전체 조회 버그 수정 (`searchAi` LIMIT 없음, total 오계산) |
| PDSH-? | `catch (Exception e)` 광범위 예외 처리 좁히기 |
| PDSH-? | 레이어 의존 방향 위반 정리 (Port 인터페이스 도입) |
| PDSH-? | 도메인-JPA 엔티티 분리 |

---

## 완료 조건

- `System.out.println` 0건
- 성공 경로 INFO 로그 0건
- 미사용 import/메서드 0건
- 기존 테스트(`AiServiceTest`) 통과
