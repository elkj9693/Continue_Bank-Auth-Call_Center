# Continue Bank (위탁사) - 기술 문서

## 📋 목차
- [서비스 개요](#🎯-서비스-개요)
- [데이터베이스 구조](#🗄-데이터베이스-구조)
- [개인정보 처리 흐름](#🔄-개인정보-처리-흐름)
- [API 명세서](#📡-api-엔드포인트)
- [주요 기능](#🔑-주요-기능)
- [보안 구현](#🔐-보안-구현)
- [환경 설정](#📝-환경-변수)

---

## 🎯 서비스 개요

Continue Bank는 SSAP 본인인증을 활용한 디지털 뱅킹 서비스의 위탁사입니다.

### 핵심 역할
- 사용자 회원가입 및 인증 관리
- 금융 서비스 제공 (계좌 개설, 잔액 관리 등)
- 금융 컴플라이언스 준수 (약관 동의 관리 및 감사 로그)
- SSAP 및 TM 센터와의 실시간 데이터 연동

### 서비스 포트
- **백엔드 (Backend)**: 8085
- **프론트엔드 (Frontend)**: 5175

---

## 🗄 데이터베이스 구조

### ERD (개체 관계도)

```text
┌─────────────────────────────────────┐
│            users 테이블              │
├─────────────────────────────────────┤
│ id (PK)                    BIGINT   │
│ username                   VARCHAR  │ ← 로그인 ID
│ password                   VARCHAR  │ ← 암호화된 비밀번호
│ name                       VARCHAR  │ ← AES-256 암호화 성명
│ phone_number               VARCHAR  │ ← AES-256 암호화 연락처
│ ci                         VARCHAR  │ ← 연계정보 (Connecting Info)
│ created_at                 DATETIME │
│                                     │
│ ─── 약관 동의 상태 (9개 필수) ───    │
│ age_agreed                 BOOLEAN  │ ← 만 14세 이상 확인
│ terms_agreed               BOOLEAN  │ ← 서비스 이용약관 동의
│ privacy_agreed             BOOLEAN  │ ← 개인정보 수집·이용 동의
│ unique_id_agreed           BOOLEAN  │ ← 고유식별정보 처리 동의
│ credit_info_agreed         BOOLEAN  │ ← 신용정보 조회·제공 동의
│ carrier_auth_agreed        BOOLEAN  │ ← 통신사 본인확인 이용 동의
│ vpass_provision_agreed     BOOLEAN  │ ← 본인인증 데이터 제공 동의
│ electronic_finance_agreed  BOOLEAN  │ ← 전자금융거래 기본약관
│ monitoring_agreed          BOOLEAN  │ ← 금융거래 모니터링/AML 동의
│                                     │
│ ─── 약관 동의 상태 (선택 항목) ───   │
│ ssap_provision_agreed      BOOLEAN  │ ← 제휴 TM 센터 정보 제공
│ third_party_provision_agreed BOOLEAN  │ ← 제3자 정보 제공 동의
│ marketing_personal_agreed  BOOLEAN  │ ← 개인맞춤형 상품 추천
│ marketing_agreed           BOOLEAN  │ ← 마케팅 혜택 알림 전체
│                                     │
│ marketing_sms_agreed       BOOLEAN  │ ← SMS 광고 수신 동의
│ agreed_at                  DATETIME │ ← 약찰 동의 발생 시각
└─────────────────────────────────────┘
                │
                │ 1:N (일대다)
                ▼
┌─────────────────────────────────────┐
│          accounts 테이블             │
├─────────────────────────────────────┤
│ id (PK)                    BIGINT   │
│ user_id (FK)               BIGINT   │
│ account_number             VARCHAR  │ ← 고유 계좌 번호
│ balance                    DECIMAL  │ ← 현재 잔액
│ account_type               VARCHAR  │ ← 계좌 종류
│ status                     VARCHAR  │ ← 상태 (ACTIVE, LOST 등)
│ created_at                 DATETIME │
└─────────────────────────────────────┘
```

### 주요 테이블 상세
#### 1. `users` 테이블
사용자의 기본 정보와 세분화된 약관 동의 내역을 관리합니다.
- **`ci`**: 수탁사(SSAP)에서 생성한 고유 식별값이며, 개인정보 노출 없는 중복 가입 방지 키로 활용됩니다.
- **동의 필드**: 2026년 규정에 따라 필수(9종)와 선택(4종 이상) 항목이 물리적으로 분리되어 관리됩니다.

#### 2. `accounts` 테이블
사용자의 금융 계좌 원장을 보관합니다.
- **`status`**: 보이스피싱 대응 등을 위한 활성(ACTIVE), 정지(SUSPENDED), 분실(LOST) 상태값을 가집니다.

---

## 🔄 개인정보 처리 흐름

### 1. 회원가입 워크플로우
1.  **동의**: 사용자가 필수 9개 및 선택 약관에 동의합니다.
2.  **인증 초기화**: 이름/번호 입력 후 [인증하기] 클릭 시 SSAP로 세션 요청이 전달됩니다.
3.  **인증 수행**: SSAP 웹 사이트로 리다이렉트되어 OTP 인증을 완료합니다.
4.  **정보 전달**: 인증 완료 후 `tokenId`를 매개로 백엔드 간(S2S) 통신하여 검증된 정보를 위탁사가 수신합니다.
5.  **가입 확정**: CI 기반 중복 체크 후 개인정보를 암호화하여 DB에 최종 저장합니다.

### 2. 계좌 개설 워크플로우 (재인증)
1.  **로그인**: 인증된 사용자가 대시보드에서 계좌 개설을 시도합니다.
2.  **추가 인증**: 금융 사고 방지를 위해 SSAP를 통한 추가 본인 확인 절차를 거칩니다.
3.  **발급**: 인증 성공 시에만 고유 계좌번호가 생성되고 원장에 등록됩니다.

---

## 📡 API 명세서

### [사용자 인증 API]

#### 1. 회원가입 (`POST /api/v1/auth/register`)
- **요청 본문 (JSON)**: 성명, 아이디, 비밀번호, 연락처, `tokenId`, 약관 동의 맵
- **응답**: 성공 시 회원 정보 반환

#### 2. 로그인 (`POST /api/v1/auth/login`)
- **요청 본문 (JSON)**: 아이디, 비밀번호
- **응답**: 세션 정보 포함한 성공 메시지

#### 3. 아이디/비밀번호 찾기
- **`GET /api/v1/auth/find-id`**: 성명, 번호 기반 아이디 조회 (마스킹 처리)
- **`POST /api/v1/auth/reset-password`**: 본인 인증 완료 후 새 비밀번호 설정

### [계좌 관리 API]

#### 1. 계좌 개설 (`POST /api/v1/accounts/create`)
- **요청 본문 (JSON)**: 사용자 ID, 계좌 종류(SAVINGS 등)

#### 2. 잔액 조회 (`GET /api/v1/accounts/user/{userId}`)
- **응답**: 해당 사용자가 보유한 모든 계좌 목록 및 상세 정보

---

## 🔑 주요 컴플라이언스 기능

### 1. 상시 동의 관리 (My Page)
- 사용자는 마이페이지에서 선택적 동의 항목(마케팅, 제3자 제공 등)을 언제든 철회하거나 다시 동의할 수 있습니다.
- 모든 상태 변경은 감사 로그에 초 단위로 기록됩니다.

### 2. 스타벅스 이벤트 연동 (On-Demand 동의)
- 특정 배너 클릭 시 발생하는 혜택 안내를 통해, 가입 당시 미동의했던 '제휴 제공' 동의를 실시간으로 획득하고 DB를 동기화합니다.

### 3. 암호화 보관
- 성명과 휴대폰 번호는 모든 저장소 단계에서 **AES-256-CBC** 방식으로 암호화되어 관리자도 원문을 직접 볼 수 없습니다.

---

## 🔐 보안 구현 명세

### 1. 서버 측 보안
- **Spring Security**: 모든 엔드포인트에 대한 접근 제어.
- **CORS 설정**: 신뢰할 수 있는 도메인(`localhost:5175, 5176, 5173`)의 요청만 허용.

### 2. 클라이언트 측 보안
- **프록시 필터**: 브라우저에서 SSAP API로 직접 접근을 차단하고 Vite 프록시를 통해 요청을 중계합니다.

---

## 📝 환경 설정 파일

### 백엔드 설정 (`application.properties`)
```properties
# 데이터베이스 접속 정보
spring.datasource.url=jdbc:mysql://localhost:3306/entrusting_db
# 암호화 키 설정 (환경 변수 권장)
encryption.key=${ENCRYPTION_KEY}
```

### 프론트엔드 설정 (`.env`)
```env
# 통신 대상 URL
VITE_TRUSTEE_FRONTEND_URL=http://localhost:5176
VITE_API_BASE_URL=http://localhost:8085
```

---
**Continue Bank** - 금융 보안을 기술의 최전선에서 리드합니다. 🏦

