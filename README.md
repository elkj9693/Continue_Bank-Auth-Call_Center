# Continue Bank - 금융 보안 & AI 콜센터 플랫폼
> "금융의 중단 없는 흐름을 기술로 지킵니다."

Continue Bank는 **SSAP 본인인증 시스템**과 **지능형 콜센터(TM Center)**를 통합한 차세대 금융 서비스 플랫폼입니다.
위탁사(Continue Bank), 수탁사(SSAP 본인인증), 그리고 상담 수탁사(콜센터) 간의 안전한 데이터 흐름과 금융 컴플라이언스를 완벽하게 구현했습니다.

---

## 📋 목차
- [프로젝트 개요](#-프로젝트-개요)
- [시스템 아키텍처](#-시스템-아키텍처)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [시작하기](#-시작하기)
- [서비스별 상세 문서](#-서비스별-상세-문서)

---

## 🎯 프로젝트 개요
이 프로젝트는 실제 금융권 환경을 모사하여 **보안(Security)**, **인증(Identity)**, **상담(CS)** 프로세스를 유기적으로 연결했습니다.

### 핵심 가치
- **토스(Toss) 스타일 UX**: 직관적이고 미려한 금융 사용자 경험 제공
- **3-Tier 보안 아키텍처**: 콜센터-은행-인증기관 간 철저한 데이터 분리 (ARS 비밀번호 평문 저장 방지)
- **ARS 시뮬레이션**: 실제 전화망을 모사한 CLI 기반 ARS 분실 신고 시스템
- **통합 컴플라이언스**: 9개 필수 약관 자동화 및 전자서명(CI/DI) 시뮬레이션

---

## 🏗 시스템 아키텍처

```mermaid
graph TD
    User((사용자))
    ARS[("ARS Simulator\n(Use CLI)")]

    subgraph "은행 및 인증망 (Bank & Auth)"
        BankWeb[은행 웹 (Port: 5175)]
        BankWAS[은행 백엔드 (Port: 8085)]
        AuthWAS[SSAP 인증 (Port: 8086)]
        DB1[(Bank DB)]
        DB2[(Auth DB)]
    end

    subgraph "콜센터망 (Call Center)"
        CallWeb[상담원 웹 (Port: 5178)]
        CallWAS[콜센터 백엔드 (Port: 8082)]
        Security[보안 게이트웨이]
    end

    User --> BankWeb
    User -->|전화 연결| ARS
    ARS -->|암호화 전송| CallWAS
    CallWAS -->|PIN 검증 요청| BankWAS
    BankWeb -->|인증 요청| AuthWAS
    BankWAS --> DB1
    AuthWAS --> DB2
    CallWAS -->|상담 이력| BankWAS
```

---

## ✨ 주요 기능

### 1. 위탁사 (Continue Bank)
- **뱅킹 서비스**: 계좌 개설, 카드 발급, 거래 내역 조회
- **아웃바운드 Lead 생성**: 마케팅 동의 고객을 대상으로 상담 리드 자동 생성
- **보안 설정**: RSA 키 교환 및 본인확인검증 서비스 연동

### 2. 수탁사 (SSAP 본인인증)
- **모의 인증 시스템**: 통신사 가입자 DB 시뮬레이션
- **규제 준수**: CI(연계정보) 및 DI(중복가입확인정보) 생성 알고리즘 구현
- **OTP 검증**: 3분 TTL(Time-To-Live)을 가진 휘발성 보안 세션

### 3. 상담 수탁사 (Call Center & ARS)
- **ARS 카드 분실 신고**: 
    - CLI 기반 시뮬레이터 (`run-ars.bat`) 제공
    - **ANI(발신자 식별)** 및 **E2E 암호화(OAEP)** 적용
    - 이미 분실된 카드 중복 신고 방지 로직
- **아웃바운드 상담 시스템**:
    - 은행에서 전달받은 Lead 기반 상담 진행
    - 상담 결과(성공/거절 등) 실시간 은행 DB 동기화
- **Audit Log**: 모든 상담 및 ARS 접근 이력 감사 로그 기록

---

## 🛠 기술 스택

### Backend
- **Core**: Java 21, Spring Boot 3.3
- **Database**: MySQL 8.0, JPA/Hibernate
- **Security**: Spring Security, RSA/AES-256 Encryption, JWT

### Frontend
- **Framework**: React 18, Vite
- **UI/UX**: Toss-style Design System (Custom CSS), Lucide Icons
- **State**: Context API

### Infrastructure & Tools
- **Build**: Gradle (CallCenter), Maven (Bank/Auth)
- **Deployment**: Docker Compose
- **Simulation**: Java CLI (ARS Simulator)

---

## 📁 프로젝트 구조

```text
root/
├── entrusting-client/    # [은행] Continue Bank (Back/Front)
├── trustee-provider/     # [인증] SSAP Authentication (Back/Front)
├── trustee-callcenter/   # [콜센터] TM Center & ARS (Back/Front)
├── docs/                 # 기술 문서 (DB 스키마, 아키텍처 등)
├── infra/                # 인프라 설정 (Nginx, Keys)
├── start-all.bat         # 원클릭 전체 실행 스크립트
├── run-ars.bat           # ARS 시뮬레이터 실행 스크립트
└── docker-compose.yml    # 데이터베이스(3-Node) 구성
```

---

## 🚀 시작하기

### 1. 데이터베이스 준비
```batch
start-all.bat
# 실행 시 자동으로 Docker DB 컨테이너가 구동됩니다.
```
*(수동 실행: `docker-compose up -d`)*

### 2. 서비스 접속 정보
| 서비스 | URL / Command | 역할 |
|--------|---------------|------|
| **은행 (고객용)** | http://localhost:5175 | 회원가입, 카드조회 |
| **인증 (팝업용)** | http://localhost:5176 | 본인인증 시뮬레이션 |
| **콜센터 (상담원)** | http://localhost:5178 | 아웃바운드 업무 |
| **은행 백엔드** | http://localhost:8085 | 메인 API 서버 |
| **콜센터 백엔드** | http://localhost:8082 | ARS 및 상담 API |

### 3. ARS 시뮬레이션 실행 (New!)
서버가 모두 켜진 상태에서 별도 터미널(CMD)을 열고 실행하세요.
```batch
run-ars.bat
```
- **테스트 번호**: `010-9999-1111` (ARS테스터1)
- **비밀번호**: `1234`

---

## 📚 서비스별 상세 문서
*   [📂 위탁사 (Bank) 상세 가이드](./entrusting-client/README.md)
*   [📂 수탁사 (Auth) 상세 가이드](./trustee-provider/README.md)
*   [📂 콜센터 (CallCenter) 상세 가이드](./trustee-callcenter/README.md)
*   [🗄️ 데이터베이스 스키마 (DB Schema)](./docs/DB_SCHEMA.md)
*   [🔑 테스트 계정 목록 (Test Accounts)](./TEST_ACCOUNTS.md)

---

## 📄 라이선스 및 기여
- **Copyright**: 2026 Continue Bank Security Team.
- **Design**: Toss Design Principles (Reference)

---
**Continue Bank** - 기술로 금융의 신뢰를 만듭니다. 🏦
