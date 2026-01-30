import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import Logo from '../components/Logo';
import { ChevronLeft, UserCheck } from 'lucide-react';

const FindId = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [step, setStep] = useState(1);
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [foundId, setFoundId] = useState('');
    const [message, setMessage] = useState('');

    useEffect(() => {
        const verified = searchParams.get('verified');
        const phoneFromUrl = searchParams.get('phoneNumber');
        const nameFromUrl = searchParams.get('name');

        if (verified === 'true' && phoneFromUrl) {
            setStep(2);
            // 실제 백엔드에서 아이디 조회 (이름까지 함께 확인)
            fetch(`/api/v1/auth/find-id?phoneNumber=${phoneFromUrl}&name=${encodeURIComponent(nameFromUrl || '')}`)
                .then(res => {
                    if (!res.ok) throw new Error('가입되지 않은 정보입니다.');
                    return res.text();
                })
                .then(id => {
                    // 아이디 앞글자 일부 마스킹 처리 (예: admin -> ad***)
                    const masked = id.length > 2 ? id.slice(0, 2) + '*'.repeat(id.length - 2) : id;
                    setFoundId(masked);
                })
                .catch(err => {
                    setMessage(err.message);
                    // [UX] 가입되지 않은 정보일 경우 브릿지 페이지로 이동
                    const errorMsg = encodeURIComponent(err.message || '정보를 찾을 수 없습니다.');
                    navigate(`/auth/bridge?type=error&message=${errorMsg}&next=/login&title=회원정보 없음`);
                });
        }
    }, [searchParams]);

    const formatPhoneNumber = (val) => {
        const numbers = val.replace(/\D/g, '');
        if (numbers.length <= 3) return numbers;
        if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
        return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
    };

    const handleAuthVerification = async () => {
        if (!name || !phoneNumber) {
            setMessage('이름과 휴대폰 번호를 모두 입력해 주세요.');
            return;
        }

        try {
            const cleanPhoneNumber = phoneNumber.replace(/\D/g, '');
            // [보성] 위탁사 프론트엔드 프록시(/trustee-api) 이용
            const initResponse = await fetch('/trustee-api/v1/auth/init', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ clientData: cleanPhoneNumber, name: name }),
            });

            const rawText = await initResponse.text();
            let initData = {};
            try {
                if (rawText) initData = JSON.parse(rawText);
            } catch (e) {
                console.error('Invalid JSON:', rawText);
            }

            if (initResponse.ok && initData.tokenId) {
                const currentHostname = window.location.hostname;
                const trusteeAuthPageUrl = new URL(`${import.meta.env.VITE_TRUSTEE_FRONTEND_URL}/verify`);
                trusteeAuthPageUrl.searchParams.append('tokenId', initData.tokenId);
                trusteeAuthPageUrl.searchParams.append('name', name);
                trusteeAuthPageUrl.searchParams.append('phoneNumber', cleanPhoneNumber);

                // 현재 접속 도메인(origin)을 리다이렉트 주소로 사용
                const redirectUrl = new URL(`${window.location.origin}/find-id`);
                redirectUrl.searchParams.append('verified', 'true');

                trusteeAuthPageUrl.searchParams.append('redirectUrl', redirectUrl.toString());

                window.location.href = trusteeAuthPageUrl.toString();
            } else {
                setMessage('본인인증 초기화 실패');
            }
        } catch (error) {
            setMessage('오류 발생: ' + error.message);
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-white">
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
            <main className="flex-1 px-8 py-12 flex flex-col max-w-[480px] mx-auto w-full">
                {step === 1 ? (
                    <div className="flex flex-col flex-1">
                        <h1 className="text-[34px] font-[900] text-gray-900 leading-[1.15] tracking-tighter mb-12">
                            아이디를<br />
                            잊으셨나요?
                        </h1>

                        <div className="space-y-8 flex-1">
                            <div>
                                <label className="input-label">이름</label>
                                <input
                                    type="text"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="성명을 입력하세요"
                                    className="input-field"
                                />
                            </div>
                            <div>
                                <label className="input-label">휴대폰 번호</label>
                                <input
                                    type="tel"
                                    value={phoneNumber}
                                    onChange={(e) => setPhoneNumber(formatPhoneNumber(e.target.value))}
                                    placeholder="인증받을 번호 입력"
                                    className="input-field"
                                />
                            </div>
                            <p className="text-[16px] text-gray-500 font-bold leading-relaxed px-1">
                                안전한 정보 확인을 위해<br />
                                본인확인 과정이 필요합니다.
                            </p>
                        </div>

                        {/* [COMPLIANCE] SSAP 데이터 전송 고지 */}
                        <div className="mt-8 p-4 bg-amber-50/50 border border-amber-200 rounded-2xl">
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

                        <div className="py-12">
                                <button
                                    onClick={handleAuthVerification}
                                    disabled={!name || phoneNumber.replace(/\D/g, '').length < 11}
                                    className="btn-primary"
                                >
                                    본인인증 하기
                                </button>
                        </div>
                    </div>
                ) : (
                    <div className="flex flex-col flex-1">
                        <div className="flex-1 flex flex-col items-center justify-center -mt-16">
                            <div className="w-28 h-28 bg-emerald-50 rounded-full flex items-center justify-center mb-10 shadow-sm border border-emerald-100/50 animate-in zoom-in duration-700">
                                <UserCheck size={56} className="text-emerald-500" />
                            </div>
                            <h1 className="text-[30px] font-[900] text-gray-900 mb-4 tracking-tighter">아이디를 찾았습니다!</h1>
                            <p className="text-gray-500 mb-12 font-bold opacity-80 text-center">고객님의 가입 정보와 일치하는<br />아이디입니다.</p>

                            <div className="w-full bg-gray-50 p-10 rounded-lg border border-gray-100 text-center shadow-inner">
                                <span className="text-[36px] font-[1000] text-[#1A1A1A] tracking-widest">{foundId}</span>
                            </div>
                        </div>

                        <div className="py-12 space-y-5">
                            <button
                                onClick={() => navigate('/login')}
                                className="btn-primary"
                            >
                                로그인 하기
                            </button>
                            <Link
                                to="/forgot-password"
                                className="btn-secondary flex items-center justify-center"
                            >
                                비밀번호 찾기
                            </Link>
                        </div>
                    </div>
                )}

                {message && (
                    <div className="mt-8 p-5 bg-red-50/50 border border-red-100 rounded-[22px] text-red-500 text-sm font-black text-center">
                        ⚠️ {message}
                    </div>
                )}
            </main>
        </div>
    );
};

export default FindId;
