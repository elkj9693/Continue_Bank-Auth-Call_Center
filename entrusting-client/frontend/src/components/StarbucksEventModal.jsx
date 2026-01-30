import React, { useState } from 'react';
import { X, Gift, Check, Coffee, ChevronRight, CheckCircle2, AlertCircle } from 'lucide-react';
import { QRCodeSVG } from 'qrcode.react';

const StarbucksEventModal = ({ isOpen, onClose }) => {
    const [step, setStep] = useState(1); // 1: Offer, 2: Consent, 3: Coupon
    const [consents, setConsents] = useState({
        essential: false,
        optional: false,
    });
    const [showAlert, setShowAlert] = useState('');

    if (!isOpen) return null;

    const handleConsentChange = (type) => {
        setConsents(prev => ({ ...prev, [type]: !prev[type] }));
        setShowAlert(''); // Clear error on change
    };

    const handleConsentSubmit = () => {
        if (!consents.essential) {
            setShowAlert('필수 약관에 동의해 주세요.');
            return;
        }
        if (!consents.optional) {
            setShowAlert('선택 항목(마케팅 활용)에 동의하지 않으시면 이벤트 참여가 불가능합니다.');
            return;
        }

        const username = sessionStorage.getItem('logged_in_user');
        const userProfile = sessionStorage.getItem('user_profile');

        if (username && userProfile) {
            const parsedProfile = JSON.parse(userProfile);

            // 1. Compliance Logging (Existing)
            fetch('/api/v1/compliance/marketing-consent', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: username,
                    productName: 'Continue 카드',
                    consentType: '3RD_PARTY_TM'
                })
            });

            // 2. Create Lead for Outbound Call Center (New)
            fetch('/api/v1/leads', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    customerRef: username,
                    name: parsedProfile.name,
                    phone: parsedProfile.phoneNumber,
                    productType: 'Continue 카드'
                })
            })
                .then(response => {
                    if (response.ok) {
                        localStorage.setItem(`marketing_agreed_${username}`, 'true');
                        setConsents({ essential: false, optional: false });
                        setStep(3);
                    } else {
                        setShowAlert('동의 처리 중 오류가 발생했습니다. 다시 시도해 주세요.');
                    }
                })
                .catch(error => {
                    console.error('Lead Creation Error:', error);
                    setShowAlert('서버 통신 오류가 발생했습니다.');
                });
        } else {
            // Fallback
            setStep(3);
        }
    };

    const handleClose = () => {
        setStep(1);
        setConsents({ essential: false, optional: false });
        setShowAlert('');
        onClose();
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-black/60 backdrop-blur-sm animate-fade-in" onClick={handleClose}></div>

            <div className={`relative bg-white w-full max-w-sm rounded-[32px] overflow-hidden shadow-2xl animate-in zoom-in-95 duration-300 ${step === 3 ? 'bg-[#1e3932]' : ''}`}>
                {/* Close Button */}
                <button
                    onClick={handleClose}
                    className="absolute top-4 right-4 p-2 bg-black/5 hover:bg-black/10 rounded-full transition-colors z-10"
                >
                    <X size={20} className={step === 3 ? "text-white/50 hover:text-white" : "text-gray-500"} />
                </button>

                {step === 1 && (
                    <div className="flex flex-col h-full">
                        {/* Hero Image Area */}
                        <div className="h-48 bg-emerald-600 relative overflow-hidden flex items-center justify-center">
                            <div className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=1000&auto=format&fit=crop')] bg-cover bg-center opacity-40 mix-blend-overlay"></div>
                            <div className="relative z-10 text-center text-white p-6">
                                <div className="w-14 h-14 bg-white/20 backdrop-blur-md rounded-2xl flex items-center justify-center mx-auto mb-3 border border-white/30 shadow-lg animate-bounce-subtle">
                                    <Gift size={28} className="text-white drop-shadow-md" />
                                </div>
                                <h2 className="text-xl font-[900] tracking-tight mb-1 drop-shadow-md leading-tight">
                                    신용카드 연회비<br /><span className="text-yellow-300">100% 무료</span> 혜택!
                                </h2>
                            </div>
                        </div>

                        {/* Content */}
                        <div className="p-8 space-y-6">
                            <div className="text-center space-y-2">
                                <p className="text-gray-900 font-bold text-lg leading-snug">
                                    지금 신청하면<br />
                                    <span className="text-emerald-600 text-xl font-black">스타벅스 아메리카노</span><br />
                                    100% 즉시 증정! ☕️
                                </p>
                                <p className="text-gray-400 text-xs font-medium">
                                    * 신규 발급 고객 대상 (마케팅 동의 필수)
                                </p>
                            </div>

                            <button
                                onClick={() => setStep(2)}
                                className="w-full h-14 bg-[#1A1A1A] text-white rounded-2xl font-black text-lg hover:bg-black transition-all shadow-xl shadow-gray-200 active:scale-[0.98] flex items-center justify-center gap-2"
                            >
                                무료 쿠폰 받기 <ChevronRight size={20} />
                            </button>
                        </div>
                    </div>
                )}

                {step === 2 && (
                    <div className="flex flex-col h-full bg-white">
                        <div className="p-8 pb-0">
                            <h2 className="text-2xl font-bold text-gray-900 mb-2">약관 동의</h2>
                            <p className="text-gray-500 text-sm leading-relaxed">
                                이벤트 참여 및 경품 발송을 위해<br />
                                아래 약관에 동의해 주세요.
                            </p>
                        </div>

                        <div className="p-8 space-y-5 flex-1">
                            {/* Essential Consent */}
                            <div
                                className="flex items-start gap-3 p-4 rounded-xl border border-gray-100 bg-gray-50 cursor-pointer hover:border-gray-300 transition-colors"
                                onClick={() => handleConsentChange('essential')}
                            >
                                <div className={`mt-0.5 w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 transition-colors ${consents.essential ? 'bg-emerald-500 border-transparent' : 'bg-white border-gray-300'}`}>
                                    {consents.essential && <Check size={14} className="text-white" />}
                                </div>
                                <div>
                                    <span className="text-sm font-bold text-gray-900 block mb-1">[필수] 개인정보 처리 방침</span>
                                    <p className="text-xs text-gray-400 leading-tight">이벤트 참여용 개인정보 수집 및 이용에 동의합니다.</p>
                                </div>
                            </div>

                            {/* Optional Consent */}
                            <div
                                className="flex items-start gap-3 p-4 rounded-xl border border-gray-100 bg-gray-50 cursor-pointer hover:border-gray-300 transition-colors"
                                onClick={() => handleConsentChange('optional')}
                            >
                                <div className={`mt-0.5 w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 transition-colors ${consents.optional ? 'bg-emerald-500 border-transparent' : 'bg-white border-gray-300'}`}>
                                    {consents.optional && <Check size={14} className="text-white" />}
                                </div>
                                <div>
                                    <span className="text-sm font-bold text-gray-900 block mb-1">[선택] 마케팅 정보 수신 동의</span>
                                    <p className="text-xs text-gray-400 leading-tight">혜택 정보 및 광고성 정보 수신에 동의합니다. (미동의 시 이벤트 참여 불가)</p>
                                </div>
                            </div>

                            {/* Alert Message */}
                            {showAlert && (
                                <div className="flex items-start gap-2 p-3 bg-red-50 rounded-lg text-red-500 text-xs font-bold animate-in slide-in-from-top-1">
                                    <AlertCircle size={14} className="mt-0.5 shrink-0" />
                                    {showAlert}
                                </div>
                            )}
                        </div>

                        <div className="p-8 pt-0">
                            <button
                                onClick={handleConsentSubmit}
                                className="w-full h-14 bg-[#1A1A1A] text-white rounded-2xl font-black text-lg hover:bg-black transition-all shadow-lg active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                동의하고 계속하기
                            </button>
                        </div>
                    </div>
                )}

                {step === 3 && (
                    <div className="flex flex-col h-full bg-[#1e3932] text-white">
                        <div className="p-8 flex flex-col items-center justify-center h-full text-center relative overflow-hidden">
                            {/* Confetti Effect */}
                            <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none">
                                <div className="absolute top-10 left-10 w-2 h-2 bg-yellow-400 rounded-full animate-ping"></div>
                                <div className="absolute top-20 right-20 w-3 h-3 bg-red-400 rounded-full animate-ping" style={{ animationDelay: '0.5s' }}></div>
                                <div className="absolute bottom-20 left-1/2 w-2 h-2 bg-blue-400 rounded-full animate-ping" style={{ animationDelay: '1s' }}></div>
                            </div>

                            <div className="w-16 h-16 bg-white/10 rounded-full flex items-center justify-center mb-6 animate-in zoom-in duration-500">
                                <CheckCircle2 size={32} className="text-emerald-400" />
                            </div>

                            <h2 className="text-2xl font-[900] mb-2 tracking-tight">쿠폰함에 저장되었습니다!</h2>
                            <p className="text-emerald-200 text-sm font-medium mb-8">
                                언제든 쿠폰함에서 확인하실 수 있습니다.
                            </p>

                            <div className="bg-white p-6 rounded-3xl shadow-2xl mb-8 transform hover:scale-105 transition-transform duration-300">
                                <div className="flex items-center gap-3 mb-4 border-b border-gray-100 pb-4">
                                    <div className="w-10 h-10 bg-[#00704A] rounded-full flex items-center justify-center">
                                        <Coffee size={20} className="text-white" />
                                    </div>
                                    <div className="text-left">
                                        <div className="text-[#00704A] font-black text-xs">STARBUCKS</div>
                                        <div className="text-gray-800 font-bold text-sm">아이스 부드러운 디저트 세트</div>
                                    </div>
                                </div>
                                <div className="bg-white p-2 rounded-xl border-2 border-dashed border-gray-200">
                                    <QRCodeSVG value="https://example.com/fake-coupon" size={160} fgColor="#1A1A1A" />
                                </div>
                                <div className="mt-3 text-[10px] font-mono text-gray-400 tracking-widest">
                                    REF: STB-2026-PROMO-A
                                </div>
                            </div>

                            <button
                                onClick={handleClose}
                                className="w-full h-14 bg-white text-[#00704A] rounded-2xl font-black text-lg hover:bg-emerald-50 transition-all shadow-lg active:scale-[0.98]"
                            >
                                확인 완료
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StarbucksEventModal;
