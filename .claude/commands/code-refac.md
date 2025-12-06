# Code Refactoring Rules

이 프로젝트의 코드 리팩토링 시 준수해야 할 규칙입니다.

---

## 1. 패키지 구조 및 계층 분리

### Controller Layer
- REST API 엔드포인트만 정의
- 비즈니스 로직 포함 금지
- Request/Response DTO만 사용
- @RestController, @RequiredArgsConstructor 사용
- HTTP 메서드별 명확한 매핑 (@GetMapping, @PostMapping 등)

### Service Layer
- 비즈니스 로직 구현
- 트랜잭션 관리 (@Transactional)
- Entity ↔ DTO 변환 처리
- Repository 계층 호출
- 단일 책임 원칙(SRP) 준수

### Repository Layer
- Spring Data JPA 인터페이스만 정의
- 복잡한 쿼리는 @Query 또는 QueryDSL 사용
- 메서드명 컨벤션 준수 (findBy, existsBy, countBy 등)

---

## 2. 명명 규칙

### 클래스명
- Controller: {Entity}Controller (예: MovieController)
- Service: {Entity}Service (예: MovieService)
- Repository: {Entity}Repository (예: MovieRepository)
- Entity: 도메인 명사 (예: Movie, Director)
- DTO: {Entity}Request, {Entity}Response (예: MovieRequest, MovieResponse)

### 메서드명
- 조회: get{Entity}, find{Entity}, search{Entity}
- 생성: save{Entity}, create{Entity}, register{Entity}
- 수정: update{Entity}, modify{Entity}
- 삭제: delete{Entity}, remove{Entity}
- 검증: validate{Target}, check{Target}
- 변환: to{Target}, from{Source}, convert{Target}

### 변수명
- camelCase 사용
- 명확하고 의미 있는 이름 (약어 지양)
- boolean: is, has, can 접두사 사용
- Collection: 복수형 사용 (movies, actors)

---

## 3. JPA Entity 규칙

### 주요 원칙
- @Setter 사용 금지 (불변성 보장)
- 기본 생성자는 protected로 설정
- 연관관계 기본 전략: FetchType.LAZY
- 양방향 연관관계 지양, 단방향 선호
- 비즈니스 메서드로 상태 변경

---

## 4. DTO 규칙

### Request DTO
- Java 17 Record 사용 권장
- 검증 로직 포함 가능
- 필수 필드 null 체크

### Response DTO
- Java 17 Record 사용 권장
- of(), from() 정적 팩토리 메서드로 변환
- Entity를 직접 반환 금지

---

## 5. 예외 처리

### 규칙
- 체크 예외(Checked Exception) 지양
- 런타임 예외(Runtime Exception) 사용
- 도메인별 커스텀 예외 정의
- @RestControllerAdvice로 전역 예외 처리

---

## 6. 트랜잭션 관리

### Service Layer
- 클래스 레벨: @Transactional(readOnly = true)
- 수정 메서드: @Transactional (readOnly = false)
- 트랜잭션 범위는 Service 레이어에서만
- OSIV=false 설정이므로 Service 내에서 연관 엔티티 로딩

---

## 7. 테스트 코드 규칙

### 규칙
- 테스트 메서드명: 한글 사용 (영화_조회_성공)
- Given-When-Then 패턴 필수
- AssertJ 사용 (assertThat())
- 단위 테스트: Mockito 사용
- 통합 테스트: @SpringBootTest + 실제 DB
- 테스트 간 독립성 보장

---

## 8. 코드 품질

### 불필요한 코드 제거
- 사용하지 않는 import 제거
- 주석 처리된 코드 삭제
- 사용하지 않는 메서드/변수 삭제
- TODO 주석은 Jira 티켓으로 전환

### 매직 넘버/문자열 제거
- 상수로 정의하여 사용
- 의미 있는 상수명 사용

### 메서드 길이
- 한 메서드는 20줄 이내 권장
- 복잡한 로직은 private 메서드로 분리
- 중첩 depth는 3 이하 유지

### null 체크
- Optional 사용 권장
- orElse, orElseThrow 활용

---

## 9. JPA 성능 최적화

### N+1 문제 해결
- Fetch Join 사용
- @EntityGraph 사용
- Batch Fetch Size 설정 활용

### DTO 직접 조회
- 복잡한 조회는 DTO로 직접 조회
- @Query에서 new 키워드로 DTO 생성

---

## 10. Lombok 사용 규칙

### 허용
- @Getter: Entity, DTO
- @RequiredArgsConstructor: Controller, Service (final 필드 주입)
- @NoArgsConstructor(access = AccessLevel.PROTECTED): Entity
- @Builder: DTO, 테스트 픽스처

### 금지
- @Setter: 불변성 위반
- @Data: 너무 많은 기능 포함
- @AllArgsConstructor: Entity에서 사용 금지
- @ToString: Entity에서 사용 금지 (순환 참조 위험)

---

## 11. API 응답 형식

### 성공 응답
- 공통 Response 래퍼 사용
- success, data, message 필드 포함

### 에러 응답
- 공통 ErrorResponse 사용
- success, message, code 필드 포함

---

## 12. 리팩토링 체크리스트

리팩토링 전 다음 항목을 확인하세요:

- 기존 테스트가 모두 통과하는가?
- 새로운 테스트 코드를 작성했는가?
- 메서드/클래스명이 명확한가?
- 불필요한 주석/코드를 제거했는가?
- 매직 넘버/문자열을 상수화했는가?
- null 체크를 Optional로 처리했는가?
- JPA N+1 문제가 없는가?
- 트랜잭션 범위가 적절한가?
- Entity를 직접 반환하지 않는가?
- Lombok을 적절히 사용했는가?

---

## 참고 자료
- Spring Boot Best Practices
- Effective Java 3rd Edition
- Clean Code by Robert C. Martin