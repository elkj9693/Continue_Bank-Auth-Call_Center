# 프론트엔드 (React + Vite) - 유플러스 본인인증(SSAP)
> "안전하고 신속한 본인 확인 인터페이스"

이 프로젝트는 Vite를 기반으로 구축된 SSAP(본인인증 수탁사)의 사용자용 인증 페이지입니다.

## 🛠 주요 기술 스택
- **프레임워크**: React 18
- **빌드 도구**: Vite
- **라우팅**: React Router v6
- **상태 관리**: React Hooks
- **아이콘**: Lucide React

## 🚀 시작하기
1. **패키지 설치**: `npm install`
2. **개발 서버 실행**: `npm run dev` (기본 포트: 5176)
3. **빌드**: `npm run build`

## 📁 주요 구조
- `src/pages/IdentityVerification`: 본인인증 단계별 화면 (정보 입력 -> OTP 확인)
- `src/components`: 인증 전용 UI 요소
- `src/services`: 백엔드 S2S 통신 및 API 호출 로직

---
**SSAP Identity Provider** - 더 안전한 연결을 위한 시작점. 🔐
