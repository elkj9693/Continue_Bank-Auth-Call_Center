import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, Calendar, CheckCircle2, XCircle, ChevronLeft, User, FileText, Trash2, LogOut } from 'lucide-react';
import Logo from '../components/Logo';

/**
 * [COMPLIANCE] 동의 내역 관리 페이지
 * 사용자가 자신의 동의 현황을 확인하고 선택 항목을 철회할 수 있는 기능 제공
 */
const ConsentManagement = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [terms, setTerms] = useState(null);
  const [authRecords, setAuthRecords] = useState([]);

  useEffect(() => {
    // 실제 운영 환경에서는 API를 통해 가져오지만, 교육용으로 세션/더미 활용
    const savedUser = JSON.parse(sessionStorage.getItem('user') || '{"name":"홍길동", "joinedAt":"2026-01-28T14:30:00"}');
    const savedTerms = JSON.parse(sessionStorage.getItem('terms_agreement') || '{"agreements":{"age":true,"terms":true,"privacy":true,"uniqueId":true,"creditInfo":true,"carrierAuth":true,"ssapProvision":true,"electronicFinance":true,"monitoring":true,"marketingPersonal":true,"marketing":false},"agreedAt":"2026-01-28T14:30:00"}');
    
    setUser(savedUser);
    setTerms(savedTerms);
    
    // 더미 본인인증 기록
    setAuthRecords([
      { purpose: '회원가입 시 본인인증', date: '2026-01-28T14:32:00', method: 'SSAP 휴대폰 인증' },
      { purpose: '계좌 개설 시 본인인증', date: '2026-01-28T14:40:00', method: 'SSAP 휴대폰 인증' }
    ]);
  }, []);

  const handleWithdraw = (type) => {
    if (window.confirm("정말로 동의를 철회하시겠습니까? 관련 혜택 안내가 중단됩니다.")) {
      setTerms(prev => ({
        ...prev,
        agreements: { ...prev.agreements, [type]: false }
      }));
      alert("철회되었습니다. 재동의는 48시간 이후에 가능합니다.");
    }
  };

  const handleAgree = (type) => {
    if (window.confirm("해당 항목에 동의하시겠습니까?")) {
      setTerms(prev => ({
        ...prev,
        agreements: { ...prev.agreements, [type]: true }
      }));
      alert("동의가 완료되었습니다.");
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '2026-01-28 14:30';
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  };

  const requiredTerms = [
    { id: 'age', title: '만 14세 이상 확인' },
    { id: 'terms', title: '서비스 이용약관' },
    { id: 'privacy', title: '개인정보 수집 및 이용' },
    { id: 'uniqueId', title: '고유식별정보 처리' },
    { id: 'creditInfo', title: '신용정보 조회 및 제공' },
    { id: 'carrierAuth', title: 'SSAP 본인인증 이용' },
    { id: 'ssapProvision', title: '개인정보의 SSAP 제공' },
    { id: 'electronicFinance', title: '전자금융거래 기본약관' },
    { id: 'monitoring', title: '금융거래 정보 모니터링' }
  ];

  const optionalTerms = [
    { id: 'marketingPersonal', title: '개인맞춤형 금융상품 추천' },
    { id: 'marketing', title: '혜택 및 이벤트 소식' }
  ];

  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Header */}
      <header className="flex items-center h-20 px-6 border-b border-gray-100">
        <button onClick={() => navigate('/dashboard')} className="p-2 -ml-2 rounded-full hover:bg-gray-50 transition-colors">
          <ChevronLeft size={28} className="text-gray-700" />
        </button>
        <div className="flex-1 flex justify-center -ml-10">
          <Logo />
        </div>
      </header>

      <main className="flex-1 px-8 py-12 max-w-[600px] mx-auto w-full">
        {/* 페이지 제목 */}
        <div className="mb-10">
          <h1 className="text-[32px] font-bold text-gray-900 mb-3 tracking-tight">동의 내역 관리</h1>
          <p className="text-gray-500 font-medium text-[15px]">귀하가 제공하신 동의 내역을 관리하실 수 있습니다.</p>
        </div>

        {/* 필수 동의 항목 */}
        <section className="mb-12">
          <h2 className="text-[15px] font-bold text-gray-900 mb-4 flex items-center gap-2">
              <div className="w-1 h-4 rounded-full" style={{ backgroundColor: 'rgb(26, 115, 232)' }}></div>
            필수 동의 항목 <span className="text-gray-400 text-[13px] font-medium">(철회 불가)</span>
          </h2>
          <div className="bg-gray-50 rounded-2xl border border-gray-100 divide-y divide-gray-100 overflow-hidden">
            {requiredTerms.map((term) => (
              <div key={term.id} className="p-5 flex items-center justify-between bg-white">
                <div className="flex items-center gap-3">
                  <CheckCircle2 size={20} className="text-emerald-500 shrink-0" />
                  <div>
                    <p className="font-bold text-gray-900 text-[14px]">{term.title}</p>
                    <p className="text-[12px] text-gray-400 font-medium mt-0.5">
                      동의일: {formatDate(terms?.agreedAt)}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* 선택 동의 항목 */}
        <section className="mb-12">
          <h2 className="text-[15px] font-bold text-gray-900 mb-4 flex items-center gap-2">
            <div className="w-1 h-4 bg-purple-500 rounded-full"></div>
            선택 동의 항목 <span className="text-gray-400 text-[13px] font-medium">(철회 가능)</span>
          </h2>
          <div className="bg-gray-50 rounded-2xl border border-gray-100 divide-y divide-gray-100 overflow-hidden">
            {optionalTerms.map((term) => {
              const agreed = terms?.agreements?.[term.id];
              return (
                <div key={term.id} className="p-5 flex items-center justify-between bg-white">
                  <div className="flex items-center gap-3">
                    {agreed ? (
                      <CheckCircle2 size={20} className="text-purple-500 shrink-0" />
                    ) : (
                      <XCircle size={20} className="text-gray-300 shrink-0" />
                    )}
                    <div>
                      <p className="font-bold text-gray-900 text-[14px]">{term.title}</p>
                      <p className="text-[12px] text-gray-400 font-medium mt-0.5">
                        {agreed ? `동의일: ${formatDate(terms?.agreedAt)}` : '미동의'}
                      </p>
                    </div>
                  </div>
                  {agreed ? (
                    <button 
                      onClick={() => handleWithdraw(term.id)}
                      className="text-[13px] font-bold text-red-500 hover:bg-red-50 px-4 py-2 rounded-xl transition-all"
                    >
                      철회하기
                    </button>
                  ) : (
                    <button 
                      onClick={() => handleAgree(term.id)}
                      className="text-[13px] font-bold hover:bg-blue-50 px-4 py-2 rounded-xl transition-all"
                      style={{ color: 'rgb(26, 115, 232)' }}
                    >
                      동의하기
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        </section>

        {/* 본인인증 기록 */}
        <section className="mb-12">
          <h2 className="text-[15px] font-bold text-gray-900 mb-4 flex items-center gap-2">
            <div className="w-1 h-4 bg-emerald-500 rounded-full"></div>
            본인인증 기록
          </h2>
          <div className="bg-gray-50 rounded-2xl border border-gray-100 divide-y divide-gray-100 overflow-hidden">
            {authRecords.map((record, index) => (
              <div key={index} className="p-5 bg-white">
                <div className="flex items-start gap-3">
                  <ShieldCheck size={18} className="text-emerald-500 shrink-0 mt-0.5" />
                  <div className="flex-1">
                    <p className="font-bold text-gray-900 text-[14px]">{record.purpose}</p>
                    <p className="text-[12px] text-gray-400 font-medium mt-1">
                      {formatDate(record.date)} | {record.method}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* 개인정보 관련 요청 */}
        <section className="mb-12">
          <h2 className="text-[15px] font-bold text-gray-900 mb-4 flex items-center gap-2">
            <div className="w-1 h-4 bg-gray-400 rounded-full"></div>
            개인정보 관련 요청
          </h2>
          <div className="grid grid-cols-2 gap-3">
            <button className="p-4 bg-white border border-gray-200 rounded-2xl hover:bg-gray-50 transition-all flex items-center justify-center gap-2 font-bold text-gray-700 text-[14px]">
              <User size={18} />
              개인정보 조회
            </button>
            <button className="p-4 bg-white border border-gray-200 rounded-2xl hover:bg-gray-50 transition-all flex items-center justify-center gap-2 font-bold text-gray-700 text-[14px]">
              <FileText size={18} />
              개인정보 정정
            </button>
            <button className="p-4 bg-white border border-gray-200 rounded-2xl hover:bg-gray-50 transition-all flex items-center justify-center gap-2 font-bold text-gray-700 text-[14px]">
              <Trash2 size={18} />
              개인정보 삭제 요청
            </button>
            <button className="p-4 bg-white border border-red-200 rounded-2xl hover:bg-red-50 transition-all flex items-center justify-center gap-2 font-bold text-red-500 text-[14px]">
              <LogOut size={18} />
              계정 탈퇴
            </button>
          </div>
        </section>

        {/* 안내사항 */}
        <div className="p-6 bg-blue-50 rounded-2xl border border-blue-100">
          <p className="text-[13px] font-bold text-gray-700 mb-3 flex items-center gap-1.5">
            <span style={{ color: 'rgb(26, 115, 232)' }}>⚠️</span> 안내사항
          </p>
          <ul className="text-[12px] text-gray-600 font-medium space-y-2 leading-relaxed">
            <li className="flex items-start gap-1.5">
              <span className="text-gray-400 mt-0.5">•</span>
              <span>필수 동의 항목은 서비스 이용을 위해 필수이므로 철회할 수 없습니다</span>
            </li>
            <li className="flex items-start gap-1.5">
              <span className="text-gray-400 mt-0.5">•</span>
              <span>선택 동의 항목 철회 후 재동의까지 48시간 소요될 수 있습니다</span>
            </li>
            <li className="flex items-start gap-1.5">
              <span className="text-gray-400 mt-0.5">•</span>
              <span>모든 동의 기록은 법적 증거로 보관됩니다</span>
            </li>
          </ul>
        </div>
      </main>
    </div>
  );
};

export default ConsentManagement;
