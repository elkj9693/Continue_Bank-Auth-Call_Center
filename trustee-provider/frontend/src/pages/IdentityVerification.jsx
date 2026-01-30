import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, CheckCircle2, AlertTriangle, ShieldX, Lock } from 'lucide-react';
import Logo from '../components/Logo';

const IdentityVerification = () => {
  const navigate = useNavigate();
  const [isValidAccess, setIsValidAccess] = useState(true); // [SECURITY] 접근 유효성 상태 추가
  const [name, setName] = useState('');
  const [residentFront, setResidentFront] = useState('');
  const [telecom, setTelecom] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otp, setOtp] = useState('');
  const [timer, setTimer] = useState(180);
  const [message, setMessage] = useState('');
  const [tokenId, setTokenId] = useState(null);
  const [isDataLocked, setIsDataLocked] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [agreed, setAgreed] = useState(false); // [COMPLIANCE] SSAP 이용 동의 추가

  useEffect(() => {
    const query = new URLSearchParams(window.location.search);
    const urlTokenId = query.get('tokenId');
    const urlName = query.get('name');
    const urlPhone = query.get('phoneNumber');

    // [SECURITY] TokenId가 없으면 직접 접근으로 간주하여 차단
    if (!urlTokenId) {
      setIsValidAccess(false);
      return;
    }

    setTokenId(urlTokenId);

    if (urlName || urlPhone) {
      if (urlName) setName(urlName);
      if (urlPhone) setPhoneNumber(formatPhoneNumber(urlPhone));
      setIsDataLocked(true);
    }
  }, []);

  // 휴대폰 번호 포맷팅 함수
  const formatPhoneNumber = (val) => {
    const numbers = val.replace(/\D/g, '');
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
  };

  // 모든 필드가 채워졌는지 확인 (버튼 활성화 조건)
  const isFormValid = name.trim().length > 0 &&
    residentFront.length === 6 &&
    telecom !== "" &&
    phoneNumber.replace(/\D/g, '').length >= 10 &&
    agreed; // [COMPLIANCE] 동의 시에만 버튼 활성화

  const timerRef = useRef(null);

  useEffect(() => {
    if (otpSent && timer > 0) {
      timerRef.current = setInterval(() => {
        setTimer(prev => prev - 1);
      }, 1000);
    } else {
      clearInterval(timerRef.current);
    }
    return () => clearInterval(timerRef.current);
  }, [otpSent, timer]);

  const handleRequestOtp = async () => {
    if (!name || !residentFront || !telecom || !phoneNumber) {
      setMessage('모든 정보를 입력해 주세요.');
      return;
    }

    if (!agreed) {
      setMessage('⚠️ 본인인증 이용약관에 동의해 주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const cleanPhoneNumber = phoneNumber.replace(/\D/g, '');
      // [보안 강화] 신규 세션을 생성하지 않고, 전달받은 TokenId로 정보 일치 여부 확인
      const response = await fetch('/api/v1/auth/request-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tokenId: tokenId,
          name: name,
          phoneNumber: cleanPhoneNumber,
          residentFront: residentFront,
          carrier: telecom
        }),
      });

      const rawBody = await response.text();
      let data = {};
      try {
        if (rawBody) data = JSON.parse(rawBody);
      } catch (e) {
        data = { message: rawBody };
      }

      if (response.ok) {
        // ... (성공 부분 유지)
        if (data.otp) {
          setOtp(data.otp); 
          setMessage('✅ 인증번호가 발송되었습니다. (테스트용 노출: ' + data.otp + ')');
        } else {
          setMessage('✅ 인증번호가 SMS로 발송되었습니다.');
        }
        setOtpSent(true);
        setTimer(180);
      } else {
        // [디버깅] 상세 에러 정보 출력
        const debugInfo = `Status: ${response.status} ${response.statusText}, Body: ${rawBody.substring(0, 100)}`;
        console.error('[DEBUG-API] Error Detail:', debugInfo);
        setMessage(`❌ [오류] ${data.message || '요청 실패'} (${response.status})`);
      }
    } catch (error) {
      setMessage('⚠️ 오류 발생: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleVerifyOtp = async () => {
    if (otp.length !== 6) {
      setMessage('인증번호 6자리를 입력하세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      console.log('[DEBUG] Verification Request:', { tokenId, otp });
      const response = await fetch('/api/v1/auth/confirm', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tokenId: tokenId, otp: otp }),
      });

      if (response.ok) {
        const query = new URLSearchParams(window.location.search);
        const redirectUrl = query.get('redirectUrl');

        if (redirectUrl) {
          const finalUrl = new URL(redirectUrl);
          finalUrl.searchParams.set('tokenId', tokenId || '');
          finalUrl.searchParams.set('phoneNumber', phoneNumber); // 하이픈 포함된 상태로 전달
          finalUrl.searchParams.set('name', name);
          finalUrl.searchParams.set('verified', 'true'); // [핵심] 인증 완료 파라미터 추가
          window.location.href = finalUrl.toString();
        } else {
          setMessage('본인인증이 성공적으로 완료되었습니다.');
        }
      } else {
        const errorMsg = await response.text();
        let parsedError = errorMsg;
        try {
           const jsonError = JSON.parse(errorMsg);
           parsedError = jsonError.message || errorMsg;
        } catch(e) {}
        setMessage(`❌ ${parsedError}`);
      }
    } catch (error) {
      setMessage('⚠️ 네트워크 오류: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  if (!isValidAccess) {
    return (
      <div className="flex flex-col min-h-screen bg-white items-center justify-center px-8 text-center">
        <div className="w-20 h-20 bg-red-50 rounded-3xl flex items-center justify-center mb-8">
          <ShieldX size={48} className="text-[#E50914]" />
        </div>
        <h2 className="text-2xl font-black text-gray-900 mb-4 tracking-tight">
          비정상적인 접근입니다
        </h2>
        <p className="text-gray-500 font-medium leading-relaxed mb-10">
          보안 정책에 따라 직접 주소를 입력한 접근은<br />
          엄격히 차단됩니다.<br />
          <span className="text-red-500 font-bold">위탁사 서비스를 통해 정상적으로 진입</span>해 주세요.
        </p>
        <div className="w-full max-w-[280px] p-4 bg-gray-50 rounded-2xl border border-gray-100 mb-10">
          <div className="flex items-center gap-2 justify-center text-xs font-bold text-gray-400 uppercase tracking-widest">
            <AlertTriangle size={14} />
            Access Denied
          </div>
          <p className="text-[10px] text-gray-300 mt-2">Security ID: UNAUTHORIZED_DIRECT_ACCESS</p>
        </div>
        <button 
          onClick={() => window.close()}
          className="btn-primary !rounded-2xl !bg-gray-900"
        >
          확인
        </button>
      </div>
    );
  }

  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Header */}
      <header className="flex items-center h-20 px-6">
        <button
          onClick={() => {
            const query = new URLSearchParams(window.location.search);
            const redirectUrl = query.get('redirectUrl');
            if (redirectUrl) {
              try {
                const url = new URL(redirectUrl);
                // 위탁사의 메인(로그인) 페이지 등으로 복귀
                window.location.href = url.origin + '/login';
              } catch (e) {
                navigate(-1);
              }
            } else {
              navigate(-1);
            }
          }}
          className="p-2 -ml-2 rounded-full hover:bg-gray-50 transition-colors"
        >
          <ChevronLeft size={28} className="text-gray-700" />
        </button>
        <div className="flex-1 flex justify-center -ml-10">
          <Logo />
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 px-8 py-12 max-w-[480px] mx-auto w-full">
        <h1 className="text-[32px] font-black text-gray-900 leading-[1.15] tracking-tight mb-10">
          강력한 보안,<br />
          <span className="text-[#E50914]">SSAP</span> 로<br />
          인증을 완료하세요.
        </h1>

        <div className="space-y-8">
          {/* 이름 */}
          <div>
            <label className="input-label">성명</label>
            <div className="relative">
              <input
                inputMode="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="성명을 입력하세요"
                className={`input-field !rounded-xl transition-all ${isDataLocked ? 'bg-gray-100 border-gray-200 text-gray-500 font-bold cursor-not-allowed pr-12' : 'focus:ring-2 focus:ring-red-100'}`}
                readOnly={isDataLocked}
              />
              {isDataLocked && (
                <div className="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-2">
                  <div className="w-[1px] h-4 bg-gray-300 mr-2"></div>
                  <Lock size={18} className="text-gray-400" />
                </div>
              )}
            </div>
          </div>

          {/* 주민등록번호 */}
          <div>
            <label className="input-label">주민등록번호 앞 6자리</label>
            <input
              type="text"
              inputMode="numeric"
              maxLength={6}
              value={residentFront}
              onChange={(e) => setResidentFront(e.target.value.replace(/\D/g, ''))}
              placeholder="생년월일 6자리"
              className="input-field !rounded-lg"
            />
          </div>

          {/* 통신사 */}
          <div>
            <label className="input-label">통신사 선택</label>
            <select
              value={telecom}
              onChange={(e) => setTelecom(e.target.value)}
              className="select-field !rounded-lg"
            >
              <option value="" disabled hidden>통신사를 선택하세요</option>
              <option value="SKT">SKT</option>
              <option value="KT">KT</option>
              <option value="LGU+">LG U+</option>
              <option value="ALDDLE">알뜰폰</option>
            </select>
          </div>

          {/* 휴대폰 번호 */}
          <div>
            <label className="input-label">휴대폰 번호</label>
            <div className="flex gap-3">
              <div className="relative flex-1">
                <input
                  type="tel"
                  inputMode="numeric"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(formatPhoneNumber(e.target.value))}
                  placeholder="인증받을 번호 입력"
                  className={`input-field !rounded-xl w-full transition-all ${isDataLocked ? 'bg-gray-100 border-gray-200 text-gray-500 font-bold cursor-not-allowed pr-12' : 'focus:ring-2 focus:ring-red-100'}`}
                  readOnly={isDataLocked}
                  disabled={otpSent}
                />
                {isDataLocked && (
                  <div className="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-2">
                    <div className="w-[1px] h-4 bg-gray-300 mr-2"></div>
                    <Lock size={18} className="text-gray-400" />
                  </div>
                )}
              </div>
              <button
                type="button"
                onClick={handleRequestOtp}
                className={`btn-action whitespace-nowrap self-center !rounded-lg transition-all ${!agreed ? 'opacity-50 cursor-not-allowed bg-gray-300 pointer-events-none' : ''}`}
                disabled={(otpSent && timer > 150) || !agreed}
              >
                {otpSent ? '재발송' : '인증번호발송'}
              </button>
            </div>
          </div>
 
          {/* [COMPLIANCE] SSAP 통합 약관 동의 */}
          <div className="pt-4 border-t border-gray-100">
            <div 
              className="flex items-start gap-3 p-4 bg-gray-50 rounded-xl cursor-pointer active:opacity-70 transition-all"
              onClick={() => setAgreed(!agreed)}
            >
              <div className={`mt-0.5 w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 transition-all ${agreed ? 'bg-[#E50914] border-transparent' : 'bg-white border-gray-200'}`}>
                {agreed && <CheckCircle2 size={14} className="text-white" />}
              </div>
              <div>
                <p className={`text-[13px] font-bold leading-tight ${agreed ? 'text-gray-900' : 'text-gray-400'}`}>
                  본인인증 이용약관 및 개인정보 제3자 제공에 동의합니다. (필수)
                </p>
                <div className="mt-2 space-y-1">
                   <p className="text-[11px] text-gray-400 line-clamp-1">SSAP 서비스 이용약관, 고유식별정보 처리, 통신사 이용약관 동의 등</p>
                </div>
              </div>
            </div>
          </div>
 
          {/* OTP 입력란 */}
          {otpSent && (
            <div className="space-y-5 animate-in fade-in slide-in-from-top-2 duration-300">
              {/* 테스트 가이드 박스 (개발용) */}
              <div className="p-5 bg-amber-50 border border-amber-100 rounded-2xl">
                <p className="text-amber-800 text-sm font-medium leading-relaxed">
                  💡 <span className="font-bold underline">인증번호 안내</span>: 현재 테스트 모드입니다.<br />
                  입력하실 번호는 <span className="text-red-600 font-extrabold ml-1">{otp}</span> 입니다.
                </p>
              </div>

              <div>
                <label className="input-label">인증번호 6자리</label>
                <div className="relative">
                  <input
                    type="text"
                    inputMode="numeric"
                    maxLength={6}
                    value={otp}
                    onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                    placeholder="인증번호 6자리 입력"
                    className="input-field pr-16 !rounded-lg"
                  />
                  <span className="absolute right-4 top-1/2 -translate-y-1/2 timer-text">
                    {formatTime(timer)}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Feedback Message */}
        {message && (
          <div className={`mt-8 px-6 py-4 rounded-2xl flex items-center gap-3 animate-in fade-in zoom-in-95 duration-300 ${
            message.includes('성공') || message.includes('발송') 
              ? 'bg-red-50 text-[#E50914] border border-red-100' 
              : 'bg-red-50 text-red-500 border border-red-100'
          }`}>
            <span className="text-xl">{message.includes('성공') || message.includes('발송') ? '🛡️' : '⚠️'}</span>
            <p className="text-sm font-bold leading-tight">{message}</p>
          </div>
        )}
      </main>

      {/* Primary Action */}
      <div className="px-8 pb-12 max-w-[480px] mx-auto w-full">
        <button
          onClick={otpSent ? handleVerifyOtp : handleRequestOtp}
          disabled={isSubmitting || (otpSent ? otp.length !== 6 : !isFormValid)}
          className="btn-primary !rounded-2xl"
        >
          {isSubmitting ? (otpSent ? '보안 인증 중...' : '인증번호 가공 중...') : (otpSent ? '본인 확인 완료' : 'SSAP 인증번호 받기')}
        </button>
      </div>
    </div>
  );
};

export default IdentityVerification;
