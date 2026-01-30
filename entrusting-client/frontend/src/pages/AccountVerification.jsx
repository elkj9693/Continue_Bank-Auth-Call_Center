import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import { ChevronLeft, ShieldCheck, AlertCircle } from 'lucide-react';
import AlertModal from '../components/AlertModal';

const AccountVerification = () => {
    const navigate = useNavigate();
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalContent, setModalContent] = useState({ title: '', message: '' });

    // 세션에 저장된 로그인 회원 정보 (Backend에서 이미 복호화된 정보를 보내줌)
    const loginUser = JSON.parse(sessionStorage.getItem('user_profile') || '{}');
    const systemName = loginUser.name || '';
    const systemPhone = (loginUser.phoneNumber || '').replace(/\D/g, '');

    const formatPhoneNumber = (val) => {
        const numbers = val.replace(/\D/g, '');
        if (numbers.length <= 3) return numbers;
        if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
        return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
    };

    const handleVerify = async () => {
        const cleanInputPhone = phoneNumber.replace(/\D/g, '');
        
        // [핵심] 수탁사로 로그인한 회원의 정보와 입력한 정보가 같은지 검증
        if (name !== systemName || cleanInputPhone !== systemPhone) {
            const errorMsg = encodeURIComponent('입력하신 정보가 회원 정보와 일치하지 않습니다. 본인 명의의 정보만 입력 가능합니다.');
            navigate(`/auth/bridge?type=error&message=${errorMsg}&next=/dashboard&title=본인확인 실패`);
            return;
        }

        try {
            // 정보 일치 시 V-PASS 인증 초기화
            const initResponse = await fetch('/trustee-api/v1/auth/init', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ clientData: cleanInputPhone, name: name }),
            });

            const initData = await initResponse.json();

            if (initResponse.ok && initData.tokenId) {
                const trusteeAuthPageUrl = new URL(`${import.meta.env.VITE_TRUSTEE_FRONTEND_URL}/verify`);
                trusteeAuthPageUrl.searchParams.append('tokenId', initData.tokenId);
                trusteeAuthPageUrl.searchParams.append('name', name);
                trusteeAuthPageUrl.searchParams.append('phoneNumber', cleanInputPhone);

                const redirectUrl = new URL(`${window.location.origin}/create-account`);
                redirectUrl.searchParams.append('verified', 'true');
                trusteeAuthPageUrl.searchParams.append('redirectUrl', redirectUrl.toString());

                window.location.href = trusteeAuthPageUrl.toString();
            } else {
                setModalContent({ title: '오류', message: initData.message || '인증 초기화 중 오류가 발생했습니다.' });
                setIsModalOpen(true);
            }
        } catch (error) {
            setModalContent({ title: '오류 발생', message: '서버와 통신 중 문제가 발생했습니다.' });
            setIsModalOpen(true);
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
                <div className="bg-blue-50 w-16 h-16 rounded-2xl flex items-center justify-center mb-8">
                    <ShieldCheck size={32} className="text-[#1A73E8]" />
                </div>
                
                <h1 className="text-[28px] font-bold text-gray-900 leading-tight tracking-tight mb-4">
                    계좌 개설을 위해<br />본인 명의 정보를 입력해 주세요.
                </h1>
                
                <p className="text-gray-500 font-medium leading-relaxed mb-10">
                    원활한 금융 거래를 위해 가입 시 등록한<br />
                    이름과 휴대폰 번호를 확인합니다.
                </p>

                <div className="space-y-6 flex-1">
                    <div className="relative">
                        <label className="input-label">이름</label>
                        <div className="relative">
                            <input
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="실명을 입력하세요"
                                className="input-field"
                            />
                        </div>
                    </div>

                    <div className="relative">
                        <label className="input-label">휴대폰 번호</label>
                        <div className="relative">
                            <input
                                type="tel"
                                value={phoneNumber}
                                onChange={(e) => setPhoneNumber(formatPhoneNumber(e.target.value))}
                                placeholder="생년월일 입력"
                                className="input-field"
                            />
                        </div>
                    </div>

                    <div className="mt-8 p-5 bg-amber-50 rounded-2xl border border-amber-100 flex items-start gap-3">
                        <AlertCircle className="text-amber-500 shrink-0 mt-0.5" size={18} />
                        <p className="text-[13px] text-amber-900 font-medium leading-relaxed">
                            입력하신 정보가 회원 가입 시 정보와 다를 경우 본인 확인이 제한될 수 있습니다.
                        </p>
                    </div>
                </div>

                <div className="mt-auto pb-10">
                    <button 
                        onClick={handleVerify} 
                        disabled={!name || phoneNumber.length < 10}
                        className="btn-primary"
                    >
                        정보 확인 및 인증하기
                    </button>
                </div>
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

export default AccountVerification;
