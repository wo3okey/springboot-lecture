#!/bin/bash

# 시니어 멘토 - 코드 리뷰 및 보안 검증 시스템
# 역할: 프로젝트 구조 파악, 코드 컨벤션 검증, 보안 취약점 탐지

set -e

# 색상 정의
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 아이콘
MENTOR="🎓"
WARNING="⚠️"
ERROR="❌"
SUCCESS="✅"
SECURITY="🔒"
STRUCTURE="🏗️"

echo "${BLUE}${MENTOR} 시니어 멘토: 코드 리뷰를 시작합니다...${NC}"

# 환경 변수에서 파일 경로 추출
FILE_PATH="${CLAUDE_FILE_PATH}"
TOOL_NAME="${CLAUDE_TOOL_NAME}"

if [ -z "$FILE_PATH" ]; then
    echo "${GREEN}${SUCCESS} 시니어 멘토: 검증할 파일이 없습니다.${NC}"
    exit 0
fi

echo "${BLUE}${MENTOR} 검토 대상: ${FILE_PATH}${NC}"
echo "${BLUE}${MENTOR} 검증 범위: 전체 파일 (동일 클래스 내 모든 코드)${NC}"

# 경고 카운터
WARNING_COUNT=0
ERROR_COUNT=0

# 파일 전체 내용을 읽어옴
if [ -f "$FILE_PATH" ]; then
    FILE_CONTENT=$(cat "$FILE_PATH")
else
    echo "${RED}${ERROR} 파일을 찾을 수 없습니다: ${FILE_PATH}${NC}"
    exit 0
fi

# 1. 보안 검증 - 민감 정보 하드코딩 체크
echo ""
echo "${SECURITY} [보안 검증] 민감 정보 노출 여부 확인 중..."

SECURITY_PATTERNS=(
    "password\s*=\s*['\"].*['\"]"
    "secret\s*=\s*['\"].*['\"]"
    "apiKey\s*=\s*['\"].*['\"]"
    "token\s*=\s*['\"].*['\"]"
    "private.*key\s*=\s*['\"].*['\"]"
    "jdbc.*password"
    "aws.*secret"
    "api_key"
)

for pattern in "${SECURITY_PATTERNS[@]}"; do
    LINE_NUMBERS=$(grep -inE "$pattern" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${ERROR}${RED} 민감 정보 하드코딩 발견: ${pattern}${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        echo "${YELLOW}   → 환경 변수 또는 AWS Parameter Store를 사용하세요!${NC}"
        ERROR_COUNT=$((ERROR_COUNT + 1))
    fi
done

# 2. SQL Injection 취약점 체크
echo ""
echo "${SECURITY} [보안 검증] SQL Injection 취약점 확인 중..."

LINE_NUMBERS=$(grep -nE "executeQuery.*\+|createQuery.*\+|createNativeQuery.*\+" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
if [ -n "$LINE_NUMBERS" ]; then
    echo "${ERROR}${RED} SQL Injection 위험: 문자열 연결로 쿼리 생성 발견${NC}"
    echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
    echo "${YELLOW}   → PreparedStatement 또는 JPA @Query를 사용하세요!${NC}"
    ERROR_COUNT=$((ERROR_COUNT + 1))
fi

# 3. 코드 컨벤션 검증 - Java 파일인 경우
if [[ "$FILE_PATH" == *.java ]]; then
    echo ""
    echo "${STRUCTURE} [코드 컨벤션] Java 코딩 규칙 검증 중..."

    # 클래스명 PascalCase 체크
    LINE_NUMBERS=$(grep -nE "^(public|private|protected)?\s*(class|interface|enum)\s+[a-z]" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${WARNING}${YELLOW} 클래스명은 PascalCase를 사용해야 합니다${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi

    # 상수는 UPPER_SNAKE_CASE
    LINE_NUMBERS=$(grep -nE "static\s+final.*[a-z].*=" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${WARNING}${YELLOW} 상수명은 UPPER_SNAKE_CASE를 사용해야 합니다${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi

    # @Transactional 없는 save/update/delete 체크 (Service 레이어)
    if [[ "$FILE_PATH" == *Service.java ]]; then
        LINE_NUMBERS=$(grep -nE "public.*\s+(save|update|delete|create|remove)" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
        if [ -n "$LINE_NUMBERS" ]; then
            if ! echo "$FILE_CONTENT" | grep "@Transactional" > /dev/null 2>&1; then
                echo "${WARNING}${YELLOW} Service 레이어의 변경 메서드에 @Transactional이 필요할 수 있습니다${NC}"
                echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
                WARNING_COUNT=$((WARNING_COUNT + 1))
            fi
        fi
    fi

    # System.out.println 사용 체크
    LINE_NUMBERS=$(grep -n "System\.out\.println" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${WARNING}${YELLOW} System.out.println 대신 Logger를 사용하세요${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        echo "${YELLOW}   → private final Logger logger = LoggerFactory.getLogger(ClassName.class);${NC}"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi

    # 예외 처리 없는 catch 블록
    LINE_NUMBERS=$(grep -nA 1 "catch.*Exception" "$FILE_PATH" 2>/dev/null | grep "^\s*}$" | cut -d- -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${ERROR}${RED} 빈 catch 블록 발견: 예외를 적절히 처리하세요${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        ERROR_COUNT=$((ERROR_COUNT + 1))
    fi
fi

# 4. Spring Boot 관련 검증
if [[ "$FILE_PATH" == *.java ]]; then
    echo ""
    echo "${STRUCTURE} [Spring Boot] 프레임워크 베스트 프랙티스 검증 중..."

    # Controller에서 직접 Repository 호출
    if [[ "$FILE_PATH" == *Controller.java ]]; then
        LINE_NUMBERS=$(grep -nE "Repository\s+\w+;" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
        if [ -n "$LINE_NUMBERS" ]; then
            echo "${WARNING}${YELLOW} Controller에서 Repository를 직접 호출하지 마세요${NC}"
            echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
            echo "${YELLOW}   → Service 레이어를 거쳐야 합니다 (Layered Architecture)${NC}"
            WARNING_COUNT=$((WARNING_COUNT + 1))
        fi
    fi

    # @RequestBody 없는 입력 검증
    if [[ "$FILE_PATH" == *Controller.java ]] && echo "$FILE_CONTENT" | grep "@RequestBody" > /dev/null 2>&1; then
        if ! echo "$FILE_CONTENT" | grep "@Valid" > /dev/null 2>&1; then
            LINE_NUMBERS=$(grep -n "@RequestBody" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
            echo "${WARNING}${YELLOW} @RequestBody에 @Valid를 추가하여 입력 검증을 수행하세요${NC}"
            echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
            WARNING_COUNT=$((WARNING_COUNT + 1))
        fi
    fi

    # Entity에 비즈니스 로직이 너무 많은 경우 (안티패턴)
    if [[ "$FILE_PATH" == *entity/*.java ]]; then
        LINE_NUMBERS=$(grep -nE "@Service|@Component" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
        if [ -n "$LINE_NUMBERS" ]; then
            echo "${ERROR}${RED} Entity 클래스에 Service 계층의 의존성이 있습니다${NC}"
            echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
            echo "${YELLOW}   → Domain 모델은 순수해야 합니다 (DDD 원칙)${NC}"
            ERROR_COUNT=$((ERROR_COUNT + 1))
        fi
    fi
fi

# 5. 설정 파일 검증
if [[ "$FILE_PATH" == *.yml ]] || [[ "$FILE_PATH" == *.yaml ]] || [[ "$FILE_PATH" == *.properties ]]; then
    echo ""
    echo "${SECURITY} [설정 파일] 보안 설정 검증 중..."

    # useSSL=false 체크
    LINE_NUMBERS=$(grep -n "useSSL=false" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${ERROR}${RED} 데이터베이스 연결에 SSL을 비활성화하지 마세요${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        echo "${YELLOW}   → useSSL=true로 변경하세요${NC}"
        ERROR_COUNT=$((ERROR_COUNT + 1))
    fi

    # show_sql: true in production
    LINE_NUMBERS=$(grep -nE "show_sql:\s*true" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${WARNING}${YELLOW} 운영 환경에서는 show_sql을 false로 설정하세요${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi

    # TRACE 로깅 레벨
    LINE_NUMBERS=$(grep -n "TRACE" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${WARNING}${YELLOW} TRACE 로깅 레벨은 개발 환경에서만 사용하세요${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi

    # Actuator 전체 노출
    LINE_NUMBERS=$(grep -nE "include:\s*\*|include:\s*'\\*'" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
    if [ -n "$LINE_NUMBERS" ]; then
        echo "${ERROR}${RED} Actuator 엔드포인트를 전체 노출하지 마세요${NC}"
        echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
        echo "${YELLOW}   → 필요한 엔드포인트만 명시적으로 지정하세요${NC}"
        ERROR_COUNT=$((ERROR_COUNT + 1))
    fi
fi

# 6. 테스트 코드 검증
if [[ "$FILE_PATH" == *Test.java ]]; then
    echo ""
    echo "${STRUCTURE} [테스트 코드] 테스트 품질 검증 중..."

    # Given-When-Then 패턴 체크
    if ! echo "$FILE_CONTENT" | grep -E "//\s*(given|when|then)" > /dev/null 2>&1; then
        echo "${WARNING}${YELLOW} Given-When-Then 패턴을 사용하여 테스트 가독성을 높이세요${NC}"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi

    # @Test 없는 테스트 메서드
    if echo "$FILE_CONTENT" | grep -E "void\s+test" > /dev/null 2>&1; then
        if ! echo "$FILE_CONTENT" | grep "@Test" > /dev/null 2>&1; then
            echo "${ERROR}${RED} 테스트 메서드에 @Test 어노테이션이 누락되었습니다${NC}"
            ERROR_COUNT=$((ERROR_COUNT + 1))
        fi
    fi
fi

# 7. 일반적인 코드 품질 체크
echo ""
echo "${STRUCTURE} [코드 품질] 일반 품질 기준 검증 중..."

# TODO/FIXME 주석 체크
LINE_NUMBERS=$(grep -nE "TODO|FIXME" "$FILE_PATH" 2>/dev/null | cut -d: -f1 | tr '\n' ',' | sed 's/,$//')
if [ -n "$LINE_NUMBERS" ]; then
    TODO_COUNT=$(echo "$LINE_NUMBERS" | tr ',' '\n' | wc -l | tr -d ' ')
    echo "${WARNING}${YELLOW} TODO/FIXME 주석이 ${TODO_COUNT}개 발견되었습니다${NC}"
    echo "${YELLOW}   → 위치: ${FILE_PATH}:${LINE_NUMBERS}${NC}"
    echo "${YELLOW}   → 가능한 빨리 처리하세요${NC}"
fi

# Magic Number 체크 (숫자 리터럴이 3개 이상)
if echo "$FILE_CONTENT" | grep -E "\s[0-9]{2,}\s" > /dev/null 2>&1; then
    echo "${WARNING}${YELLOW} Magic Number가 발견되었습니다. 상수로 추출을 고려하세요${NC}"
    WARNING_COUNT=$((WARNING_COUNT + 1))
fi

# 결과 출력
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "${MENTOR} 시니어 멘토 리뷰 결과:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $ERROR_COUNT -eq 0 ] && [ $WARNING_COUNT -eq 0 ]; then
    echo "${GREEN}${SUCCESS} 완벽합니다! 코드가 모든 검증을 통과했습니다.${NC}"
    echo "${GREEN}   Keep up the good work! 🎉${NC}"
    exit 0
elif [ $ERROR_COUNT -gt 0 ]; then
    echo "${RED}${ERROR} 심각한 문제: ${ERROR_COUNT}개${NC}"
    echo "${YELLOW}${WARNING} 경고: ${WARNING_COUNT}개${NC}"
    echo ""
    echo "${RED}${MENTOR} 시니어 멘토: 위 이슈를 수정한 후 다시 시도하세요!${NC}"
    echo "${YELLOW}   보안 및 아키텍처 문제가 발견되었습니다.${NC}"
    exit 1
else
    echo "${YELLOW}${WARNING} 경고: ${WARNING_COUNT}개${NC}"
    echo ""
    echo "${YELLOW}${MENTOR} 시니어 멘토: 경고 사항을 검토해주세요.${NC}"
    echo "${YELLOW}   코드가 작동하지만, 개선의 여지가 있습니다.${NC}"
    exit 0
fi
