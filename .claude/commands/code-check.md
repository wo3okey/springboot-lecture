# Code Check Command

변경된 모든 코드를 시니어 멘토 훅을 통해 검증합니다.

---

## 실행 대상

Git에서 변경 사항이 있는 모든 파일들:
- Modified (M): 수정된 파일
- Added (A): 새로 추가된 파일
- Staged & Modified (MM): 스테이징되고 추가 수정된 파일

---

## 검증 항목

### 1. 보안 검증
- 민감 정보 하드코딩 체크 (password, secret, apiKey, token 등)
- SQL Injection 취약점 체크

### 2. 코드 컨벤션
- 클래스명 PascalCase
- 상수명 UPPER_SNAKE_CASE
- @Transactional 사용 여부
- System.out.println → Logger 사용
- 빈 catch 블록

### 3. Spring Boot 베스트 프랙티스
- Controller에서 Repository 직접 호출 금지
- @RequestBody에 @Valid 사용
- Entity 순수성 유지

### 4. 설정 파일 검증
- SSL 설정
- show_sql 설정 (운영 환경)
- 로깅 레벨
- Actuator 엔드포인트 노출

### 5. 테스트 코드 품질
- Given-When-Then 패턴
- @Test 어노테이션 누락

### 6. 코드 품질
- TODO/FIXME 주석
- Magic Number

---

## 실행 방법

다음 명령어를 실행하여 변경된 모든 파일을 검증하세요:

```bash
# Git에서 변경된 파일 목록 가져오기
git status --short | grep -E "^(M|A|MM)" | awk '{print $NF}' | while read -r file; do
    if [ -f "$file" ]; then
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "🔍 검증 중: $file"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

        # senior-mentor.sh 훅 실행
        CLAUDE_FILE_PATH="$file" CLAUDE_TOOL_NAME="Write" .claude/hooks/senior-mentor.sh

        echo ""
    fi
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ 모든 변경 파일 검증 완료!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
```

---

## 검증 결과

### 성공 (exit 0)
- ✅ 완벽합니다! 모든 검증 통과
- ⚠️ 경고 발견 (개선 권장)

### 실패 (exit 1)
- ❌ 심각한 문제 발견 (필수 수정)
  - 보안 취약점
  - 아키텍처 위반
  - 중요 컨벤션 위반

---

## 주의사항

1. **커밋 전 필수 실행**: 모든 커밋 전에 이 명령어를 실행하세요
2. **에러 수정**: 심각한 문제(❌)는 반드시 수정해야 합니다
3. **경고 검토**: 경고(⚠️)도 가능한 수정하는 것이 좋습니다
4. **전체 파일 검증**: 동일 클래스 내 모든 코드가 검증됩니다

---

## 예시

```bash
# /code-check 실행 시
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 검증 중: src/main/java/com/example/service/MovieService.java
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🎓 시니어 멘토: 코드 리뷰를 시작합니다...
🔒 [보안 검증] 민감 정보 노출 여부 확인 중...
🏗️ [코드 컨벤션] Java 코딩 규칙 검증 중...
⚠️ System.out.println 대신 Logger를 사용하세요
🏗️ [Spring Boot] 프레임워크 베스트 프랙티스 검증 중...
🏗️ [코드 품질] 일반 품질 기준 검증 중...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🎓 시니어 멘토 리뷰 결과:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ 경고: 1개
🎓 시니어 멘토: 경고 사항을 검토해주세요.
```

---

## 자동화

이 커맨드를 pre-commit 훅으로 등록하면 자동으로 검증됩니다:

```bash
# .git/hooks/pre-commit에 추가
#!/bin/bash
/code-check
```

---

## 참고

- 시니어 멘토 스크립트: `.claude/hooks/senior-mentor.sh`
- 리팩토링 규칙: `/code-refac`
