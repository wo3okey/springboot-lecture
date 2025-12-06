# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 프로젝트 개요
Spring Boot 3.2 기반 영화(Movie) 관리 학습 프로젝트입니다. JPA, AOP, Monitoring(Prometheus/Grafana) 등 Spring Boot 핵심 기능을 학습하기 위한 예제 코드입니다.

---

## 기술 스택
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.2
- **빌드 도구**: Gradle (Groovy DSL)
- **데이터베이스**: MySQL 8.0
- **ORM**: Spring Data JPA (Hibernate)
- **모니터링**: Spring Boot Actuator + Prometheus + Grafana
- **API 문서**: Springdoc OpenAPI 2.3.0 (Swagger)
- **테스트**: JUnit 5, Mockito
- **기타**: Lombok, AOP

---

## 빌드 및 실행 명령어

### 빌드
```bash
# 전체 빌드 (테스트 포함)
./gradlew clean build

# 테스트 제외 빌드
./gradlew clean build -x test
```

### 테스트
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests MovieServiceMockTest

# 특정 테스트 메서드 실행
./gradlew test --tests MovieServiceMockTest.영화단건조회_불가_테스트
```

### 애플리케이션 실행
```bash
# Gradle로 실행
./gradlew bootRun

# 빌드된 JAR 실행
java -jar build/libs/springboot-lecture-0.0.1-SNAPSHOT.jar
```

### Docker
```bash
# Docker 이미지 빌드
docker build -t springboot-lecture .

# Docker 컨테이너 실행
docker run -p 8080:8080 springboot-lecture

# 모니터링 스택 실행 (Prometheus + Grafana)
cd monitoring
docker-compose up -d
```

---

## 프로젝트 구조

### 패키지 구조
```
src/main/java/com/example/
├── SpringbootLectureApplication.java    # 메인 애플리케이션
├── controller/                          # REST API 컨트롤러
│   └── MovieController.java
├── service/                             # 비즈니스 로직
│   ├── MovieService.java
│   └── LogService.java
├── repository/                          # 데이터 액세스 계층
│   ├── MovieRepository.java
│   └── LogRepository.java
├── domain/                              # 도메인 모델
│   ├── entity/                         # JPA 엔티티
│   │   ├── Movie.java
│   │   ├── Director.java
│   │   ├── Actor.java
│   │   ├── Investor.java
│   │   └── Log.java
│   ├── request/                        # 요청 DTO
│   │   └── MovieRequest.java
│   └── response/                       # 응답 DTO
│       └── MovieResponse.java
├── aop/                                # AOP (관점 지향 프로그래밍)
│   └── Timer.java                      # 실행 시간 측정 어노테이션
└── common/                             # 공통 컴포넌트
    ├── config/                         # 설정 클래스
    │   ├── SwaggerConfig.java
    │   └── JwtUtils.java
    ├── Response.java                   # 공통 응답 래퍼
    ├── ExceptionController.java        # 전역 예외 처리
    └── ExceptionResponse.java          # 예외 응답 DTO
```

### 테스트 구조
```
src/test/java/com/example/
├── SpringbootLectureApplicationTests.java
└── service/
    ├── BaseTest.java                   # 테스트 베이스 클래스
    └── MovieServiceMockTest.java       # Mock 기반 단위 테스트
```

---

## 아키텍처 특징

### 계층 아키텍처
전통적인 3-Layer Architecture를 따릅니다:
1. **Controller Layer**: REST API 엔드포인트 정의, HTTP 요청/응답 처리
2. **Service Layer**: 비즈니스 로직 구현, 트랜잭션 관리
3. **Repository Layer**: 데이터 액세스, JPA를 통한 DB 연동

### JPA 연관관계
- `Movie` 엔티티가 중심 엔티티
- **@OneToOne**: Movie ↔ Director (영화 1개당 감독 1명)
- **@OneToMany**: Movie → Actor (영화 1개당 배우 여러 명)
- **@OneToMany**: Movie → Investor (영화 1개당 투자자 여러 명)
- **기본 전략**: LAZY Loading (지연 로딩)
- **배치 최적화**: `default_batch_fetch_size: 20` 설정

### AOP 활용
- `@Timer` 어노테이션: 메서드 실행 시간 자동 측정
- `TimerImpl` Aspect에서 `@Around` advice로 실행 시간 로깅
- Spring AOP (`spring-boot-starter-aop`) 사용

### OSIV (Open Session In View)
- **설정**: `spring.jpa.open-in-view: false`
- OSIV를 끈 상태이므로, Service 레이어에서 연관 엔티티를 명시적으로 로딩해야 함
- `/api/v1/movies/{movieId}/osiv-error` 엔드포인트는 OSIV=true 필요 (학습용)

### 모니터링
- **Spring Boot Actuator**: `/actuator/health`, `/actuator/prometheus` 엔드포인트
- **Prometheus**: 메트릭 수집 (포트 9090)
- **Grafana**: 메트릭 시각화 (포트 3000)
- `monitoring/docker-compose.yml`로 모니터링 스택 실행

---

## 데이터베이스 설정

### MySQL 로컬 환경
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/boot_test?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&serverTimezone=Asia/Seoul
    username: <설정 필요>
    password: <설정 필요>
```

**중요**: `username`, `password`는 로컬 환경에 맞게 설정해야 합니다.

### Docker로 MySQL 실행
```bash
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=boot_test \
  -p 3306:3306 \
  mysql:8
```

---

## API 엔드포인트

### Swagger UI
- **URL**: http://localhost:8080/swagger-ui/index.html
- API 문서 자동 생성 및 테스트 가능

### 주요 엔드포인트
| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/movies` | 영화 목록 조회 |
| GET | `/api/v1/movies/{movieId}` | 영화 단건 조회 |
| GET | `/api/v1/movies/multi-fetch-error` | N+1 문제 재현용 |
| GET | `/api/v1/movies/{movieId}/osiv-error` | OSIV 문제 재현용 |
| POST | `/api/v1/movies` | 영화 생성 |
| PUT | `/api/v1/movies/{movieId}` | 영화 수정 |
| DELETE | `/api/v1/movies/{movieId}` | 영화 삭제 |

---

## 테스트 작성 가이드

### 테스트 전략
- **단위 테스트**: Mockito로 의존성 모킹
- `@ExtendWith(MockitoExtension.class)` 사용
- `@Mock`: 의존성 모킹
- `@InjectMocks`: 테스트 대상 객체에 Mock 주입

### 테스트 명명 규칙
한글 메서드명 사용 (JUnit 5 지원):
- `영화단건조회_불가_테스트()`
- `영화단건_저장_테스트()`

### Given-When-Then 패턴
```java
@Test
public void 영화단건_저장_테스트() {
    // given: 테스트 데이터 준비
    MovieRequest request = new MovieRequest("영화명", 2002, 1L);
    Movie movie = new Movie("영화명", 2002);

    // when: Mock 동작 정의
    when(movieRepository.save(any(Movie.class))).thenReturn(movie);
    doNothing().when(logService).saveLog();

    // then: 실행 및 검증
    movieService.saveMovie(request);
}
```

---

## 주의사항

### JPA N+1 문제
- `getMoviesMultiFetchError()` 메서드는 의도적으로 N+1 문제를 발생시킴
- Fetch Join 또는 EntityGraph로 해결 가능
- `default_batch_fetch_size` 설정으로 부분 완화

### OSIV 설정
- 현재 `open-in-view: false` 상태
- Controller에서 Lazy Loading 시 `LazyInitializationException` 발생
- Service 레이어에서 `@Transactional` 내에서 연관 엔티티 로딩 필요

### Lombok 사용
- `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 활용
- Entity는 기본 생성자를 protected로 설정
- `@RequiredArgsConstructor`로 생성자 주입

### 로깅 설정
- Hibernate SQL 로깅: `show_sql: true`, `format_sql: true`
- JDBC 바인딩 로깅: `org.hibernate.orm.jdbc.bind: TRACE`
- JPA 트랜잭션 로깅: `org.springframework.transaction: DEBUG`

---

## 학습 목적 코드

이 프로젝트는 다음을 학습하기 위한 예제 코드입니다:

1. **JPA 기본**: Entity, Repository, 연관관계 매핑
2. **JPA 성능**: N+1 문제, Fetch Join, Batch Size
3. **OSIV**: Open Session In View 이해
4. **AOP**: `@Timer` 어노테이션으로 횡단 관심사 처리
5. **Spring Boot Actuator**: 헬스체크, 메트릭
6. **Prometheus/Grafana**: 모니터링 스택 구성
7. **테스트**: Mockito 기반 단위 테스트

---

## 마지막 업데이트
- **날짜**: 2025-12-06
- **버전**: 0.0.1-SNAPSHOT
