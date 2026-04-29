# Docs — Knowledge Base Index

이 디렉터리는 리포지터리의 기록 시스템(System of Record)이다.  
코드 외의 모든 의사결정, 설계 의도, 품질 상태는 여기에 기록된다.

> 여기에 없는 것은 에이전트에게 존재하지 않는다.

## 구조

| 경로 | 내용 |
|------|------|
| `design-docs/` | 아키텍처 결정, 설계 배경, 핵심 신념 |
| `exec-plans/active/` | 진행 중인 실행 계획 |
| `exec-plans/completed/` | 완료된 실행 계획 (의사결정 기록) |
| `references/` | 외부 라이브러리·프레임워크 참조 문서 |
| `QUALITY.md` | 서비스별 품질 등급 및 기술 부채 현황 |

## 작성 원칙

- 설계 결정을 내렸다면 → `design-docs/`에 배경과 이유를 기록
- 복잡한 작업을 시작한다면 → `exec-plans/active/`에 실행 계획 생성
- 완료된 계획은 → `exec-plans/completed/`로 이동 (삭제 금지)
- Slack·구두 합의로 결정된 것 → 이 디렉터리에 옮겨 기록
