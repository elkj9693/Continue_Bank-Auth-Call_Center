import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, User, Phone, Shield, ChevronRight, AlertCircle, CheckCircle2 } from 'lucide-react';



const MyPage = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState({ name: '', phone: '' });
    const [consentStatus, setConsentStatus] = useState({
        marketingAgreed: false,
        ssapProvisionAgreed: false,
        thirdPartyProvisionAgreed: false
    });
    const [loading, setLoading] = useState(true);
    const username = sessionStorage.getItem('logged_in_user');

    useEffect(() => {
        const fetchMyData = async () => {
            if (!username) return;
            
            // 1. Load User Profile from Session
            const sessionProfile = sessionStorage.getItem('user_profile');
            if (sessionProfile) {
                const parsed = JSON.parse(sessionProfile);
                setUserInfo({ name: parsed.name, phone: parsed.phoneNumber });
            }

            // 2. Fetch Consent Status from Backend
            try {
                const response = await fetch(`/api/v1/compliance/my-consent?username=${username}`);
                if (response.ok) {
                    const data = await response.json();
                    setConsentStatus(data);
                }
            } catch (error) {
                console.error("Failed to fetch consent status:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchMyData();
    }, [username]);

    const handleToggleConsent = async (type, currentStatus, label) => {
        const newStatus = !currentStatus;
        if (!newStatus && !window.confirm(`[${label}] 동의를 철회하시겠습니까?\n관련 혜택 안내를 받으실 수 없습니다.`)) {
            return;
        }

        try {
            const response = await fetch('/api/v1/compliance/update-consent', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: username,
                    consentType: type, // MARKETING, SSAP_PROVISION, THIRD_PARTY, PERSONAL_MARKETING
                    agreed: newStatus
                })
            });

            if (response.ok) {
                // Update local state
                let stateKey = '';
                if (type === 'MARKETING') stateKey = 'marketingAgreed';
                else if (type === 'SSAP_PROVISION') stateKey = 'ssapProvisionAgreed';
                else if (type === 'THIRD_PARTY') stateKey = 'thirdPartyProvisionAgreed';
                
                setConsentStatus(prev => ({ ...prev, [stateKey]: newStatus }));
            } else {
                alert('처리 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('Error updating consent:', error);
        }
    };

    if (loading) return <div className="min-h-screen bg-[#F5F6F8]"></div>;

    const ConsentItem = ({ title, type, checked, stateKey, description }) => (
        <div className="bg-white p-5 border-b border-gray-100 last:border-0 last:rounded-b-[24px] first:rounded-t-[24px]">
            <div className="flex justify-between items-start mb-2">
                <div>
                    <h3 className="text-[15px] font-bold text-gray-900 mb-1">{title}</h3>
                    <p className="text-[12px] text-gray-500 leading-tight">{description}</p>
                </div>
                <button 
                    onClick={() => handleToggleConsent(type, checked, title)}
                    className={`w-12 h-7 rounded-full transition-colors relative ${checked ? 'bg-[#1A73E8]' : 'bg-gray-200'}`}
                >
                    <div className={`absolute top-1 left-1 w-5 h-5 bg-white rounded-full shadow-sm transition-transform ${checked ? 'translate-x-5' : 'translate-x-0'}`}></div>
                </button>
            </div>
            {checked && (
                <div className="mt-3 flex items-center gap-1.5 text-[11px] font-medium text-[#1A73E8] bg-blue-50 px-3 py-1.5 rounded-lg w-fit">
                    <CheckCircle2 size={12} />
                    <span>동의 중 (즉시 혜택 적용)</span>
                </div>
            )}
        </div>
    );

    return (
        <div className="flex flex-col min-h-screen bg-[#F5F6F8]">
            <header className="flex items-center h-14 px-4 bg-white sticky top-0 z-10 border-b border-gray-100">
                <button onClick={() => navigate(-1)} className="p-2 -ml-2 text-gray-800 hover:bg-gray-50 rounded-full transition-colors">
                    <ChevronLeft size={24} />
                </button>
                <h1 className="text-[17px] font-bold text-gray-900 ml-1">내 정보</h1>
            </header>

            <main className="flex-1 p-5 space-y-6 max-w-[480px] mx-auto w-full">
                
                {/* 1. Basic User Info */}
                <section>
                    <h2 className="text-[17px] font-bold text-gray-800 mb-3 px-1">기본 정보</h2>
                    <div className="bg-white rounded-[24px] p-6 shadow-sm border border-gray-50">
                        <div className="flex items-center gap-4 mb-5">
                            <div className="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center text-gray-400">
                                <User size={24} />
                            </div>
                            <div>
                                <div className="text-[13px] text-gray-500 font-medium mb-0.5">이름</div>
                                <div className="text-[18px] font-bold text-gray-900">{userInfo.name}</div>
                            </div>
                        </div>
                        <div className="w-full h-px bg-gray-100 my-4"></div>
                        <div className="flex items-center gap-4">
                            <div className="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center text-gray-400">
                                <Phone size={24} />
                            </div>
                            <div>
                                <div className="text-[13px] text-gray-500 font-medium mb-0.5">휴대전화번호</div>
                                <div className="text-[18px] font-bold text-gray-900 font-mono tracking-wide">{userInfo.phone}</div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* 2. Consent Management */}
                <section>
                    <div className="flex items-center justify-between mb-3 px-1">
                        <div className="flex items-center gap-1.5">
                            <Shield size={16} className="text-[#1A73E8]" />
                            <h2 className="text-[17px] font-bold text-gray-800">개인정보 동의 관리</h2>
                        </div>
                        <span className="text-[11px] text-gray-400 font-medium">안전한 데이터 관리 중</span>
                    </div>
                    
                    <div className="rounded-[24px] overflow-hidden shadow-sm border border-gray-50">
                        <ConsentItem 
                            title="제3자 정보 제공 동의" 
                            type="THIRD_PARTY"
                            checked={consentStatus.thirdPartyProvisionAgreed}
                            description="제휴 서비스 및 맞춤 혜택 제공을 위해 제3자에게 정보를 제공합니다."
                        />
                        <ConsentItem 
                            title="제휴 TM 센터 연락처 제공" 
                            type="SSAP_PROVISION"
                            checked={consentStatus.ssapProvisionAgreed}
                            description="Continue Call 전문 상담원이 고객님께 꼭 맞는 금융 상품을 안내해 드려요."
                        />

                        <ConsentItem 
                            title="혜택/이벤트 알림 (SMS/Push)" 
                            type="MARKETING"
                            checked={consentStatus.marketingAgreed}
                            description="특별 혜택과 이벤트 소식을 전화나 문자로 가장 먼저 받아보세요."
                        />
                    </div>
                    
                    <div className="mt-4 px-2 text-[11px] text-gray-400 leading-relaxed font-medium">
                        * 선택 항목에 동의하지 않아도 서비스 이용은 가능하나, 일부 맞춤형 혜택 제공이 제한될 수 있습니다.<br/>
                        * 동의 철회 시 즉시 반영되며, 이미 제공된 정보는 파기 요청됩니다.
                    </div>
                </section>
            </main>
        </div>
    );
};

export default MyPage;
