# Company 모듈 리팩토링 가이드

> **대상**: 개발이 오랜만인 분들을 위한 변경 설명서
> **브랜치**: `feature/PDSH-2-company-refactoring`
> **작성일**: 2026-04-30
> **상태**: 코드 리뷰 반영 완료

---

## 목차

1. [리팩토링이란?](#리팩토링이란)
2. [변경된 파일 개요](#변경된-파일-개요)
3. [변경 1 — 중복 코드 제거 (메서드 추출)](#변경-1--중복-코드-제거-메서드-추출)
4. [변경 2 — 하드코딩 문자열 상수화](#변경-2--하드코딩-문자열-상수화)
5. [변경 3 — 예외(에러) 코드 추가](#변경-3--예외에러-코드-추가)
6. [변경 4 — 로그 형식 개선 및 userId 추가](#변경-4--로그-형식-개선-및-userid-추가)
7. [변경 5 — 단건 조회 예외 처리 개선](#변경-5--단건-조회-예외-처리-개선)
8. [변경 6 — 키워드 검색 버그 수정](#변경-6--키워드-검색-버그-수정)
9. [변경 7 — 공통 역할(Role) 조회 메서드 추가](#변경-7--공통-역할role-조회-메서드-추가)
10. [변경 8 — JPA Auditing 어노테이션 추가](#변경-8--jpa-auditing-어노테이션-추가)
11. [변경 9 — 도메인 모델 책임 분리](#변경-9--도메인-모델-책임-분리)
12. [변경 10 — QueryDSL 조건 일관성 수정 및 NPE 방지](#변경-10--querydsl-조건-일관성-수정-및-npe-방지)
13. [변경 11 — GlobalExceptionHandler 추가 (신규)](#변경-11--globalexceptionhandler-추가-신규)
14. [변경 12 — ROLE_SELLER 소속 업체 소유권 검증 추가](#변경-12--role_seller-소속-업체-소유권-검증-추가)
15. [변경 13 — 단건 조회 소프트 삭제 필터링 추가](#변경-13--단건-조회-소프트-삭제-필터링-추가)
16. [Postman 테스트 결과](#postman-테스트-결과)
17. [설계 결정 사항](#설계-결정-사항)

---

## 리팩토링이란?

리팩토링은 **기능은 그대로 두고, 코드를 더 읽기 쉽고 유지보수하기 좋게 다듬는 작업**입니다.

비유하자면, 요리 레시피가 같아도 "마늘을 넣고, 소금을 넣고, 후추를 넣는다"를 "양념 재료를 모두 넣는다"로 묶어서 표현하는 것과 같습니다.

이번 리팩토링의 핵심 목표:
- **중복 코드 제거**: 같은 내용을 여러 곳에 반복 작성하지 않기
- **버그 수정**: 논리적 오류 교정
- **가독성 향상**: 다른 사람이 읽기 쉬운 코드 만들기
- **예외 처리 완성**: 에러 상황을 클라이언트에게 명확하게 전달

---

## 변경된 파일 개요

```
company/
├── application/
│   ├── exception/
│   │   └── CompanyExceptionCode.java    ← 새 예외 코드 추가
│   └── service/
│       └── CompanyService.java          ← 메서드 추출, 버그 수정
├── domain/
│   ├── model/
│   │   ├── BaseEntity.java              ← Auditing 어노테이션 추가
│   │   └── Company.java                ← update 버그 수정
│   └── repository/
│       └── CompanyQueryRepository.java  ← QueryDSL 조건 일관성 수정
├── infrastructure/
│   ├── exception/
│   │   └── GlobalExceptionHandler.java  ← 신규 추가 (예외 → HTTP 응답 변환)
│   └── security/
│       └── RequestUserDetails.java      ← getRole() 헬퍼 메서드 추가
└── presentation/
    └── controller/
        └── CompanyController.java       ← 중복 코드 제거, 코드 정리
```

---

## 변경 1 — 중복 코드 제거 (메서드 추출)

**파일**: `CompanyService.java`

### 문제 — 같은 코드가 3곳에 반복됨

업체 생성/수정/삭제 모두 같은 권한 검사 코드를 갖고 있었습니다.

```java
// createCompany 안에도...
if (!role.equals("ROLE_MASTER") && !role.equals("ROLE_SELLER")) {
    throw new AccessDeniedException("업체 생성 권한이 없습니다.");
}

// updateCompany 안에도 똑같이...
if (!role.equals("ROLE_MASTER") && !role.equals("ROLE_SELLER")) {
    throw new AccessDeniedException("업체 생성 권한이 없습니다.");
}

// deleteCompany 안에도 똑같이...
if (!role.equals("ROLE_MASTER") && !role.equals("ROLE_SELLER")) {
    throw new AccessDeniedException("업체 생성 권한이 없습니다.");
}
```

**문제점**: 나중에 권한 로직을 바꿔야 할 때 3곳을 모두 찾아가야 합니다. 한 곳을 빠뜨리면 버그가 생깁니다.

### 해결 — 메서드 하나로 묶기

```java
// 검사 로직을 별도 메서드로 추출
private void validateAuthorization(String role) {
    if (!ROLE_MASTER.equals(role) && !ROLE_SELLER.equals(role)) {
        throw new AccessDeniedException("업체 관리 권한이 없습니다.");
    }
}

// 이제 각 메서드에서 한 줄로 호출
public CompanyCreateResponse createCompany(...) {
    validateAuthorization(role);  // 한 줄!
    ...
}
```

마찬가지로 "이미 삭제된 업체인지 확인하는 로직"도 메서드로 묶었습니다:

```java
// 수정/삭제 시 이미 삭제된 업체인지 확인
private void validateNotDeleted(Company company) {
    if (Boolean.TRUE.equals(company.getDeletedStatus())) {
        throw new CompanyException(CompanyExceptionCode.ALREADY_DELETED);
    }
}
```

---

## 변경 2 — 하드코딩 문자열 상수화

**파일**: `CompanyService.java`

### 문제 — 같은 문자열이 코드 곳곳에 박혀 있음

```java
// 오타 낼 위험이 있는 리터럴 문자열
if (!role.equals("ROLE_MASTER") && !role.equals("ROLE_SELLER")) {
```

### 해결 — 클래스 최상단에 상수로 선언

```java
private static final String ROLE_MASTER = "ROLE_MASTER";
private static final String ROLE_SELLER = "ROLE_SELLER";
```

**왜 좋은가?**
- 오타로 인한 버그 방지 (IDE가 자동완성해줌)
- 나중에 역할 이름이 바뀌면 상수 한 곳만 수정

> `static final` = "이 값은 변하지 않는 고정값"이라는 선언입니다.

---

## 변경 3 — 예외(에러) 코드 추가

**파일**: `CompanyExceptionCode.java`

### 변경 내용

```java
// 이전
public enum CompanyExceptionCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "Company Not Found");
}

// 이후
public enum CompanyExceptionCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "Company Not Found"),
    ALREADY_DELETED(HttpStatus.GONE, "이미 삭제된 업체입니다.");  // 추가됨
}
```

### 왜 `HttpStatus.GONE`인가?

HTTP 상태 코드는 서버가 클라이언트에게 "무슨 일이 일어났는지" 알려주는 숫자입니다:

| 코드 | 이름 | 의미 |
|------|------|------|
| 404 | NOT_FOUND | "그런 것 없어요" (아예 존재하지 않음) |
| 410 | GONE | "원래 있었는데 삭제됐어요" (존재했지만 사라짐) |

이미 삭제된 업체에는 404보다 410이 더 정확한 표현입니다.

---

## 변경 4 — 로그 형식 개선 및 userId 추가

**파일**: `CompanyService.java`

### 변경 내용

```java
// 이전 — 문자열 더하기 방식, role만 출력
log.info("User role: " + role);

// 이후 — 파라미터 방식, userId와 role 함께 출력
log.info("User: {}, Role: {}", userId, role);
```

`createCompany`와 `updateCompany` 두 메서드 모두 동일하게 적용됩니다.

### 왜 바꿨는가?

**파라미터 방식 (`{}`)**: 문자열 더하기(`+`)는 로그 레벨과 상관없이 항상 문자열을 만들지만, `{}` 방식은 실제로 로그가 출력될 때만 문자열을 만듭니다. SLF4J 권장 방식입니다.

**userId 추가**: 업체 생성/수정 요청이 누구에 의해 발생했는지 추적 가능하게 합니다. 나중에 감사 로그(Audit Log)나 문제 발생 시 원인 파악에 활용됩니다.

> `userId`는 서명 파라미터에 유지됩니다 — Gateway가 전달하는 `X-User-Id` 헤더 값으로, 향후 업체 소유자 기록 등에 활용될 수 있습니다.

---

## 변경 5 — 단건 조회 예외 처리 개선

**파일**: `CompanyService.java`

### 문제 — 예외 없는 `orElseThrow()`

```java
// 이전 — 업체가 없을 때 NoSuchElementException 발생 (의미 없는 에러 → 클라이언트에 500 반환)
Company company = companyRepository.findById(id).orElseThrow();

// 이후 — 의미 있는 커스텀 예외 발생 → 클라이언트에 404 반환
Company company = companyRepository.findById(id)
        .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
```

`orElseThrow()` 안에 아무것도 넣지 않으면 자바 기본 예외가 던져져 클라이언트가 "뭔가 잘못됐다(500)"는 것만 알고 "업체를 찾지 못했다(404)"는 것을 알 수 없습니다.

---

## 변경 6 — 키워드 검색 버그 수정

**파일**: `CompanyService.java`

### 문제 — 논리 연산자 오류 (`||` vs `&&`)

```java
// 이전 — 버그! keyword가 null이면 NPE(NullPointerException) 발생
if (keyword != null || !keyword.isBlank()) {
    //           ↑↑ 이게 true면 뒤 조건 안 봐서, null일 때 .isBlank() 호출 → 터짐
}

// 이후 — 올바른 null 체크
if (keyword != null && !keyword.isBlank()) {
    //           ↑↑ AND 조건이라 null이면 뒤 조건을 아예 안 봄 → 안전
}
```

### 쉬운 이해

- `A || B` : A가 거짓이어야 B를 봄 → keyword가 null이면 A가 false → B로 넘어가서 null.isBlank() 호출 → 폭발
- `A && B` : A가 참이어야 B를 봄 → keyword가 null이면 A가 false → B를 아예 안 봄 → 안전

---

## 변경 7 — 공통 역할(Role) 조회 메서드 추가

**파일**: `RequestUserDetails.java`

### 문제 — 컨트롤러마다 중복된 역할 추출 코드

```java
// 이전 — 컨트롤러의 createCompany, updateCompany, deleteCompany 모두에 반복
String role = userDetails.getAuthorities().iterator().next().getAuthority();
```

이 코드는 길고 이해하기 어렵습니다.
`getAuthorities()` → `iterator()` → `next()` → `getAuthority()` — 4단계 체인.

### 해결 — `RequestUserDetails`에 편의 메서드 추가

```java
// RequestUserDetails.java에 추가
public String getRole() {
    return authorities.iterator().next().getAuthority();
}
```

```java
// 이제 컨트롤러에서는 한 줄로
return ResponseUtil.success(companyService.createCompany(request, userDetails.getUserId(), userDetails.getRole()));
```

---

## 변경 8 — JPA Auditing 어노테이션 추가

**파일**: `BaseEntity.java`

### 변경 내용

```java
// 이전 — 어노테이션 누락
private LocalDateTime updatedAt;
private String updatedBy;

// 이후 — 어노테이션 추가
@LastModifiedDate
private LocalDateTime updatedAt;

@LastModifiedBy
private String updatedBy;
```

### 왜 중요한가?

JPA Auditing은 **데이터가 언제, 누가 수정했는지 자동으로 기록해주는 기능**입니다.

`@LastModifiedDate`가 없으면 `updatedAt`은 업데이트되어도 자동으로 갱신되지 않습니다.
`@LastModifiedBy`가 없으면 `updatedBy`가 누가 수정했는지 자동으로 채워지지 않습니다.

`@CreatedDate`, `@CreatedBy`는 이미 있었는데, 수정 관련 어노테이션만 빠져 있었던 것입니다.

---

## 변경 9 — 도메인 모델 책임 분리

**파일**: `Company.java`

### 변경 내용

```java
// 이전 — update() 메서드가 deletedStatus를 건드림
public void update(final CompanyUpdateRequest updateCompany) {
    this.name = updateCompany.name();
    this.type = updateCompany.type();
    this.address = updateCompany.address();
    this.number = updateCompany.number();
    this.description = updateCompany.description();
    this.deletedStatus = false;  // ← 이게 왜 여기 있지?
}

// 이후 — update()는 업체 정보만 수정
public void update(final CompanyUpdateRequest updateCompany) {
    this.name = updateCompany.name();
    this.type = updateCompany.type();
    this.address = updateCompany.address();
    this.number = updateCompany.number();
    this.description = updateCompany.description();
    // deletedStatus 라인 제거됨
}
```

### 왜 제거했는가?

- `update()`는 "업체 정보 수정" 역할만 해야 합니다
- 삭제 상태(`deletedStatus`)를 건드리는 것은 `BaseEntity.delete()`가 담당해야 합니다
- 만약 삭제된 업체를 수정하면 은근슬쩍 복구되는 의도하지 않은 동작이 발생했을 것입니다
- `validateNotDeleted()`가 추가되어 삭제된 업체는 애초에 수정/삭제 시도를 막습니다

---

## 변경 10 — QueryDSL 조건 일관성 수정

**파일**: `CompanyQueryRepository.java`

### 문제 — 데이터 쿼리와 count 쿼리의 조건이 달랐음

```java
// 이전 — findAll()의 count 쿼리가 다른 조건 사용 (버그!)
List<Company> companies = queryFactory
    .selectFrom(company)
    .where(company.deletedStatus.eq(false))  // deletedStatus 기준
    ...

long total = queryFactory
    .select(company.count())
    .from(company)
    .where(company.deletedAt.isNull())  // ← deletedAt 기준! (불일치)
    ...
```

이렇게 되면 "삭제 안 된 업체 목록" 10개를 가져오는데 "total 카운트"는 다른 기준으로 15개가 나오는 상황이 발생합니다.

### 해결 — 동일한 조건으로 통일 + NPE 방지

```java
// 이후 — 둘 다 deletedStatus.isFalse() 사용, Optional로 NPE 방지
List<Company> companies = queryFactory
    .where(company.deletedStatus.isFalse())
    ...

long total = Optional.ofNullable(queryFactory
        .select(company.count())
        .from(company)
        .where(company.deletedStatus.isFalse())
        .fetchFirst()).orElse(0L);  // null 가능성 제거
```

**`isFalse()` 변경**: `deletedStatus.eq(false)` → `deletedStatus.isFalse()`로 변경했는데, QueryDSL에서 더 가독성이 좋고 관용적인 표현입니다.

**`Optional.ofNullable().orElse(0L)` 추가**: `fetchFirst()`와 `fetchOne()`은 결과가 없을 때 `null`을 반환할 수 있습니다. 이를 기본형 `long`에 바로 대입하면 `NullPointerException`이 발생합니다. `Optional.ofNullable()`로 감싸서 null이면 `0L`을 반환하도록 안전하게 처리했습니다.

```java
// getCompaniesByKeyword()의 count도 동일하게 처리
long total = Optional.ofNullable(queryFactory
        .select(company.count())
        .from(company)
        .where(company.name.containsIgnoreCase(keyword)
                .and(company.deletedStatus.isFalse()))
        .fetchOne()).orElse(0L);
```

---

## 변경 11 — GlobalExceptionHandler 추가 (신규)

**파일**: `infrastructure/exception/GlobalExceptionHandler.java` (새로 생성)

### 문제 — 예외가 터져도 클라이언트에 500만 반환됨

기존에는 `GlobalExceptionHandler`가 없어서 `CompanyException`이 발생해도 Spring이 500 Internal Server Error로 뭉뚱그려 반환했습니다.

```json
// 기존 — 아무 정보가 없어 디버깅 불가
{
    "timestamp": "2026-04-30T09:24:51.629+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/api/v1/companies/..."
}
```

### 해결 — 예외 종류별로 적절한 HTTP 응답 반환

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // CompanyException → CompanyExceptionCode의 HTTP 상태 코드 반환
    @ExceptionHandler(CompanyException.class)
    public ResponseEntity<ApiResponse<String>> handleCompanyException(CompanyException ex) {
        return ResponseUtil.customResponse(ex.getErrorCode().getHttpStatus(), ex.getMessage());
    }

    // 권한 없음 → 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseUtil.customResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 유효성 검증 실패 (@Valid) → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(...) { ... }

    // 잘못된 파라미터 → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(...) { ... }
}
```

```json
// 이제 — 명확한 에러 정보 반환
{
    "status": "error",
    "data": "Company Not Found"
}
```

### 처리되는 예외 목록

| 예외 | HTTP 코드 | 상황 |
|------|-----------|------|
| `CompanyException(NOT_FOUND)` | 404 | 없는 업체 조회 |
| `CompanyException(ALREADY_DELETED)` | 410 | 삭제된 업체 수정/재삭제 시도 |
| `AccessDeniedException` | 403 | 권한 없는 사용자 접근 |
| `MethodArgumentNotValidException` | 400 | 요청 body 유효성 검증 실패 |
| `IllegalArgumentException` | 400 | 잘못된 sort 파라미터 등 |

---

## 변경 12 — ROLE_SELLER 소속 업체 소유권 검증 추가

**파일**: `CompanyService.java`
**계기**: 코드 리뷰 (STW5)

### 문제 — ROLE_SELLER가 모든 업체를 수정/삭제 가능

```java
// 이전 — role만 확인, 어느 업체든 수정/삭제 가능
private void validateAuthorization(String role) {
    if (!ROLE_MASTER.equals(role) && !ROLE_SELLER.equals(role)) {
        throw new AccessDeniedException("업체 관리 권한이 없습니다.");
    }
}
```

SELLER 권한이 있으면 `companyId`만 알면 타인의 업체도 수정/삭제할 수 있는 보안 취약점이었습니다.

### 해결 — 소유권 검증 메서드 추가

```java
// 추가된 메서드
private void validateOwnership(Company company, UUID userId, String role) {
    if (ROLE_SELLER.equals(role) && !company.getOwner().equals(userId)) {
        throw new AccessDeniedException("소속 업체만 관리할 수 있습니다.");
    }
}
```

```java
// updateCompany, deleteCompany 모두 동일하게 적용
validateNotDeleted(company);
validateOwnership(company, userId, role);  // 추가
```

### 역할별 동작

| 역할 | 조회 | 생성 | 수정/삭제 |
|------|------|------|-----------|
| `ROLE_MASTER` | 전체 가능 | 가능 | 모든 업체 가능 |
| `ROLE_SELLER` | 전체 가능 | 가능 | **소속 업체만** 가능 |
| 그 외 | 전체 가능 | 불가 (403) | 불가 (403) |

---

## 변경 13 — 단건 조회 소프트 삭제 필터링 추가

**파일**: `CompanyService.java`
**계기**: 코드 리뷰 (STW5)

### 문제 — 목록/검색과 단건 조회의 동작 불일치

```java
// 이전 — 삭제된 업체도 그대로 반환
public CompanyGetOneResponse getCompany(final UUID id) {
    Company company = companyRepository.findById(id)
            .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
    return CompanyGetOneResponse.of(company);  // deletedStatus 확인 없음
}
```

목록(`getCompanies`)과 검색(`getCompaniesByKeyword`)은 `deletedStatus = false` 조건으로 삭제된 업체를 제외하는데, 단건 조회만 삭제된 업체를 그대로 내려주는 불일치가 있었습니다.

### 해결 — `validateNotDeleted` 추가

```java
// 이후 — 삭제된 업체는 410 Gone 반환
public CompanyGetOneResponse getCompany(final UUID id) {
    Company company = companyRepository.findById(id)
            .orElseThrow(() -> new CompanyException(CompanyExceptionCode.NOT_FOUND));
    validateNotDeleted(company);  // 추가
    return CompanyGetOneResponse.of(company);
}
```

이로써 삭제된 업체에 대한 응답이 모든 엔드포인트에서 일관되게 처리됩니다.

---

## Postman 테스트 결과

2026-04-30 직접 테스트한 결과입니다. Gateway(`http://localhost:19091`) 경유, `Authorization: Bearer {token}` 헤더 포함.

| # | 요청 | 기대 결과 | 실제 결과 |
|---|------|-----------|-----------|
| 1 | `POST /api/v1/companies` — 업체 생성 | 200 OK | ✅ 통과 |
| 2 | `DELETE /api/v1/companies/{id}` — 업체 삭제 | 200 OK | ✅ 통과 |
| 3 | `PUT /api/v1/companies/{삭제된id}` — 삭제된 업체 수정 시도 | 410 Gone | ✅ 통과 |
| 4 | `DELETE /api/v1/companies/{삭제된id}` — 삭제된 업체 재삭제 | 410 Gone | ✅ 통과 |
| 5 | `GET /api/v1/companies/{없는id}` — 없는 업체 조회 | 404 Not Found | ✅ 통과 |
| 6 | `GET /api/v1/companies/search/농업?page=0&size=10` — 키워드 검색 | 200 + 결과 | ✅ 통과 |
| 7 | `GET /api/v1/companies?page=0&size=5` — 페이징 조회 | 200 + 올바른 total | ✅ 통과 |

**모든 테스트 통과.**

---

## 설계 결정 사항

코드 분석 중 확인된 "버그처럼 보이지만 의도된 설계" 목록입니다.

| 항목 | 결정 | 이유 |
|------|------|------|
| `getCompany()` — 삭제된 업체 조회 가능 | **변경됨** | 코드 리뷰 반영 — 목록/검색과 일관성을 위해 `validateNotDeleted` 추가, 삭제된 업체는 410 반환 |
| `CompanyUpdateRequest`에 `owner` 없음 | 의도된 설계 | 업체 소유자는 생성 시에만 지정, 변경 불가 |
| `page`, `size` 기본값 없음 | 의도된 설계 | 필수 파라미터로 명시적 전달 요구 |

---

## 최종 정리

이번 작업을 한 문장으로 요약하면:

> **"코드 품질을 높이고, 버그를 수정하고, 예외 처리를 완성했습니다."**

핵심 원칙:
1. **DRY (Don't Repeat Yourself)**: 같은 코드는 한 곳에만 — 메서드 추출
2. **SRP (Single Responsibility Principle)**: 각 메서드는 하나의 책임만 — 책임 분리
3. **버그 수정**: `||` → `&&`, count 쿼리 조건 통일, 올바른 예외 발생
4. **표준 준수**: SLF4J 로그 파라미터, JPA Auditing 완성
5. **예외 처리 완성**: GlobalExceptionHandler 추가로 500 → 의미있는 에러 코드 반환
