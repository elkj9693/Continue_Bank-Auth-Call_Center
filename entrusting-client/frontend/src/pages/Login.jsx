import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import AlertModal from '../components/AlertModal';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState(''); 
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalContent, setModalContent] = useState({ title: '', message: '' });
  
  const navigate = useNavigate();

  // ... (handleLogin function remains same)
  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });
      if (response.ok) {
        const data = await response.json();
        sessionStorage.setItem('logged_in_user', username);
        sessionStorage.setItem('user_profile', JSON.stringify({
          name: data.name,
          phoneNumber: data.phoneNumber
        }));
        sessionStorage.setItem('is_first_login_check', 'true');
        setMessage('로그인 성공'); 
        navigate('/dashboard');
      } else {
        const errorText = await response.text();
        setModalContent({ title: '로그인 실패', message: errorText || '아이디 또는 비밀번호를 확인해 주세요.' });
        setIsModalOpen(true);
      }
    } catch (error) {
      setModalContent({ title: '오류 발생', message: error.message });
      setIsModalOpen(true);
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Header */}
      <header className="flex items-center h-20 px-6">
        <Logo />
      </header>

      {/* ... (Rest of component) */}

      {/* Brand Philosophy */}
      <div className="px-8 pt-3 pb-6 max-w-[480px] mx-auto w-full">
        <div className="text-center animate-fade-in">
          <p className="text-[#1A73E8] font-bold text-[13px] mb-2.5 opacity-90">
            당신의 금융은 멈추지 않도록, 보안은 계속됩니다.
          </p>
          <p className="text-[#1A73E8] font-extrabold text-[15px] mb-2">
            "금융의 중단 없는 흐름을 기술로 지킵니다."
          </p>
          <p className="text-gray-500 font-medium text-[14px] leading-relaxed">
            보안 전문가의 DNA로 완성한 <strong className="text-[#1A73E8]">전문가들의 은행</strong><br/>
            그렇기에 우리의 보안은 종료가 아닌 지속(Continue)입니다.<br/><br/><br/><br/><br/>
          </p>
        </div>
      </div>

      {/* Content */}
      <main className="flex-1 px-8 pt-4 pb-6 flex flex-col justify-start max-w-[480px] mx-auto w-full">
        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-11">
          반갑습니다! 👋<br />
          로그인을 진행해 주세요.
        </h1>

        <form id="login-form" onSubmit={handleLogin} className="space-y-8">
          <div>
            <label className="input-label">아이디</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="아이디를 입력하세요"
              className="input-field"
              required
            />
          </div>

          <div>
            <label className="input-label">비밀번호</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              className="input-field"
              required
            />
          </div>
        </form>

        <div className="mt-12 flex items-center justify-between text-[16px] font-bold">
          <Link to="/signup" className="text-[#1A73E8] hover:underline px-1">회원가입</Link>
          <div className="flex items-center gap-4 text-gray-400">
            <Link to="/find-id" className="hover:text-gray-900 transition-colors">아이디 찾기</Link>
            <div className="w-[1.5px] h-3.5 bg-gray-200"></div>
            <Link to="/forgot-password" size="sm" className="hover:text-gray-900 transition-colors">비밀번호 찾기</Link>
          </div>
        </div>
      </main>

      {/* Bottom Button */}
      <div className="px-8 pb-12 max-w-[480px] mx-auto w-full">
        <button
          type="submit"
          form="login-form"
          className="btn-primary"
        >
          로그인
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

export default Login;
