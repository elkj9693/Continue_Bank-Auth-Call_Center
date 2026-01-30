import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Logo from '../components/Logo';
import { ShieldCheck, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';

/**
 * 전용 브릿지 페이지 (Toss Style)
 * alert() 대신 이 페이지를 통해 상태를 보여주고 리다이렉트합니다.
 */
const AuthBridge = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [status, setStatus] = useState('loading'); // loading, error, success
    const [title, setTitle] = useState('정보 확인 중');
    const [message, setMessage] = useState('잠시만 기다려 주세요.');
    const [targetUrl, setTargetUrl] = useState('/login');

    useEffect(() => {
        const type = searchParams.get('type'); // error, success
        const msg = searchParams.get('message');
        const next = searchParams.get('next') || '/login';
        const t = searchParams.get('title');

        if (type === 'error') {
            setStatus('error');
            setTitle(t || '확인 실패');
            setMessage(msg || '요청 처리에 실패했습니다.');
            setTargetUrl(next);
        } else if (type === 'success') {
            setStatus('success');
            setTitle(t || '확인 완료');
            setMessage(msg || '성공적으로 처리되었습니다.');
            setTargetUrl(next);
        }

        // 2초 후 자동 이동
        const timer = setTimeout(() => {
            navigate(next);
        }, 2500);

        return () => clearTimeout(timer);
    }, [searchParams, navigate]);

    return (
        <div className="flex flex-col min-h-screen bg-white">
            <header className="flex items-center h-20 px-6">
                <Logo />
            </header>

            <main className="flex-1 px-8 py-20 flex flex-col items-center justify-center max-w-[480px] mx-auto w-full text-center">
                <div className="mb-10">
                    {status === 'loading' && (
                        <div className="w-20 h-20 bg-blue-50 rounded-[32px] flex items-center justify-center animate-pulse">
                            <Loader2 size={40} className="text-[#1A73E8] animate-spin" />
                        </div>
                    )}
                    {status === 'error' && (
                        <div className="w-20 h-20 bg-red-50 rounded-[32px] flex items-center justify-center animate-bounce">
                            <AlertCircle size={40} className="text-red-500" />
                        </div>
                    )}
                    {status === 'success' && (
                        <div className="w-20 h-20 bg-emerald-50 rounded-[32px] flex items-center justify-center animate-in zoom-in duration-500">
                            <CheckCircle2 size={40} className="text-emerald-500" />
                        </div>
                    )}
                </div>

                <h1 className="text-[28px] font-extrabold text-gray-900 leading-tight tracking-tight mb-4">
                    {title}
                </h1>
                
                <p className="text-gray-500 font-bold text-[16px] leading-relaxed mb-12">
                    {message}<br />
                    <span className="text-[14px] font-medium text-gray-400">잠시 후 화면이 자동으로 이동합니다.</span>
                </p>

                <div className="bg-gray-50 p-4 rounded-2xl flex items-center gap-3 border border-gray-100/50">
                    <ShieldCheck size={18} className="text-gray-400" />
                    <span className="text-[13px] text-gray-400 font-bold">안전한 Continue 보안 시스템</span>
                </div>
            </main>

            <footer className="p-10 flex justify-center">
                <div className="w-1.5 h-1.5 bg-gray-100 rounded-full mx-1 animate-ping"></div>
                <div className="w-1.5 h-1.5 bg-gray-100 rounded-full mx-1 animate-ping delay-150"></div>
                <div className="w-1.5 h-1.5 bg-gray-100 rounded-full mx-1 animate-ping delay-300"></div>
            </footer>
        </div>
    );
};

export default AuthBridge;
