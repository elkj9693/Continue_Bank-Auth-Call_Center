import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import Logo from '../components/Logo';

const OtpInput = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [otp, setOtp] = useState(Array(6).fill(''));
  const [message, setMessage] = useState('');
  const [timer, setTimer] = useState(180);
  const [isResendDisabled, setIsResendDisabled] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const inputRefs = useRef([]);

  const tokenId = searchParams.get('tokenId');
  const phoneNumber = searchParams.get('phoneNumber') || '010-****-****';
  const redirectUrl = searchParams.get('redirectUrl');

  useEffect(() => {
    const countdown = setInterval(() => {
      setTimer((prev) => {
        if (prev <= 1) {
          clearInterval(countdown);
          setIsResendDisabled(false);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    if (inputRefs.current[0]) {
      inputRefs.current[0].focus();
    }

    return () => clearInterval(countdown);
  }, []);

  const handleOtpChange = (e, index) => {
    const value = e.target.value;
    if (/[^0-9]/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    if (value && index < 5) {
      inputRefs.current[index + 1].focus();
    }
  };

  const handlePaste = (e) => {
    const pasteData = e.clipboardData.getData('text/plain').slice(0, 6);
    if (/[^0-9]/.test(pasteData)) return;

    const newOtp = pasteData.split('').concat(Array(6 - pasteData.length).fill(''));
    setOtp(newOtp);
    if (inputRefs.current[Math.min(pasteData.length, 5)]) {
      inputRefs.current[Math.min(pasteData.length, 5)].focus();
    }
    e.preventDefault();
  };

  const handleKeyDown = (e, index) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1].focus();
    }
  };

  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  const isOtpComplete = otp.every((digit) => digit !== '');

  const handleConfirm = async () => {
    if (!isOtpComplete) {
      setMessage('인증번호 6자리를 모두 입력해주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetch('/api/v1/auth/confirm', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tokenId: tokenId, otp: otp.join('') }),
      });

      if (response.ok) {
        if (redirectUrl) {
          const callbackUrl = new URL(redirectUrl);
          callbackUrl.searchParams.append('tokenId', tokenId);
          callbackUrl.searchParams.append('phoneNumber', phoneNumber.replace(/\D/g, ''));
          window.location.href = callbackUrl.toString();
        } else {
          setMessage('본인인증 완료!');
        }
      } else {
        const errorData = await response.text();
        setMessage('인증 실패: ' + errorData);
      }
    } catch (error) {
      setMessage('오류 발생: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResendOtp = () => {
    setMessage('🛡️ 인증번호가 재전송되었습니다.');
    setTimer(180);
    setIsResendDisabled(true);
  };

  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Header */}
      <header className="flex items-center h-20 px-6">
        <button onClick={() => navigate(-1)} className="p-2 -ml-2 rounded-full hover:bg-gray-50 transition-colors">
          <ChevronLeft size={28} className="text-gray-700" />
        </button>
        <div className="flex-1 flex justify-center -ml-10">
          <Logo />
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 px-8 py-12 max-w-[480px] mx-auto w-full">
        <h1 className="text-[28px] font-black text-gray-900 leading-tight mb-3">
          인증번호를<br />입력해 주세요
        </h1>
        <p className="text-[16px] text-gray-500 font-bold mb-12">
          <span className="text-[#E50914]">{phoneNumber}</span>로 발송되었습니다
        </p>

        {/* OTP Input */}
        <div className="flex justify-between gap-2 mb-10">
          {otp.map((digit, index) => (
            <input
              key={index}
              ref={(el) => (inputRefs.current[index] = el)}
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              onChange={(e) => handleOtpChange(e, index)}
              onKeyDown={(e) => handleKeyDown(e, index)}
              onPaste={handlePaste}
              className="otp-input !w-[50px] !h-[64px]"
            />
          ))}
        </div>

        {/* Footer Actions */}
        <div className="flex flex-col items-center space-y-6">
          <div className="px-4 py-2 bg-red-50 rounded-full">
            {timer > 0 ? (
              <span className="timer-text font-black text-lg">{formatTime(timer)}</span>
            ) : (
              <span className="text-red-500 font-bold">인증 시간이 만료되었습니다</span>
            )}
          </div>

          <button
            type="button"
            onClick={handleResendOtp}
            disabled={isResendDisabled}
            className="text-[15px] font-bold text-gray-400 hover:text-[#E50914] transition-colors"
          >
            인증번호 다시 받기
          </button>
        </div>

        {/* Error Message */}
        {message && (
          <div className="mt-8 p-4 bg-red-50 border border-red-100 rounded-2xl text-center">
            <p className="text-sm font-bold text-[#E50914]">{message}</p>
          </div>
        )}
      </main>

      {/* Bottom Button */}
      <div className="px-8 pb-12 max-w-[480px] mx-auto w-full">
        <button
          onClick={handleConfirm}
          disabled={!isOtpComplete || isSubmitting}
          className="btn-primary !rounded-2xl"
        >
          {isSubmitting ? '보안 인증 확인 중...' : '인증 완료'}
        </button>
      </div>
    </div>
  );
};

export default OtpInput;
