import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Loader2, CheckCircle2, AlertCircle } from 'lucide-react';

const AuthCallback = () => {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState('loading'); // 'loading', 'success', 'error'
  const [message, setMessage] = useState('본인인증 결과를 확인하고 있습니다');
  const navigate = useNavigate();

  useEffect(() => {
    const tokenId = searchParams.get('tokenId');
    const phoneNumber = searchParams.get('phoneNumber');

    if (tokenId) {
      const cleanPhone = phoneNumber?.replace(/\D/g, '') || '';
      fetch('/api/v1/auth/callback', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ tokenId, phoneNumber: cleanPhone }),
      })
        .then(async (response) => {
          console.log('[CALLBACK-DEBUG] Status:', response.status);
          const rawText = await response.text();
          console.log('[CALLBACK-DEBUG] Raw Body:', rawText);

          let isSuccess = false;
          let serverMessage = '';
          let responseData = null;

          try {
            responseData = JSON.parse(rawText);
            serverMessage = responseData.message || rawText;
            if (response.ok && (responseData.status === 'success' || (responseData.message && responseData.message.toLowerCase().includes('success')))) {
              isSuccess = true;
            }
          } catch (e) {
            // JSON 파シング 실패 시 텍스트 기반 판별
            serverMessage = rawText;
            if (response.ok && rawText.toLowerCase().includes('success')) {
              isSuccess = true;
            }
          }

          if (isSuccess) {
            setStatus('success');
            setMessage('본인인증을 완료했습니다');
            const isRegistering = !!sessionStorage.getItem('register_form_data');
            if (isRegistering) {
              const nameValue = (responseData && responseData.name) ? responseData.name : '인증완료';
              const nameParam = `&name=${encodeURIComponent(nameValue)}`;
              setTimeout(() => navigate(`/register?verified=true&phoneNumber=${phoneNumber}&tokenId=${tokenId}${nameParam}`), 1200);
            } else {
              setTimeout(() => navigate('/dashboard'), 1200);
            }
          } else {
            console.error('[CALLBACK-DEBUG] Failed to verify:', serverMessage);
            setStatus('error');
            setMessage(`검증 실패: ${serverMessage || '정보가 불일치합니다'}`);
          }
        })
        .catch((error) => {
          console.error('[CALLBACK-DEBUG] CRITICAL Network Error:', {
            message: error.message,
            stack: error.stack,
            type: error.name
          });
          setStatus('error');
          setMessage('네트워크 연결 확인이 필요합니다');
        });
    } else {
      setStatus('error');
      setMessage('잘못된 접근입니다');
    }
  }, [searchParams, navigate]);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-8 bg-white">
      <div className="mb-10">
        {status === 'loading' && (
          <Loader2 size={64} className="text-[#1A73E8] animate-spin" />
        )}
        {status === 'success' && (
          <div className="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center animate-in zoom-in duration-500">
            <CheckCircle2 size={48} className="text-[#1A73E8]" />
          </div>
        )}
        {status === 'error' && (
          <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center animate-in zoom-in duration-500">
            <AlertCircle size={48} className="text-red-500" />
          </div>
        )}
      </div>

      <h1 className="text-2xl font-bold text-gray-900 text-center mb-3">
        {message}
      </h1>
      <p className="text-[17px] text-gray-500 text-center leading-relaxed">
        {status === 'loading' ? '잠시만 기다려주세요' :
          status === 'success' ? '잠시 후 서비스로 이동합니다' :
            '정보 검증 중 오류가 발생했습니다. 다시 입력해 주세요.'}
      </p>

      {status === 'error' && (
        <div className="mt-12 w-full space-y-4">
          {sessionStorage.getItem('register_form_data') ? (
            <button
              onClick={() => navigate('/register')}
              className="btn-primary"
            >
              회원가입으로 돌아가기
            </button>
          ) : (
            <button
              onClick={() => navigate('/dashboard')}
              className="btn-primary"
            >
              대시보드로 돌아가기
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default AuthCallback;
