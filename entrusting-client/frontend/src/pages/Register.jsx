import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import Logo from '../components/Logo';
import { ChevronLeft, CheckCircle2 } from 'lucide-react';
import AlertModal from '../components/AlertModal';

const Register = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // URL에서 인증 여부를 확인
  const isVerified = searchParams.get('verified') === 'true';
  const urlPhoneNumber = searchParams.get('phoneNumber') || '';
  const urlName = searchParams.get('name') || '';

  const [name, setName] = useState(urlName);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [phoneNumber, setPhoneNumber] = useState(urlPhoneNumber);
  const [message, setMessage] = useState('');
  const [showSuccess, setShowSuccess] = useState(false); // 가입 성공 오버레이 상태
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalContent, setModalContent] = useState({ title: '', message: '' });

  const tokenId = searchParams.get('tokenId') || '';

  // 컴포넌트 마운트 시 저장된 데이터 복구
  React.useEffect(() => {
    const savedData = sessionStorage.getItem('register_form_data');
    if (savedData) {
      const parsed = JSON.parse(savedData);
      setName(parsed.name || '');
      setUsername(parsed.username || '');
      // 보안상 비밀번호는 복구하지 않거나, 편의를 위해 선택적 복구 (여기서는 편의를 위해 복구)
      setPassword(parsed.password || '');
      setConfirmPassword(parsed.confirmPassword || '');
      if (!urlPhoneNumber) setPhoneNumber(parsed.phoneNumber || '');
    }
  }, [urlPhoneNumber]);

  // 입력값이 변경될 때마다 자동 저장 (디바운싱 없이 간단하게 구현)
  React.useEffect(() => {
    const formData = { name, username, password, confirmPassword, phoneNumber };
    sessionStorage.setItem('register_form_data', JSON.stringify(formData));
  }, [name, username, password, confirmPassword, phoneNumber]);

  // 가입 완료 후 데이터 삭제 함수
  const clearSavedData = () => {
    sessionStorage.removeItem('register_form_data');
  };

  // 휴대폰 번호 포맷팅 함수
  const formatPhoneNumber = (val) => {
    const numbers = val.replace(/\D/g, '');
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
  };

  // Basic Validation Logic
  const isIdValid = username.length === 0 || (username.length >= 6 && username.length <= 12 && /^[a-zA-Z0-9]+$/.test(username));
  const isPasswordValid = password.length === 0 || (/^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])/.test(password));
  const isPasswordMatch = password === confirmPassword;

  const isFormValid = name &&
    username.length >= 6 &&
    isIdValid &&
    password.length > 0 &&
    isPasswordValid &&
    isPasswordMatch &&
    phoneNumber.length >= 10 &&
    isVerified; // 본인인증 필수 조건 추가

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!isPasswordMatch) {
      setMessage('비밀번호가 일치하지 않습니다.');
      return;
    }
    try {
      // 약관 동의 정보 가져오기
      const termsAgreement = JSON.parse(sessionStorage.getItem('terms_agreement') || '{}');
      
      const cleanPhoneNumber = phoneNumber.replace(/\D/g, '');
      const response = await fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          name, 
          username, 
          password, 
          phoneNumber: cleanPhoneNumber, 
          tokenId,
          termsAgreement: {
            ...termsAgreement,
            agreements: {
              ...termsAgreement.agreements,
              carrierAuth: true     // 본인인증 완료 시 자동 동의 처리
            }
          }
        }),
      });
      const data = await response.text();
      if (response.ok) {
        clearSavedData(); // 가입 성공 시 데이터 삭제
        setShowSuccess(true); // 축하 오버레이 표시
        setTimeout(() => navigate('/login'), 2500); // 2.5초 후 이동
      } else {
        setModalContent({ 
          title: '가입 실패', 
          message: data || '회원가입 처리 중 오류가 발생했습니다.' 
        });
        setIsModalOpen(true);
      }
    } catch (error) {
      setModalContent({ 
        title: '오류 발생', 
        message: error.message 
      });
      setIsModalOpen(true);
    }
  };

  const handleAuthVerification = async () => {
    if (!phoneNumber) {
      setMessage('본인인증을 위해 휴대폰 번호를 입력해 주세요.');
      return;
    }

    try {
      const cleanName = name.trim();
      const cleanPhoneNumber = phoneNumber.replace(/\D/g, '');

      if (!cleanName || !cleanPhoneNumber) {
        setMessage('이름과 휴대폰 번호를 모두 입력해 주세요.');
        return;
      }

      const initResponse = await fetch('/trustee-api/v1/auth/init', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ clientData: cleanPhoneNumber, name: cleanName }),
      });
      const rawText = await initResponse.text();
      let initData = {};
      try {
        if (rawText) initData = JSON.parse(rawText);
      } catch (e) {
        throw new Error(`서버 응답 오류 (JSON 아님): ${rawText.substring(0, 50)}`);
      }

      if (initResponse.ok && initData.tokenId) {
        // 수탁사 프론트엔드로 이동 (환경 변수 우선 사용, 없으면 현재 Host 기반 추론)
        const trusteeFrontendBase = import.meta.env.VITE_TRUSTEE_FRONTEND_URL || `${window.location.protocol}//${window.location.hostname}:5176`;
        const trusteeAuthPageUrl = new URL(`${trusteeFrontendBase}/verify`);
        
        trusteeAuthPageUrl.searchParams.append('tokenId', initData.tokenId);
        trusteeAuthPageUrl.searchParams.append('phoneNumber', cleanPhoneNumber);
        trusteeAuthPageUrl.searchParams.append('name', cleanName);
        // 인증 완료 후 다시 위탁사 콜백 페이지로 복귀
        trusteeAuthPageUrl.searchParams.append('redirectUrl', `${window.location.origin}/auth/callback`);

        const targetUrl = trusteeAuthPageUrl.toString();
        console.log('[DEBUG] Redirecting to Trustee:', targetUrl);

        window.location.href = targetUrl;
      } else {
        // [DEBUG] 상세 에러 메시지 표시
        const errorMsg = initData.message || initData.error || '알 수 없는 서버 오류';
        setModalContent({ 
          title: `본인인증 실패 (Code: ${initResponse.status})`, 
          message: `서버 응답: ${errorMsg}\n\n(API: /trustee-api/v1/auth/init)` 
        });
        setIsModalOpen(true);
      }
    } catch (error) {
      setModalContent({ 
        title: '오류 발생', 
        message: error.message 
      });
      setIsModalOpen(true);
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Success Overlay */}
      {showSuccess && (
        <div className="fixed inset-0 z-[100] bg-white flex flex-col items-center justify-center p-8 animate-in fade-in duration-500">
          <div className="w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center mb-8 animate-bounce">
            <CheckCircle2 size={64} className="text-[#1A73E8]" />
          </div>
          <h2 className="text-3xl font-bold text-gray-900 text-center mb-4 leading-tight">
            회원가입을<br />진심으로 축하드립니다! 🎉
          </h2>
          <p className="text-gray-500 text-lg font-bold text-center">
            첫 계좌 개설 시 <span className="text-[#1A73E8]">10,000원</span> 가입 축하금이 지급됩니다.<br />
            잠시 후 로그인 페이지로 이동합니다.
          </p>
        </div>
      )}

      {/* Header */}
      <header className="flex items-center h-20 px-6">
        <button onClick={() => navigate('/login')} className="p-2 -ml-2 rounded-full hover:bg-gray-50 transition-colors">
          <ChevronLeft size={28} className="text-gray-700" />
        </button>
        <div className="flex-1 flex justify-center -ml-10">
          <Logo />
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 px-8 py-12 overflow-y-auto max-w-[480px] mx-auto w-full">
        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-10">
          회원정보를<br />
          입력해 주세요.
        </h1>

        <form id="register-form" onSubmit={handleRegister} className="space-y-8">
          {/* 이름 */}
          <div>
            <label className="input-label">이름</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="실명을 입력하세요"
              className={`input-field ${isVerified ? 'bg-gray-100/50 text-gray-400 font-bold' : ''}`}
              required
              disabled={isVerified}
            />
          </div>

          {/* 휴대폰 번호 & 본인인증 */}
          <div>
            <label className="input-label">휴대폰 번호</label>
            <div className="flex gap-3">
              <input
                type="tel"
                inputMode="numeric"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(formatPhoneNumber(e.target.value))}
                placeholder="인증받을 번호 입력"
                className={`input-field flex-1 ${isVerified ? 'bg-gray-100/50 text-gray-400 font-bold' : ''}`}
                required
                disabled={isVerified}
              />
              {isVerified ? (
                <div className="flex items-center gap-2 px-5 h-[60px] bg-emerald-50 text-emerald-600 rounded-2xl font-bold border border-emerald-100">
                  <CheckCircle2 size={20} />
                  <span>완료</span>
                </div>
              ) : (
                <button
                  type="button"
                  onClick={handleAuthVerification}
                  className="btn-action whitespace-nowrap self-center"
                >
                  본인인증
                </button>
              )}
            </div>
            
            {/* [COMPLIANCE] SSAP 데이터 전송 고지 (위치 이동됨) */}
            {!isVerified && (
              <div className="mt-3 p-4 bg-amber-50/50 border border-amber-200 rounded-2xl animate-in fade-in slide-in-from-top-2 duration-300">
                <p className="text-[12px] font-bold text-gray-700 mb-2">
                  ※ 본인인증 시 다음 정보가 SSAP로 전송됩니다:
                </p>
                <p className="text-[12px] text-gray-600 font-medium leading-relaxed ml-4">
                  이름, 휴대폰번호
                </p>
                <p className="text-[11px] text-gray-500 font-medium mt-2 leading-relaxed">
                  (본인인증 목적으로만 사용되며, 다른 용도로는 절대 사용되지 않습니다)
                </p>
              </div>
            )}
          </div>

          {/* 아이디 */}
          <div>
            <label className="input-label">아이디</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="6~12자 영문/숫자"
              className={`input-field ${!isIdValid ? 'border-red-500' : ''}`}
              required
            />
            {!isIdValid && (
              <p className="error-text">6~12자 영문/숫자 조합만 가능합니다.</p>
            )}
          </div>

          {/* 비밀번호 */}
          <div>
            <label className="input-label">비밀번호</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="영문, 숫자, 특수문자 조합"
              className={`input-field ${!isPasswordValid ? 'border-red-500' : ''}`}
              required
            />
            {!isPasswordValid && password.length > 0 && (
              <p className="error-text">영문, 숫자, 특수문자를 모두 포함해야 합니다.</p>
            )}
          </div>

          {/* 비밀번호 확인 */}
          <div>
            <label className="input-label">비밀번호 확인</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="비밀번호를 한번 더 입력하세요"
              className={`input-field ${!isPasswordMatch ? 'border-red-500' : ''}`}
              required
            />
            {!isPasswordMatch && confirmPassword.length > 0 && (
              <p className="error-text">비밀번호가 일치하지 않습니다.</p>
            )}
          </div>
        </form>

        {/* Inline Error Message (Selective) */}
        {message && (
          <div className="mt-8 p-5 bg-red-50/50 rounded-2xl text-red-500 text-sm font-semibold text-center border border-red-100 flex items-center justify-center gap-2">
            <span>⚠️</span> {message}
          </div>
        )}
      </main>

      {/* Bottom Actions */}
      <div className="px-8 pb-12 space-y-5 max-w-[480px] mx-auto w-full">
        {!isVerified && (
          <div className="p-5 bg-blue-50/50 border border-blue-100 rounded-2xl flex items-center gap-4 animate-in fade-in slide-in-from-bottom-2 duration-500">
            <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center shadow-lg shadow-blue-100 flex-shrink-0">
              <span className="text-[#1A73E8] font-black text-xl">!</span>
            </div>
            <div>
              <p className="text-[#1A73E8] font-bold text-[16px]">본인인증이 필요합니다</p>
              <p className="text-[#1A73E8]/70 text-[13px] leading-tight">안전하게 가입하려면 본인확인이 필요해요.</p>
            </div>
          </div>
        )}

        <button
          type="submit"
          form="register-form"
          disabled={!isFormValid}
          className="btn-primary"
        >
          가입 완료
        </button>
      </div>

      <AlertModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={modalContent.title}
        message={modalContent.message}
      />
    </div>
  );
};

export default Register;
