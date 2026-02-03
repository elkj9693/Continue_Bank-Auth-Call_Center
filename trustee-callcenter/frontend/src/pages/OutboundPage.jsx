import { useState, useEffect } from "react";
import {
    RefreshCcw,
    Phone,
    User,
    CheckCircle2,
    XCircle,
    Clock,
    Mic2,
    Save,
    ChevronLeft,
    ChevronRight
} from "lucide-react";

const API_BASE = ""; // Relative path using Vite proxy

export default function OutboundPage() {
    const [targets, setTargets] = useState([]);
    const [selectedLead, setSelectedLead] = useState(null);
    const [loading, setLoading] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [callStatus, setCallStatus] = useState("IDLE"); // IDLE, CALLING, CONNECTED
    const [outcome, setOutcome] = useState("COMPLETED");

    useEffect(() => {
        fetchTargets();
    }, []);

    const fetchTargets = async (isManual = false) => {
        if (isManual) setRefreshing(true);
        else setLoading(true);

        try {
            const res = await fetch(`${API_BASE}/api/v1/outbound/targets`);
            if (!res.ok) throw new Error(`HTTP error ${res.status}`);

            const data = await res.json();
            if (Array.isArray(data)) setTargets(data);
        } catch (e) {
            console.error("[Outbound] Fetch error:", e);
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    const startCall = (lead) => {
        setSelectedLead(lead);
        setCallStatus("CALLING");
        setTimeout(() => setCallStatus("CONNECTED"), 1800);
    };

    const submitResult = async () => {
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE}/api/v1/outbound/result`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    leadId: selectedLead.leadId,
                    customerName: selectedLead.name, // [NEW] 이름 전송
                    status: outcome,
                    agentId: sessionStorage.getItem("callcenter-user") || "AGENT_001"
                }),
            });
            if (!res.ok) throw new Error("Result submission failed");
            setCallStatus("IDLE");
            setSelectedLead(null);
            fetchTargets();
        } catch (e) {
            alert("저장 실패: " + e.message);
        } finally {
            setLoading(false);
        }
    };

    if (!selectedLead) {
        return (
            <div className="animate-up">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '32px' }}>
                    <div>
                        <h2 style={{ fontSize: '28px', fontWeight: '800', marginBottom: '8px' }}>상담 대기 고객</h2>
                        <p style={{ color: 'var(--text-dim)', fontWeight: '600' }}>오늘은 {targets.length}명의 고객님이 기다리고 계세요.</p>
                    </div>
                    <button
                        className="btn-toss btn-toss-secondary"
                        onClick={() => fetchTargets(true)}
                        disabled={refreshing}
                        style={{ padding: '12px 20px', fontSize: '14px' }}
                    >
                        <RefreshCcw size={16} className={refreshing ? "animate-spin" : ""} />
                        업데이트
                    </button>
                </div>

                {loading ? (
                    <div style={{ textAlign: 'center', padding: '100px 0', color: 'var(--primary)', fontWeight: '800' }}>
                        불러오는 중...
                    </div>
                ) : targets.length > 0 ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {targets.map((t, idx) => (
                            <div key={t.leadId || idx} className="toss-card" style={{
                                padding: '24px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'space-between',
                                transition: 'transform 0.2s',
                                cursor: 'pointer'
                            }} onClick={() => startCall(t)}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                                    <div className="avatar" style={{ background: '#f2f4f6', color: '#191f28' }}>
                                        <User size={20} />
                                    </div>
                                    <div>
                                        <div style={{ fontSize: '18px', fontWeight: '800' }}>{t.name}</div>
                                        <div style={{ fontSize: '14px', color: 'var(--text-dim)', fontWeight: '600' }}>{t.phone}</div>
                                    </div>
                                </div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                                    <span style={{ fontSize: '14px', fontWeight: '800', color: 'var(--primary)', background: 'var(--primary-bg)', padding: '6px 12px', borderRadius: '12px' }}>
                                        {t.requestedProductType || '이벤트 상담'}
                                    </span>
                                    <ChevronRight size={20} color="#adb5bd" />
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="toss-card" style={{ textAlign: 'center', padding: '80px 0' }}>
                        <div style={{ fontSize: '48px', marginBottom: '16px' }}>✨</div>
                        <h3 style={{ fontSize: '20px', fontWeight: '800', marginBottom: '8px' }}>모든 상담이 완료되었습니다</h3>
                        <p style={{ color: 'var(--text-dim)' }}>새로운 상담 신청이 들어오면 알려드릴게요.</p>
                    </div>
                )}
            </div>
        );
    }

    return (
        <div className="animate-up">
            <button
                onClick={() => { setSelectedLead(null); setCallStatus("IDLE"); }}
                style={{ background: 'none', border: 'none', color: 'var(--primary)', fontWeight: '800', display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', marginBottom: '24px' }}
            >
                <ChevronLeft size={20} strokeWidth={3} /> 뒤로가기
            </button>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '24px' }}>
                <div className="toss-card" style={{ textAlign: 'center' }}>
                    <div className={`avatar ${callStatus === 'CONNECTED' ? 'pulse-animation' : ''}`} style={{
                        width: '100px',
                        height: '100px',
                        fontSize: '40px',
                        margin: '0 auto 24px',
                        background: callStatus === 'CONNECTED' ? 'var(--success)' : 'var(--primary)'
                    }}>
                        <Phone size={40} />
                    </div>
                    <h2 style={{ fontSize: '28px', fontWeight: '800', marginBottom: '8px' }}>{selectedLead.name} 고객님</h2>
                    <p style={{ fontSize: '18px', color: 'var(--text-dim)', fontWeight: '700', marginBottom: '24px' }}>{selectedLead.phone}</p>

                    <div className="badge-container" style={{ display: 'flex', justifyContent: 'center', gap: '8px' }}>
                        <span style={{ background: 'var(--primary-bg)', color: 'var(--primary)', padding: '8px 16px', borderRadius: '20px', fontWeight: '800', fontSize: '13px' }}>
                            {callStatus === 'IDLE' ? '대기' : callStatus === 'CALLING' ? '연결 중' : '통화 중'}
                        </span>
                    </div>
                </div>

                <div className="toss-card">
                    <div style={{ background: '#f2f4f6', padding: '24px', borderRadius: '20px', marginBottom: '32px' }}>
                        <h4 style={{ fontSize: '14px', color: 'var(--primary)', fontWeight: '800', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Mic2 size={16} /> 상담 가이드
                        </h4>
                        <p style={{ fontSize: '17px', fontWeight: '700', color: '#333d4b', lineHeight: '1.6' }}>
                            "안녕하세요 <strong>{selectedLead.name}</strong> 고객님!<br />
                            Continue Bank 상담원입니다. 신청하신 상품 안내를 위해 연락드렸습니다. 잠시 통화 괜찮으실까요?"
                        </p>
                    </div>

                    <div style={{ marginBottom: '40px' }}>
                        <h4 style={{ fontSize: '15px', fontWeight: '800', marginBottom: '16px' }}>상담 결과</h4>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px' }}>
                            {[
                                { id: 'COMPLETED', label: '관심 있음', icon: <CheckCircle2 size={24} />, color: 'var(--success)' },
                                { id: 'REJECTED', label: '의사 없음', icon: <XCircle size={24} />, color: 'var(--danger)' },
                                { id: 'NO_ANSWER', label: '부재중', icon: <Clock size={24} />, color: 'var(--warning)' }
                            ].map(item => (
                                <button
                                    key={item.id}
                                    onClick={() => setOutcome(item.id)}
                                    style={{
                                        padding: '20px',
                                        borderRadius: '20px',
                                        border: outcome === item.id ? `2px solid ${item.color}` : '2px solid transparent',
                                        background: outcome === item.id ? `${item.color}0a` : '#f9fafb',
                                        color: outcome === item.id ? item.color : 'var(--text-dim)',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        alignItems: 'center',
                                        gap: '12px',
                                        transition: 'all 0.2s',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {item.icon}
                                    <span style={{ fontWeight: '800', fontSize: '15px' }}>{item.label}</span>
                                </button>
                            ))}
                        </div>
                    </div>

                    <button
                        className="btn-toss btn-toss-primary"
                        style={{ width: '100%', height: '64px', borderRadius: '24px', fontSize: '18px' }}
                        onClick={submitResult}
                        disabled={callStatus !== 'CONNECTED' || loading}
                    >
                        <Save size={20} />
                        기록하고 통화 종료하기
                    </button>
                </div>
            </div>
        </div>
    );
}

const style = document.createElement('style');
style.textContent = `
  .animate-spin { animation: spin 1s linear infinite; }
  @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
  .pulse-animation { animation: pulse 2s infinite; }
  @keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(49, 130, 246, 0.4); } 70% { box-shadow: 0 0 0 15px rgba(49, 130, 246, 0); } 100% { box-shadow: 0 0 0 0 rgba(49, 130, 246, 0); } }
`;
document.head.appendChild(style);
