import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Logo from '../components/Logo';
import { ChevronLeft, ShieldCheck, CheckCircle2, Wallet } from 'lucide-react';
import AlertModal from '../components/AlertModal';

const CreateAccount = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [step, setStep] = useState(1);
    const [accountName, setAccountName] = useState('Continue 입출금 통장');
    const [pin, setPin] = useState('');
    const [message, setMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [bonusApplied, setBonusApplied] = useState(false); // 기본값 false (응답에 따라 가변)
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalContent, setModalContent] = useState({ title: '', message: '' });

    const isVerified = searchParams.get('verified') === 'true';
    const tokenId = searchParams.get('tokenId');
    const username = sessionStorage.getItem('logged_in_user');

    useEffect(() => {
        if (isVerified) {
            setStep(2);
        }
    }, [isVerified]);

    const handleAuthVerification = async () => {
        try {
            // [수정] 세션에 저장된 실제 사용자 정보(프로필)를 최우선으로 가져옴
            const profileData = sessionStorage.getItem('user_profile');
            const registerData = sessionStorage.getItem('register_form_data');

            let realName = '회원';
            let realPhone = 'account-open';

            if (profileData) {
                const parsed = JSON.parse(profileData);
                if (parsed.name) realName = parsed.name;
                if (parsed.phoneNumber) realPhone = parsed.phoneNumber.replace(/\D/g, '');
            } else if (registerData) {
                const parsed = JSON.parse(registerData);
                if (parsed.name) realName = parsed.name;
                if (parsed.phoneNumber) realPhone = parsed.phoneNumber.replace(/\D/g, '');
            }

            const initResponse = await fetch('/trustee-api/v1/auth/init', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ clientData: realPhone, name: realName }),
            });

            const contentType = initResponse.headers.get("content-type");
            let initData = {};
            if (contentType && contentType.includes("application/json")) {
                initData = await initResponse.json();
            } else {
                const errorText = await initResponse.text();
                throw new Error(`서버 응답 오류: ${errorText.substring(0, 100)}`);
            }

            if (initResponse.ok && initData.tokenId) {
                const currentHostname = window.location.hostname;
                const trusteeAuthPageUrl = new URL(`${import.meta.env.VITE_TRUSTEE_FRONTEND_URL}/verify`);
                trusteeAuthPageUrl.searchParams.append('tokenId', initData.tokenId);

                // [Fixed] Use the resolved variables 'realName' and 'realPhone' directly
                if (realName) trusteeAuthPageUrl.searchParams.append('name', realName);
                if (realPhone) trusteeAuthPageUrl.searchParams.append('phoneNumber', realPhone);

                const redirectUrl = new URL(`${window.location.origin}/create-account`);
                redirectUrl.searchParams.append('verified', 'true');
                trusteeAuthPageUrl.searchParams.append('redirectUrl', redirectUrl.toString());

                window.location.href = trusteeAuthPageUrl.toString();
            } else {
                setModalContent({ title: '본인인증 실패', message: initData.message || '인증 정보를 다시 확인해 주세요.' });
                setIsModalOpen(true);
            }
        } catch (error) {
            setModalContent({ title: '오류 발생', message: error.message });
            setIsModalOpen(true);
        }
    };

    const handleFinalizeCreation = async () => {
        if (!username) {
            navigate('/login');
            return;
        }

        setIsLoading(true);
        console.log('[DEBUG] Finalizing Account Creation:', { username, accountName, pinLen: pin?.length });
        try {
            const response = await fetch('/api/v1/accounts/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, accountName, pin, tokenId }),
            });

            // [수정] 응답 본문을 단 한 번만 읽도록 보장하는 가장 확실한 방법
            const rawBody = await response.text();
            let parsedData = null;
            try {
                if (rawBody) parsedData = JSON.parse(rawBody);
            } catch (e) {
                // Not JSON
            }

            if (response.ok) {
                // 백엔드에서 전달한 축하금 지급 여부 저장
                if (parsedData && parsedData.bonusApplied !== undefined) {
                    setBonusApplied(parsedData.bonusApplied);
                }
                setStep(3);
            } else {
                const errorMsg = parsedData?.message || rawBody || '알 수 없는 오류';
                setModalContent({ title: '계좌 생성 실패', message: errorMsg });
                setIsModalOpen(true);
            }
        } catch (error) {
            setModalContent({ title: '오류 발생', message: error.message });
            setIsModalOpen(true);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-white">
            <header className="flex items-center h-20 px-6">
                <button onClick={() => navigate('/dashboard')} className="p-2 -ml-2 rounded-full hover:bg-gray-50">
                    <ChevronLeft size={28} className="text-gray-700" />
                </button>
                <div className="flex-1 flex justify-center -ml-10">
                    <Logo />
                </div>
            </header>

            <main className="flex-1 px-8 py-10 flex flex-col max-w-[480px] mx-auto w-full">
                {step === 1 && (
                    <div className="flex flex-col flex-1">
                        <div className="bg-blue-50 w-16 h-16 rounded-2xl flex items-center justify-center mb-8">
                            <ShieldCheck size={32} className="text-[#1A73E8]" />
                        </div>
                        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-4">
                            계좌를 개설하기 위해<br />본인인증을 진행합니다.
                        </h1>
                        <p className="text-gray-500 font-medium leading-relaxed mb-12">
                            안전한 금융 거래를 위해<br />수탁사 본인확인이 필요해요.
                        </p>
                        <div className="mt-auto pb-10">
                            <button onClick={handleAuthVerification} className="btn-primary">
                                본인인증 하기
                            </button>
                        </div>
                    </div>
                )}

                {step === 2 && (
                    <div className="flex flex-col flex-1">
                        <div className="bg-emerald-50 w-16 h-16 rounded-2xl flex items-center justify-center mb-8 text-emerald-600">
                            <CheckCircle2 size={32} />
                        </div>
                        <h1 className="text-[32px] font-semibold text-gray-900 leading-tight tracking-tight mb-4">
                            본인인증 완료!<br />계좌 이름을 정해주세요.
                        </h1>
                        <div className="mt-8 space-y-6 flex-1">
                            <div>
                                <label className="input-label">계좌명</label>
                                <input
                                    type="text"
                                    value={accountName}
                                    onChange={(e) => setAccountName(e.target.value)}
                                    className="input-field"
                                    placeholder="계좌 이름을 입력하세요"
                                />
                            </div>
                            <div>
                                <label className="input-label">계좌 비밀번호 (4자리)</label>
                                <input
                                    type="password"
                                    maxLength={4}
                                    value={pin}
                                    onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
                                    className="input-field tracking-widest text-lg font-bold"
                                    placeholder="숫자 4자리 입력"
                                />
                            </div>
                        </div>
                        <div className="mt-auto pb-10">
                            <button
                                onClick={handleFinalizeCreation}
                                disabled={isLoading || !accountName || pin.length !== 4}
                                className="btn-primary"
                            >
                                {isLoading ? '개설 중...' : '계좌 개설 완료'}
                            </button>
                        </div>
                    </div>
                )}

                {step === 3 && (
                    <div className="flex flex-col flex-1 items-center justify-center text-center">
                        <div className="w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center mb-8 animate-bounce">
                            <CheckCircle2 size={48} className="text-[#1A73E8]" />
                        </div>
                        <h1 className="text-[28px] font-bold text-gray-900 mb-4">
                            계좌 개설을<br />축하드립니다! 🎉
                        </h1>
                        <p className="text-gray-500 font-medium mb-12">
                            {bonusApplied ? (
                                <>가입 축하금 <span className="text-[#1A73E8] font-bold">10,000원</span>이 입금되었습니다.</>
                            ) : (
                                <>계좌 개설이 완료되었습니다.</>
                            )}
                            <br />
                            이제 Continue의 모든 서비스를 이용해 보세요.
                        </p>
                        <button onClick={() => navigate('/dashboard')} className="btn-primary w-full">
                            확인
                        </button>
                    </div>
                )}
            </main>

            <AlertModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalContent.title}
                message={modalContent.message}
            />
        </div>
    );
};

export default CreateAccount;
