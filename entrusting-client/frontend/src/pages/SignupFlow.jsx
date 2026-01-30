import React from 'react';
import { useNavigate } from 'react-router-dom';
import TermsAgreement from '../components/TermsAgreement';

/**
 * 회원가입 전체 플로우 관리
 * [변경] 중간 브릿지 페이지(단계 안내) 제거하고 약관 동의 후 바로 가입 폼으로 이동
 */
const SignupFlow = () => {
  const navigate = useNavigate();

  const handleTermsComplete = (data) => {
    // 1. 약관 동의 데이터 세션 저장
    sessionStorage.setItem('terms_agreement', JSON.stringify(data));
    
    // 2. [Direct] 중간 페이지 없이 바로 회원가입(본인인증+정보입력) 페이지로 이동
    navigate('/register');
  };

  // 오직 약관 동의 컴포넌트만 렌더링
  return <TermsAgreement onComplete={handleTermsComplete} />;
};

export default SignupFlow;