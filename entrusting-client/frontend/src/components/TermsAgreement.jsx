import React, { useState, useEffect } from 'react';
import { Check, ChevronRight, ChevronDown, ShieldCheck, Lock, AlertCircle } from 'lucide-react';
import Logo from './Logo';

/**
 * 고도화된 금융 컴플라이언스 약관 동의 페이지
 * 9종 필수 + 2종 선택 약관 및 전문보기(Accordion) 기능 구현
 */
const TermsAgreement = ({ onComplete }) => {
  const [agreements, setAgreements] = useState({
    age: false,           // 1. 만 14세 이상
    terms: false,         // 2. 서비스 이용약관
    privacy: false,       // 3. 개인정보 수집·이용
    uniqueId: false,      // 4. 고유식별정보 처리
    creditInfo: false,    // 5. 신용정보 조회·제공
    electronicFinance: false, // 6. 전자금융거래 기본약관
    monitoring: false,    // 7. 거래 모니터링/AML
    ssapProvision: false, // 8. 제휴 TM (선택)
    thirdPartyProvision: false, // 9. 제3자 제공 (선택)
    marketingPersonal: false, // 10. 개인맞춤형 추천 (선택)
    marketing: false      // 11. 혜택/이벤트 알림 (선택)
  });

  const [expandedId, setExpandedId] = useState(null);
  const [visibleItems, setVisibleItems] = useState(0);

  useEffect(() => {
    const itemsCount = 13; 
    for (let i = 0; i <= itemsCount; i++) {
      setTimeout(() => setVisibleItems(i), i * 80);
    }
  }, []);

  const requiredItems = [
    'age', 'terms', 'privacy', 'uniqueId', 'creditInfo', 
    'electronicFinance', 'monitoring'
  ];
  const allRequiredChecked = requiredItems.every(item => agreements[item]);
  const allChecked = Object.values(agreements).every(v => v);

  const handleAllCheck = (type = 'all') => {
    const newAgreements = { ...agreements };
    if (type === 'required') {
      requiredItems.forEach(key => newAgreements[key] = true);
    } else {
      const targetValue = !allChecked;
      Object.keys(newAgreements).forEach(key => newAgreements[key] = targetValue);
    }
    setAgreements(newAgreements);
  };

  const handleItemCheck = (item) => {
    setAgreements(prev => ({ ...prev, [item]: !prev[item] }));
  };

  const handleNext = (fullAgree = false) => {
    if (fullAgree) {
      handleAllCheck('all');
      // 시간차를 두고 완료 처리 (동작 확인용)
      setTimeout(() => {
        onComplete({
          agreements: Object.fromEntries(Object.keys(agreements).map(k => [k, true])),
          marketingChannels: { sms: true },
          agreedAt: new Date().toISOString()
        });
      }, 300);
      return;
    }
    
    if (!allRequiredChecked) return;
    onComplete({
      agreements,
      marketingChannels: agreements.marketing ? { sms: true } : null,
      agreedAt: new Date().toISOString()
    });
  };

  const toggleExpand = (id, e) => {
    e.stopPropagation();
    setExpandedId(expandedId === id ? null : id);
  };

  const agreementList = [
    { 
      id: 'age', 
      title: '만 14세 이상이며, 본인 명의 계좌임을 확인합니다', 
      required: true, 
      desc: "금융거래를 위한 기본 연령 확인입니다." 
    },
    { 
      id: 'terms', 
      title: 'Continue Bank 서비스 이용약관', 
      required: true, 
      hasDetail: true, 
      desc: "거래 한도, 분쟁 처리 등 서비스 전반에 대한 약속입니다.",
      summary: { items: "이용자 식별 정보", period: "서비스 이용 종료 시까지" }
    },
    { 
      id: 'privacy', 
      title: '개인정보 수집 및 이용 동의', 
      required: true, 
      hasDetail: true, 
      desc: "이름, 연락처 등 서비스 제공에 필요한 정보를 수집합니다.",
      summary: { items: "이름, 휴대전화번호, CI(연계정보), DI", period: "회원 탈퇴 후 5년까지" }
    },
    { 
      id: 'uniqueId', 
      title: '고유식별정보(주민번호 등) 처리 동의', 
      required: true, 
      hasDetail: true, 
      desc: "금융거래 및 본인확인을 위해 민감정보를 처리합니다.",
      summary: { items: "주민등록번호(또는 외국인등록번호)", period: "법령에 따른 보관 기간까지" }
    },
    { 
      id: 'creditInfo', 
      title: '신용정보 조회 및 제공 동의', 
      required: true, 
      hasDetail: true, 
      desc: "신용 평가 및 감독 규정 준수를 위해 신용 정보를 조회합니다.",
      summary: { items: "신용점수, 대출/연체 이력, 카드 개설 정보", period: "거래 종료 후 5년" }
    },
    { 
      id: 'electronicFinance', 
      title: '전자금융거래 기본약관 동의', 
      required: true, 
      hasDetail: true, 
      desc: "비밀번호 관리, 손실배상 등 전자금융 기본 수칙입니다.",
      summary: { items: "접속 기록, 단말기 정보(IP 등)", period: "5년 (전자금융거래법)" }
    },
    { 
      id: 'monitoring', 
      title: '금융거래 정보 모니터링 및 기록 동의', 
      required: true, 
      hasDetail: true, 
      desc: "자금세탁방지(AML) 및 모니터링을 위한 법적 절차입니다.",
      summary: { items: "거래 일시, 금액, 상대방 정보", period: "5년 (특정금융정보법)" }
    },
    { 
      id: 'thirdPartyProvision', 
      title: '[선택] 제3자 정보 제공 동의', 
      required: false, 
      hasDetail: true, 
      desc: "더 나은 서비스 제공을 위해 제휴사에 정보를 제공합니다.",
      summary: { items: "이름, 휴대전화번호, CI (연계정보)", period: "제휴 서비스 종료 시까지" }
    },
    { 
      id: 'ssapProvision', 
      title: '[선택] 제휴 TM 센터(Continue Call)에 상품 소개 목적의 연락처 제공', 
      required: false, 
      hasDetail: true, 
      desc: "전문 상담원이 고객님께 꼭 맞는 금융 상품을 안내해 드려요.",
      summary: { items: "이름, 전화번호, 마케팅 활용 동의 여부", period: "동의 철회 시까지" }
    },
    { 
      id: 'marketingPersonal', 
      title: '[선택] 개인맞춤형 금융상품 추천 동의', 
      required: false, 
      hasDetail: true, 
      desc: "고객님의 패턴을 분석해 꼭 필요한 상품을 추천해 드려요.",
      summary: { items: "상품 가입 이력, 소비 패턴 데이터", period: "동의 철회 또는 탈퇴 시까지" } 
    },
    { 
      id: 'marketing', 
      title: '[선택] 혜택 및 이벤트 소식 받기 (전화, 문자 메시지)', 
      required: false, 
      hasDetail: true, 
      desc: "특별 혜택과 이벤트 소식을 가장 먼저 보내드릴게요.",
      summary: { items: "휴대전화번호, 이메일", period: "2년 (2년 주기 재동의)" }
    }
  ];

  return (
    <div className="flex flex-col min-h-screen bg-white">
      <header className="flex items-center h-20 px-6 shrink-0">
        <div className="flex-1 flex justify-center"><Logo /></div>
      </header>

      <main className="flex-1 px-8 py-10 max-w-[520px] mx-auto w-full overflow-y-auto scrollbar-hide">
        {/* Step Info (Optional, but gives context) */}
        <div className={`mb-10 transition-all duration-700 ${visibleItems >= 1 ? 'translate-y-0 opacity-100' : 'translate-y-6 opacity-0'}`}>
          <div className="flex items-center gap-1.5 bg-blue-50 w-fit px-3 py-1 rounded-full border border-blue-100/50 mb-6">
            <ShieldCheck size={14} className="text-[#1A73E8]" />
            <span className="text-[13px] font-bold text-[#1A73E8]">강력한 보안 보호됨</span>
          </div>
          <h1 className="text-[30px] font-bold text-[#1A1A1A] leading-tight tracking-tight mb-4">
            반가워요!<br />안전한 금융생활을 위해<br /><span className="text-[#1A73E8]">약관 동의</span>가 필요해요.
          </h1>
          <p className="text-gray-400 text-sm font-medium">개인정보 보호법 및 신용정보법에 의거하여<br/>꼭 필요한 정보만 안전하게 처리합니다.</p>
        </div>

        {/* [NEW] 전체 동의 카드 (금융사 스타일) */}
        <div 
          onClick={() => handleAllCheck()}
          className={`
            mb-10 p-6 rounded-[24px] cursor-pointer transition-all duration-500 transform
            ${visibleItems >= 2 ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}
            ${allChecked ? 'bg-blue-50/70 border-2 border-[#1A73E8]/20' : 'bg-gray-50 border-2 border-transparent hover:bg-gray-100/80'}
          `}
        >
          <div className="flex items-center">
            <div className={`
              w-7 h-7 rounded-full border-2 flex items-center justify-center mr-4 transition-all duration-300
              ${allChecked ? 'bg-[#1A73E8] border-transparent shadow-lg shadow-blue-200' : 'bg-white border-gray-200'}
            `}>
              <Check size={18} className={allChecked ? 'text-white' : 'text-gray-200'} strokeWidth={4} />
            </div>
            <div className="flex-1">
              <span className={`text-[18px] font-bold ${allChecked ? 'text-[#1A73E8]' : 'text-[#1A1A1A]'}`}>
                모든 약관에 동의합니다
              </span>
              <p className="text-[13px] text-gray-400 font-medium mt-0.5">필수 및 선택 항목 포함</p>
            </div>
          </div>
        </div>

        {/* Section Holder */}
        <div className="space-y-6">
          {/* Required Section Label */}
          <div className={`flex items-center gap-2 transition-all duration-500 ${visibleItems >= 3 ? 'opacity-100' : 'opacity-0'}`}>
            <AlertCircle size={16} className="text-[#1A73E8]" />
            <span className="text-sm font-bold text-gray-500">필수 동의 항목 (반드시 필요)</span>
          </div>

          <div className="space-y-1">
            {agreementList.map((item, index) => (
              <div key={item.id} className={`transition-all duration-500 transform ${visibleItems >= index + 4 ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}`}>
                {index === 7 && ( // Index 7 is the first optional item
                   <div className="flex items-center gap-2 mt-10 mb-4 opacity-100">
                    <span className="text-sm font-bold text-gray-500">선택 동의 항목 (선택 가능)</span>
                  </div>
                )}
                <AgreementItem 
                  checked={agreements[item.id]} 
                  onChange={() => handleItemCheck(item.id)}
                  onExpand={(e) => toggleExpand(item.id, e)}
                  isExpanded={expandedId === item.id}
                  {...item}
                />
              </div>
            ))}
          </div>
        </div>

        <div className={`mt-10 flex items-center justify-center gap-2 transition-all duration-1000 ${visibleItems >= 12 ? 'opacity-100' : 'opacity-0'}`}>
          <Lock size={14} className="text-gray-300" />
          <p className="text-[12px] text-gray-300 font-medium">개인정보는 AES-256 방식으로 안전하게 암호화됩니다.</p>
        </div>
      </main>

      {/* Action Button (금융사 스타일) */}
      <div className={`px-8 pb-12 max-w-[520px] mx-auto w-full transition-all duration-700 ${visibleItems >= 12 ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'}`}>
        <button 
          onClick={() => handleNext(false)} 
          disabled={!allRequiredChecked}
          className={`
            w-full py-5 rounded-[20px] font-bold text-[17px] transition-all duration-300 active:scale-[0.98]
            ${allRequiredChecked 
              ? 'bg-[#1A73E8] text-white shadow-xl shadow-blue-100' 
              : 'bg-gray-100 text-gray-400 cursor-not-allowed'}
          `}
        >
          {allChecked ? '동의하고 계속하기' : (allRequiredChecked ? '선택 항목 확인 후 계속하기' : '필수 항목에 동의해 주세요')}
        </button>
      </div>
    </div>
  );
};

const AgreementItem = ({ checked, onChange, required, title, desc, summary, hasDetail, isExpanded, onExpand }) => {
  const labelBg = required ? "bg-blue-50" : "bg-purple-50";
  const textColor = required ? "text-[#1A73E8]" : "text-purple-600";
  const checkBg = required ? (checked ? "bg-[#1A73E8]" : "bg-white") : (checked ? "bg-purple-500" : "bg-white");

  return (
    <div className="border-b border-gray-50 last:border-0 overflow-hidden">
      <div 
        className="group flex flex-col py-4.5 px-2 cursor-pointer active:opacity-70 transition-all"
        onClick={onChange}
      >
        <div className="flex items-start">
            <div className={`w-6 h-6 mt-0.5 rounded-full border-2 flex items-center justify-center mr-4 transition-all duration-300 ${checked ? 'border-transparent' : 'border-gray-200'} ${checkBg} shrink-0`}>
            {checked && <Check size={14} className="text-white" strokeWidth={4} />}
            </div>
            
            <div className="flex-1 flex flex-col gap-1">
                <div className="flex items-start gap-2 pr-6 relative">
                    <span className={`text-[10px] font-black px-1.5 py-0.5 rounded-[4px] ${labelBg} ${textColor} shrink-0 mt-0.5`}>{required ? '필수' : '선택'}</span>
                    <span className={`text-[15.5px] font-bold tracking-tight leading-snug transition-colors ${checked ? 'text-[#1A1A1A]' : 'text-gray-500'}`}>{title}</span>
                    {hasDetail && (
                        <button onClick={(e) => { e.stopPropagation(); onExpand(e); }} className="absolute right-[-8px] top-[-4px] p-2 text-gray-300 hover:text-gray-500 transition-colors">
                            {isExpanded ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                        </button>
                    )}
                </div>
                
                {/* 2026 Compliance: Summary Display moved to expandable section */}
            </div>
        </div>
      </div>

      <div className={`px-2 pb-4 pl-12 transition-all duration-300 ease-in-out ${isExpanded ? 'max-h-[400px] opacity-100' : 'max-h-0 opacity-0 pointer-events-none'}`}>
        <div className="p-4 bg-gray-50 rounded-2xl">
          {summary && (
              <div className="mb-3 text-[12px] font-medium text-gray-400 bg-white p-3 rounded-xl border border-gray-100 shadow-sm">
                  <div className="flex gap-2">
                     <span className="text-gray-500 w-12 shrink-0">수집항목</span>
                     <span className="text-gray-800">{summary.items}</span>
                  </div>
                  <div className="flex gap-2 mt-1">
                     <span className="text-gray-500 w-12 shrink-0">보유기간</span>
                     <span className="text-[#1A73E8] font-bold">{summary.period}</span>
                  </div>
              </div>
          )}
          <p className="text-[13px] text-gray-500 leading-relaxed font-medium">*{desc}</p>
          <div className="mt-3 p-3 bg-white rounded-xl border border-gray-100">
             <p className="text-[11px] text-gray-400 font-mono leading-tight">
               [전문보기 요약]<br/>
               제1조 (목적) 이 약관은 Continue Bank의 서비스를 이용함에 있어...<br/>
               제2조 (권리/의무) 회사는 고객의 정보를 안전하게...
             </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TermsAgreement;