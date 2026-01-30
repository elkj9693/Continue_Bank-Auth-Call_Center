import React, { useState } from 'react';
import { X, ShieldCheck, CheckCircle2, ChevronRight, AlertCircle } from 'lucide-react';

const MarketingConsentModal = ({ isOpen, onClose, onConfirm }) => {
  const [step, setStep] = useState('LIST'); // LIST, DETAIL
  const [agreements, setAgreements] = useState({
    required: false,
    marketing: false,
    thirdParty: false
  });

  if (!isOpen) return null;

  const handleAllAgree = () => {
    const nextState = !Object.values(agreements).every(v => v);
    setAgreements({
      required: nextState,
      marketing: nextState,
      thirdParty: nextState
    });
  };

  const handleConfirm = () => {
    if (agreements.required && agreements.marketing && agreements.thirdParty) {
      onConfirm();
    } else {
      alert('모든 필수 및 선택 항목에 동의해 주세요 (이벤트 참여 대상).');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/50 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-[480px] bg-white rounded-t-[32px] sm:rounded-[32px] overflow-hidden animate-slide-up shadow-2xl">
        {/* Header */}
        <div className="px-6 py-6 flex justify-between items-center border-b border-gray-50">
          <h2 className="text-[20px] font-bold text-gray-900 tracking-tight">이벤트 참여 동의</h2>
          <button onClick={onClose} className="p-2 text-gray-400 hover:bg-gray-50 rounded-full transition-colors">
            <X size={24} />
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-8">
          <div className="bg-blue-50/50 rounded-2xl p-5 mb-8 flex items-start gap-3 border border-blue-100/50">
            <GiftIcon className="w-6 h-6 text-[#1A73E8] shrink-0" />
            <div>
              <p className="text-[14px] font-bold text-[#1A73E8]">Continue 신용카드 연회비 무료!</p>
              <p className="text-[12px] text-gray-500 mt-1 leading-relaxed">
                상담만 해도 스타벅스 쿠폰이! (마케팅 동의 시)
              </p>
            </div>
          </div>

          <div className="space-y-6">
            {/* All Agree */}
            <label className="flex items-center gap-4 p-4 rounded-2xl bg-gray-50 cursor-pointer group active:scale-[0.98] transition-all">
              <input
                type="checkbox"
                checked={Object.values(agreements).every(v => v)}
                onChange={handleAllAgree}
                className="hidden"
              />
              <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all ${Object.values(agreements).every(v => v) ? 'bg-[#1A73E8] border-[#1A73E8]' : 'border-gray-200'}`}>
                {Object.values(agreements).every(v => v) && <CheckCircle2 size={16} className="text-white" />}
              </div>
              <span className="text-[16px] font-bold text-gray-900">전체 동의하기</span>
            </label>

            {/* Individual Agreemets */}
            <div className="space-y-4 px-2">
              <AgreementItem
                label="[필수] 개인정보 수집·이용 동의"
                checked={agreements.required}
                onChange={() => setAgreements(prev => ({ ...prev, required: !prev.required }))}
              />
              <AgreementItem
                label="[선택] 마케팅 정보 수신 동의"
                checked={agreements.marketing}
                onChange={() => setAgreements(prev => ({ ...prev, marketing: !prev.marketing }))}
              />
              <AgreementItem
                label="[선택] 제3자 제공 및 위탁 동의"
                checked={agreements.thirdParty}
                onChange={() => setAgreements(prev => ({ ...prev, thirdParty: !prev.thirdParty }))}
                showDetail
              />
            </div>
          </div>

          {/* Compliance Box */}
          <div className="mt-10 p-5 bg-gray-50 rounded-2xl border border-dashed border-gray-200">
            <div className="flex items-center gap-2 mb-3">
              <AlertCircle size={14} className="text-gray-400" />
              <span className="text-[12px] font-bold text-gray-500">2026 개인정보 처리 위탁 고지</span>
            </div>
            <ul className="text-[11px] text-gray-400 space-y-1.5 leading-relaxed font-medium">
              <li>• 수탁사: Continue TM센터 (제공 목적: Continue 카드 상담)</li>
              <li>• 수집 항목: 이름, 휴대전화번호 (CI 데이터는 가명처리)</li>
              <li>• 보유 기간: 상담 완료 후 3개월 (이후 즉시 파기)</li>
              <li>• 동의를 거부할 수 있으며, 거부 시 이벤트 참여가 제한됩니다.</li>
            </ul>
          </div>
        </div>

        {/* Action */}
        <div className="p-6 pt-0">
          <button
            onClick={handleConfirm}
            className="w-full h-15 bg-[#1A73E8] text-white rounded-2xl font-bold text-[16px] shadow-lg shadow-blue-100 hover:brightness-110 active:scale-[0.98] transition-all"
          >
            확인 및 참여하기
          </button>
        </div>
      </div>
    </div>
  );
};

const AgreementItem = ({ label, checked, onChange, showDetail }) => (
  <div className="flex items-center justify-between py-1 group">
    <div className="flex items-center gap-3 cursor-pointer" onClick={onChange}>
      <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-all ${checked ? 'bg-[#1A73E8] border-[#1A73E8]' : 'border-gray-200'}`}>
        {checked && <div className="w-2 h-2 bg-white rounded-full" />}
      </div>
      <span className={`text-[14px] font-medium transition-colors ${checked ? 'text-gray-900 font-bold' : 'text-gray-500'}`}>{label}</span>
    </div>
    {showDetail && (
      <button className="p-2 text-gray-300 hover:text-gray-400">
        <ChevronRight size={16} />
      </button>
    )}
  </div>
);

const GiftIcon = ({ className }) => (
  <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v13m0-13V6a2 2 0 112 2h-2zm0 0V6a2 2 0 10-2 2h2zm0 0h3a2 2 0 110 4h-3m0-4h-3a2 2 0 100 4h3m0 0v8m0-8h3a2 2 0 110 4h-3m0-4h-3a2 2 0 100 4h3" />
  </svg>
);

export default MarketingConsentModal;
