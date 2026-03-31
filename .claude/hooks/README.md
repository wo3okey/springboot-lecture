# Claude Code Hooks - 시니어 멘토 시스템

## 개요
이 디렉토리는 Claude Code의 자동화된 코드 리뷰 및 보안 검증 시스템을 포함합니다.

## 📁 파일 구조
```
.claude/hooks/
├── README.md                        # 이 문서
├── security-check.sh                # 보안 체크 스크립트 (경호원)
├── senior-mentor.sh                 # 종합 코드 리뷰 스크립트 (시니어 멘토)
├── test-senior-mentor-good.yml      # 테스트: 올바른 코드
└── test-senior-mentor-bad.java      # 테스트: 문제가 있는 코드
```

---

# 🎓 시니어 멘토 (Senior Mentor)

## 역할

시니어 개발자의 관점에서 코드를 종합적으로 검토합니다:

1. **숲을 보는 시야**: 프로젝트 전체 구조와 아키텍처 패턴 검증
2. **보안 전문가**: 취약점 탐지 및 시큐어 코딩 가이드
3. **즉각적인 피드백**: 문제가 되는 코드 작성 시 실시간 알림

## 검증 항목

### 1. 보안 검증 🔒
- **민감 정보 하드코딩 탐지**
  - password, secret, apiKey, token
  - AWS credentials
  - 데이터베이스 비밀번호

- **SQL Injection 취약점**
  - 문자열 연결로 쿼리 생성 (`executeQuery` + 변수)
  - PreparedStatement 미사용 체크

### 2. Java 코드 컨벤션 🏗️
- **명명 규칙**
  - 클래스명: PascalCase
  - 상수: UPPER_SNAKE_CASE

- **Spring 베스트 프랙티스**
  - Service 레이어의 `@Transactional` 누락 체크
  - `System.out.println` 대신 Logger 사용 권장
  - 빈 catch 블록 탐지

### 3. Spring Boot 아키텍처 검증 🏛️
- **계층 아키텍처 준수**
  - Controller에서 Repository 직접 호출 금지
  - Service 레이어를 통한 비즈니스 로직 처리

- **입력 검증**
  - `@RequestBody`에 `@Valid` 누락 체크

- **도메인 모델 순수성**
  - Entity에 Service 의존성 주입 금지 (DDD 원칙)

### 4. 설정 파일 보안 🔐
- **데이터베이스 연결**
  - `useSSL=false` 사용 금지
  - SSL 활성화 권장

- **로깅 레벨**
  - 운영 환경에서 `show_sql: true` 금지
  - `TRACE` 레벨 사용 경고

- **Actuator 보안**
  - 전체 엔드포인트 노출(`*`) 금지
  - 필요한 엔드포인트만 명시적 지정

### 5. 테스트 코드 품질 ✅
- **Given-When-Then 패턴** 준수 여부
- `@Test` 어노테이션 누락 체크

### 6. 일반 코드 품질 📊
- TODO/FIXME 주석 개수 확인
- Magic Number 탐지 (상수 추출 권장)

## 사용 방법

### 자동 실행 (권장)

Write 또는 Edit 도구 사용 시 자동으로 실행됩니다:

```json
// .claude/settings.json
"PreToolUse": [
  {
    "matcher": "Write|Edit",
    "hooks": [
      {
        "command": "bash .claude/hooks/senior-mentor.sh",
        "timeout": 15
      }
    ]
  }
]
```

### 수동 실행

```bash
# 특정 파일 검증
export CLAUDE_FILE_PATH="src/main/java/com/example/controller/MovieController.java"
export CLAUDE_TOOL_NAME="Write"
export CLAUDE_NEW_CONTENT="$(cat $CLAUDE_FILE_PATH)"
bash .claude/hooks/senior-mentor.sh
```

## 검증 결과 예시

### ✅ 성공 (Good Code)

```
🎓 시니어 멘토: 코드 리뷰를 시작합니다...
🎓 검토 대상: application.yml

🔒 [보안 검증] 민감 정보 노출 여부 확인 중...
🔒 [보안 검증] SQL Injection 취약점 확인 중...
🔒 [설정 파일] 보안 설정 검증 중...
🏗️ [코드 품질] 일반 품질 기준 검증 중...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎓 시니어 멘토 리뷰 결과:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ 완벽합니다! 코드가 모든 검증을 통과했습니다.
   Keep up the good work! 🎉
```

### ❌ 실패 (Bad Code)

```
🎓 시니어 멘토: 코드 리뷰를 시작합니다...
🎓 검토 대상: BadMovieController.java

🔒 [보안 검증] 민감 정보 노출 여부 확인 중...
❌ 민감 정보 하드코딩 발견: password\s*=\s*['"].*['"]
   → 환경 변수 또는 AWS Parameter Store를 사용하세요!
❌ 민감 정보 하드코딩 발견: apiKey\s*=\s*['"].*['"]
   → 환경 변수 또는 AWS Parameter Store를 사용하세요!

🔒 [보안 검증] SQL Injection 취약점 확인 중...

🏗️ [코드 컨벤션] Java 코딩 규칙 검증 중...
⚠️ System.out.println 대신 Logger를 사용하세요
   → private final Logger logger = LoggerFactory.getLogger(ClassName.class);

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎓 시니어 멘토 리뷰 결과:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❌ 심각한 문제: 2개
⚠️ 경고: 1개

🎓 시니어 멘토: 위 이슈를 수정한 후 다시 시도하세요!
   보안 및 아키텍처 문제가 발견되었습니다.
```

## 테스트

### 올바른 코드 테스트
```bash
CLAUDE_FILE_PATH=".claude/hooks/test-senior-mentor-good.yml" \
CLAUDE_TOOL_NAME="Write" \
CLAUDE_NEW_CONTENT="$(cat .claude/hooks/test-senior-mentor-good.yml)" \
bash .claude/hooks/senior-mentor.sh
```

### 문제 있는 코드 테스트
```bash
CLAUDE_FILE_PATH=".claude/hooks/test-senior-mentor-bad.java" \
CLAUDE_TOOL_NAME="Write" \
CLAUDE_NEW_CONTENT="$(cat .claude/hooks/test-senior-mentor-bad.java)" \
bash .claude/hooks/senior-mentor.sh
```

---

# 🛡️ 보안 체크 (Security Check)

## 🛡️ security-check.sh

### 목적
파일 수정(Edit, Write) 작업 전에 자동으로 실행되어 민감 정보가 코드에 포함되는 것을 방지합니다.

### 실행 시점
- **PreToolUse Hook**: Edit, Write 작업 전 자동 실행

### 체크 항목

#### 1. AWS 관련
- AWS Access Key (AKIA...)
- aws_access_key_id
- aws_secret_access_key

#### 2. 데이터베이스 연결 정보
- JDBC URL에 포함된 password
- spring.datasource.password (하드코딩)
- spring.datasource.username=root (위험한 설정)

#### 3. JWT 및 시크릿 키
- jwt.secret (하드코딩된 값)
- jwt.secretKey
- secret_key, secret-key

#### 4. API 키
- api_key, api-key, apiKey (하드코딩)

#### 5. Private Key
- RSA, EC Private Key
- BEGIN PRIVATE KEY

#### 6. 비밀번호 하드코딩
- password = "..."
- passwd = "..."

#### 7. 기타
- Bearer Token
- 긴 문자열 Token

### 허용 패턴 (False Positive 방지)
다음과 같은 경우는 **안전**하다고 판단하여 경고하지 않습니다:

✅ **환경 변수 참조**
```yaml
password: ${DB_PASSWORD}
secret: ${JWT_SECRET}
```

✅ **Spring @Value 어노테이션**
```java
@Value("${jwt.secret}")
private String jwtSecret;
```

✅ **환경변수 읽기**
```java
String password = System.getenv("DB_PASSWORD");
```

### 제외 파일
다음 파일/디렉토리는 체크하지 않습니다:
- `.gradle/`, `build/`, `target/`
- `.idea/`, `.git/`
- `*.class`, `*.jar`, `*.war`
- `node_modules/`
- `*.md` (문서 파일)

### 사용 방법

#### 1. 자동 실행 (권장)
`.claude/settings.json`에 이미 설정되어 있습니다.
```json
"PreToolUse": [
  {
    "matcher": "Write|Edit",
    "hooks": [
      {
        "type": "command",
        "command": "bash .claude/hooks/security-check.sh",
        "timeout": 10
      }
    ]
  }
]
```

#### 2. 수동 실행
```bash
# 특정 파일 체크
CLAUDE_FILE_PATH="src/main/resources/application.yml" bash .claude/hooks/security-check.sh

# Git staged 파일 체크
bash .claude/hooks/security-check.sh

# 모든 수정된 파일 체크
git diff --name-only | xargs -I {} bash .claude/hooks/security-check.sh
```

### 출력 예시

#### ✅ 안전한 경우
```
🛡️  [보안체크] 보안 체크 시작...
🛡️  [보안체크] 파일 검사 중: application.yml
================================
✅ [안전] 보안 체크 통과! 민감 정보가 발견되지 않았습니다.
```

#### 🚨 위험 감지
```
🛡️  [보안체크] 보안 체크 시작...
🛡️  [보안체크] 파일 검사 중: application.yml
🚨 [위험] 민감 정보 패턴 발견!
🚨 [위험]   파일: application.yml
🚨 [위험]   패턴: spring.datasource.password\s*=\s*['"]?[^'"\s]+

해당 라인:
6:    password: mySecretPassword123!

================================
🚨 [위험] 총 1 개의 보안 이슈가 발견되었습니다!

⚠️  [경고] 권장 조치:
  1. 민감 정보를 환경 변수로 대체하세요.
  2. Spring: @Value("${property}") 사용
  3. AWS Parameter Store 또는 Secrets Manager 사용
  4. .env 파일은 .gitignore에 추가
```

## 권장 사항

### ❌ 나쁜 예 (하드코딩)
```yaml
# application.yml
spring:
  datasource:
    password: myPassword123

jwt:
  secret: thisIsMySecretKey12345
```

### ✅ 좋은 예 (환경 변수)
```yaml
# application.yml
spring:
  datasource:
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
```

### ✅ 좋은 예 (Spring @Value)
```java
@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String jwtSecret;
}
```

### ✅ 좋은 예 (로컬 전용 파일)
```yaml
# application-local.yml (Git에 커밋하지 않음)
spring:
  datasource:
    password: local_dev_password
```

## 커스터마이징

### 민감 정보 패턴 추가
`security-check.sh` 파일의 `SENSITIVE_PATTERNS` 배열에 추가:
```bash
declare -a SENSITIVE_PATTERNS=(
    # 기존 패턴...
    "your-custom-pattern"
)
```

### 허용 패턴 추가
`ALLOWED_PATTERNS` 배열에 추가:
```bash
declare -a ALLOWED_PATTERNS=(
    # 기존 패턴...
    'your-safe-pattern'
)
```

### 제외 파일 추가
`EXCLUDED_FILES` 배열에 추가:
```bash
declare -a EXCLUDED_FILES=(
    # 기존 패턴...
    "custom-excluded-dir/"
)
```

## 문제 해결

### Q: 안전한 코드인데 경고가 뜹니다
A: `ALLOWED_PATTERNS`에 해당 패턴을 추가하세요.

### Q: 특정 파일을 체크에서 제외하고 싶습니다
A: `EXCLUDED_FILES`에 파일 패턴을 추가하세요.

### Q: 스크립트가 실행되지 않습니다
A: 실행 권한을 확인하세요:
```bash
chmod +x .claude/hooks/security-check.sh
```

### Q: Hook이 실행되지 않습니다
A: `.claude/settings.json` 파일의 hooks 설정을 확인하세요.

## 참고 자료
- [Claude Code Hooks 공식 문서](https://docs.anthropic.com/claude-code/hooks)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/)

---

**마지막 업데이트**: 2025-12-06
**버전**: 1.0.0
