#!/bin/bash

# =============================================================================
# Security Check Hook Script for Backend Development
# =============================================================================
# 목적: 민감 정보, 시크릿 키, 비밀번호 등이 코드에 포함되는 것을 방지
# 실행 시점: PreToolUse (Edit, Write, Create 작업 전)
# =============================================================================

# 색상 정의
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}🛡️  [보안체크]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}⚠️  [경고]${NC} $1"
}

log_error() {
    echo -e "${RED}🚨 [위험]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅ [안전]${NC} $1"
}

# =============================================================================
# 1. 민감 정보 패턴 정의 (Backend 서버 개발용)
# =============================================================================

# 민감 정보 패턴 배열
declare -a SENSITIVE_PATTERNS=(
    # AWS 관련
    "AKIA[0-9A-Z]{16}"                           # AWS Access Key
    "aws_access_key_id"
    "aws_secret_access_key"

    # 데이터베이스 연결 정보
    "jdbc:mysql://.*password="
    "jdbc:postgresql://.*password="
    "spring.datasource.password\s*=\s*['\"]?[^'\"\s]+"
    "spring.datasource.username\s*=\s*['\"]?root['\"]?"

    # JWT 및 시크릿 키
    "jwt\.secret\s*=\s*['\"]?[^'\"\s]{16,}"
    "jwt\.secretKey\s*=\s*['\"]?[^'\"\s]{16,}"
    "secret[_-]?key\s*[=:]\s*['\"]?[A-Za-z0-9+/]{32,}"

    # API 키 패턴
    "api[_-]?key\s*[=:]\s*['\"]?[A-Za-z0-9]{20,}"
    "apiKey\s*[=:]\s*['\"]?[A-Za-z0-9]{20,}"

    # Private Key
    "-----BEGIN (RSA |EC )?PRIVATE KEY-----"
    "BEGIN PRIVATE KEY"

    # 비밀번호 (하드코딩)
    "password\s*[=:]\s*['\"]?[^'\"\s]{8,}['\"]?"
    "passwd\s*[=:]\s*['\"]?[^'\"\s]{8,}"

    # 기타 민감 정보
    "bearer [A-Za-z0-9\-\._~\+\/]+=*"
    "token\s*[=:]\s*['\"]?[A-Za-z0-9\-\._~\+\/]{32,}"
)

# 허용된 패턴 (false positive 방지) - 정규식 패턴
declare -a ALLOWED_PATTERNS=(
    '\$\{[A-Z_]+\}'                              # ${ENV_VAR} 환경변수 참조
    '@Value\('                                    # Spring @Value 어노테이션
    'System\.getenv'                              # 환경변수 읽기
    'properties\.get'                             # Properties 읽기
    'env\.get'                                    # env 읽기
)

# =============================================================================
# 2. 제외할 파일/디렉토리 (체크 불필요)
# =============================================================================

# 제외할 파일 패턴
declare -a EXCLUDED_FILES=(
    ".gradle"
    "build/"
    "target/"
    ".idea/"
    ".git/"
    "*.class"
    "*.jar"
    "*.war"
    "node_modules/"
    ".DS_Store"
    "*.md"           # 문서 파일
    "CLAUDE.md"
    "README.md"
)

# =============================================================================
# 3. 파일 체크 함수
# =============================================================================

check_file() {
    local file="$1"
    local found_issues=0

    # 파일 존재 여부 확인
    if [[ ! -f "$file" ]]; then
        return 0
    fi

    # 제외 파일 체크
    for excluded in "${EXCLUDED_FILES[@]}"; do
        if [[ "$file" == *"$excluded"* ]]; then
            return 0
        fi
    done

    log_info "파일 검사 중: $file"

    # 민감 정보 패턴 체크
    for pattern in "${SENSITIVE_PATTERNS[@]}"; do
        if grep -qiE "$pattern" "$file" 2>/dev/null; then
            # 허용된 패턴인지 확인
            local is_allowed=false
            for allowed in "${ALLOWED_PATTERNS[@]}"; do
                if grep -qiE "$allowed" "$file" 2>/dev/null; then
                    is_allowed=true
                    break
                fi
            done

            if [[ "$is_allowed" == false ]]; then
                log_error "민감 정보 패턴 발견!"
                log_error "  파일: $file"
                log_error "  패턴: $pattern"
                echo ""
                echo -e "${YELLOW}해당 라인:${NC}"
                grep -niE "$pattern" "$file" | head -3
                echo ""
                found_issues=$((found_issues + 1))
            fi
        fi
    done

    return $found_issues
}

# =============================================================================
# 4. 특정 파일 타입별 추가 체크
# =============================================================================

check_application_properties() {
    local file="$1"

    if [[ "$file" == *"application.properties"* ]] || [[ "$file" == *"application.yml"* ]] || [[ "$file" == *"application.yaml"* ]]; then
        log_warn "Spring 설정 파일 감지: $file"

        # application-local.* 파일이 아니면 경고
        if [[ "$file" != *"application-local"* ]]; then
            log_warn "운영/공용 설정 파일입니다. 민감 정보 포함 여부를 다시 확인하세요!"

            # 특정 키워드 체크
            if grep -qiE "(password|secret|key|token)" "$file" 2>/dev/null; then
                if ! grep -qE "(\\\$\{|@Value)" "$file" 2>/dev/null; then
                    log_error "설정 파일에 하드코딩된 민감 정보가 있을 수 있습니다!"
                    return 1
                fi
            fi
        fi
    fi

    return 0
}

check_env_files() {
    local file="$1"

    if [[ "$file" == *".env"* ]] || [[ "$file" == *"credentials"* ]]; then
        log_error ".env 또는 credentials 파일 감지!"
        log_error "이 파일은 Git에 커밋되면 안 됩니다!"
        log_warn "  .gitignore에 추가되었는지 확인하세요."
        return 1
    fi

    return 0
}

# =============================================================================
# 5. 메인 실행 로직
# =============================================================================

main() {
    log_info "보안 체크 시작..."
    echo ""

    local total_issues=0

    # 환경 변수에서 파일 경로 가져오기 (Claude Code가 전달)
    # 기본값: 최근 수정된 파일들 체크
    local files_to_check=""

    if [[ -n "$CLAUDE_FILE_PATH" ]]; then
        files_to_check="$CLAUDE_FILE_PATH"
    else
        # Git staged 파일 체크
        files_to_check=$(git diff --cached --name-only 2>/dev/null)

        # staged 파일이 없으면 최근 수정 파일 체크
        if [[ -z "$files_to_check" ]]; then
            files_to_check=$(git diff --name-only 2>/dev/null)
        fi
    fi

    # 파일이 없으면 종료
    if [[ -z "$files_to_check" ]]; then
        log_success "체크할 파일이 없습니다."
        return 0
    fi

    # 각 파일 체크
    while IFS= read -r file; do
        if [[ -n "$file" ]]; then
            check_file "$file"
            local file_issues=$?
            total_issues=$((total_issues + file_issues))

            check_application_properties "$file"
            local config_issues=$?
            total_issues=$((total_issues + config_issues))

            check_env_files "$file"
            local env_issues=$?
            total_issues=$((total_issues + env_issues))
        fi
    done <<< "$files_to_check"

    echo ""
    echo "================================"

    if [[ $total_issues -gt 0 ]]; then
        log_error "총 $total_issues 개의 보안 이슈가 발견되었습니다!"
        echo ""
        log_warn "권장 조치:"
        echo "  1. 민감 정보를 환경 변수로 대체하세요."
        echo "  2. Spring: @Value(\"\${property}\") 사용"
        echo "  3. AWS Parameter Store 또는 Secrets Manager 사용"
        echo "  4. .env 파일은 .gitignore에 추가"
        echo ""
        log_warn "그래도 계속 진행하시겠습니까? (경고만 표시, 차단하지 않음)"
        return 0  # 경고만 하고 실제 차단은 하지 않음
    else
        log_success "보안 체크 통과! 민감 정보가 발견되지 않았습니다."
        return 0
    fi
}

# 스크립트 실행
main "$@"