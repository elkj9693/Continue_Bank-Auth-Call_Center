# 🗄️ Database Schema

본 프로젝트는 마이크로서비스 아키텍처(MSA)를 지향하며, 각 서비스별로 독립된 데이터베이스 스키마를 가지고 있습니다.

---

## 🏦 1. entrust_db (은행 서비스)

**서비스**: `entrusting-client` (Bank Backend)
**용도**: 사용자 계정, 카드 정보, 상담 리드 관리

### `site_users` (사용자)
은행 웹사이트 회원 정보 테이블입니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| `id` | BIGINT | PK, Auto Increment | |
| `name` | VARCHAR(500) | 사용자 이름 | **AES-256 암호화** |
| `username` | VARCHAR(255) | 로그인 아이디 | |
| `password` | VARCHAR(255) | 비밀번호 (BCrypt) | |
| `phone_number` | VARCHAR(500) | 휴대폰 번호 | **AES-256 암호화** |
| `ci` | VARCHAR(120) | 연계정보(CI) | 본인인증 후 저장 |
| `di` | VARCHAR(90) | 중복가입확인정보(DI) | 본인인증 후 저장 |
| `is_verified` | BOOLEAN | 본인인증 여부 | |
| `terms_agreed` | BOOLEAN | 이용약관 동의 | |
| `privacy_agreed` | BOOLEAN | 개인정보 수집 동의 | |
| `marketing_agreed` | BOOLEAN | 마케팅 수신 동의 | |

### `cards` (카드)
발급된 신용카드 정보 테이블입니다. ARS 분실 신고 시 조회됩니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| `id` | BIGINT | PK, Auto Increment | |
| `card_ref` | VARCHAR(255) | 카드 고유 참조 ID | UUID |
| `customer_ref` | VARCHAR(255) | 소유자 참조 ID | User ID |
| `card_no` | VARCHAR(255) | 카드 번호 | |
| `pin_hash` | VARCHAR(255) | 비밀번호 해시 | SHA-256 |
| `status` | VARCHAR(255) | 상태 | `ACTIVE`, `LOST` |
| `created_at` | DATETIME | 발급 일시 | |

### `leads` (상담 리드)
마케팅 동의 고객을 대상으로 생성되는 아웃바운드 상담 대상 목록입니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| `lead_id` | VARCHAR(255) | PK, UUID | |
| `customer_ref` | VARCHAR(255) | 고객 참조 ID | User ID |
| `name` | VARCHAR(255) | 고객명 | |
| `phone` | VARCHAR(255) | 전화번호 | |
| `requested_product_type` | VARCHAR(255) | 관심 상품 | |
| `status` | VARCHAR(255) | 상담 상태 | `PENDING`, `CONTACTED`, `COMPLETED` |
| `created_at` | DATETIME | 생성 일시 | |
| `contacted_at` | DATETIME | 최근 상담 일시 | |

---

## 🛡️ 2. trustee_db (인증/수탁 서비스)

**서비스**: `trustee-provider` (Auth Backend)
**용도**: 휴대폰 본인확인, 통신사 가입자 모의 데이터

### `auth_token` (인증 세션)
본인확인 요청 시 생성되는 세션 및 결과 정보입니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| `token_id` | BINARY(16) | PK, UUID (jti) | |
| `auth_request_id` | VARCHAR(255) | 위탁사 요청 ID | |
| `client_data` | VARCHAR(500) | 전화번호 | **AES-256 암호화** |
| `name` | VARCHAR(500) | 이름 | **AES-256 암호화** |
| `carrier` | VARCHAR(255) | 통신사 | |
| `otp` | VARCHAR(100) | 인증번호 | 해시 저장 |
| `status` | VARCHAR(255) | 진행 상태 | `INIT`, `OTP_SENT`, `VERIFIED` |
| `retry_count` | INT | 재시도 횟수 | |
| `ci` | VARCHAR(120) | 생성된 CI | |
| `di` | VARCHAR(90) | 생성된 DI | |

### `carrier_user` (통신사 가입자)
실제 통신사 DB를 모방한 가입자 원장 테이블입니다.

| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| `id` | BIGINT | PK, Auto Increment | |
| `name` | VARCHAR(255) | 가입자명 | |
| `phone_number` | VARCHAR(255) | 휴대폰 번호 | Unique |
| `carrier` | VARCHAR(255) | 통신사 | `SKT`, `KT`, `LGU+` |
| `resident_front` | VARCHAR(6) | 생년월일 | 주민번호 앞자리 |

---

## 📞 3. callcenter_db (콜센터)

**서비스**: `trustee-callcenter` (Call Center Backend)
**용도**: 콜센터 상담 이력 등 (현재 버전에서는 Stateless Gateway로 동작하여 테이블 미사용)

*현재 버전에서는 은행 API(`entrusting-client`)를 직접 호출하여 데이터를 조회 및 처리하므로 자체적인 영속성 데이터는 관리하지 않습니다.*
