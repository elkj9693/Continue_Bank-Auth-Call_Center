import { useState, useEffect } from 'react';
import { History, Search, User, Phone, Calendar, Clock, CheckCircle2, XCircle, AlertCircle } from "lucide-react";

export default function AuditView() {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");

    useEffect(() => {
        fetchHistory();
    }, []);

    const fetchHistory = async () => {
        setLoading(true);
        try {
            const res = await fetch('/api/v1/outbound/history');
            if (res.ok) {
                const data = await res.json();
                // Sort by contactedAt desc if available, otherwise by logic
                const sorted = data.sort((a, b) => new Date(b.contactedAt || b.createdAt) - new Date(a.contactedAt || a.createdAt));
                setLogs(sorted);
            }
        } catch (e) {
            console.error("Failed to fetch history", e);
        } finally {
            setLoading(false);
        }
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case 'COMPLETED':
                return { label: '상담 성공', color: 'var(--success)', bg: 'rgba(0, 208, 130, 0.1)', icon: <CheckCircle2 size={14} /> };
            case 'REJECTED':
                return { label: '거절/실패', color: 'var(--danger)', bg: 'rgba(240, 68, 82, 0.1)', icon: <XCircle size={14} /> };
            case 'NO_ANSWER':
                return { label: '부재중', color: 'var(--warning)', bg: 'rgba(255, 187, 0, 0.1)', icon: <Clock size={14} /> };
            default:
                return { label: status, color: 'var(--text-dim)', bg: '#f2f4f6', icon: <AlertCircle size={14} /> };
        }
    };

    const filteredLogs = logs.filter(log =>
        log.name?.includes(searchTerm) || log.phone?.includes(searchTerm)
    );

    return (
        <div className="animate-up">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '24px' }}>
                <div>
                    <h2 style={{ fontSize: '24px', fontWeight: '800', marginBottom: '8px' }}>상담 이력</h2>
                    <p style={{ color: 'var(--text-dim)', fontWeight: '600' }}>
                        총 {logs.length}건의 상담 기록이 있습니다.
                    </p>
                </div>
                <div style={{ position: 'relative', width: '300px' }}>
                    <Search size={18} style={{ position: 'absolute', left: '16px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dim)' }} />
                    <input
                        className="input"
                        placeholder="이름 또는 전화번호 검색"
                        style={{ paddingLeft: '44px', paddingRight: '16px', borderRadius: '20px' }}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '100px 0', color: 'var(--primary)', fontWeight: '800' }}>
                    데이터를 불러오고 있습니다...
                </div>
            ) : filteredLogs.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    {filteredLogs.map((log) => {
                        const badge = getStatusBadge(log.status);
                        return (
                            <div key={log.leadId} className="toss-card" style={{ padding: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                                    <div className="avatar" style={{ background: '#f2f4f6', color: '#191f28', width: '48px', height: '48px', fontSize: '18px', borderRadius: '18px' }}>
                                        {log.name ? log.name.charAt(0) : '?'}
                                    </div>
                                    <div>
                                        <div style={{ fontSize: '18px', fontWeight: '800', marginBottom: '4px' }}>{log.name}</div>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '14px', color: 'var(--text-dim)', fontWeight: '500' }}>
                                            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}><Phone size={14} /> {log.phone}</span>
                                            <span style={{ width: '1px', height: '10px', background: '#d1d6db' }}></span>
                                            <span>{log.requestedProductType}</span>
                                        </div>
                                    </div>
                                </div>

                                <div style={{ textAlign: 'right' }}>
                                    <div style={{
                                        display: 'inline-flex',
                                        alignItems: 'center',
                                        gap: '6px',
                                        padding: '8px 12px',
                                        borderRadius: '12px',
                                        background: badge.bg,
                                        color: badge.color,
                                        fontWeight: '800',
                                        fontSize: '14px',
                                        marginBottom: '8px'
                                    }}>
                                        {badge.icon}
                                        {badge.label}
                                    </div>
                                    <div style={{ fontSize: '13px', color: 'var(--text-dim)', fontWeight: '600' }}>
                                        {log.contactedAt ? new Date(log.contactedAt).toLocaleString() : '일시 정보 없음'}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            ) : (
                <div className="toss-card" style={{ textAlign: 'center', padding: '80px 0' }}>
                    <div style={{ fontSize: '40px', marginBottom: '16px', opacity: 0.5 }}>📭</div>
                    <h3 style={{ fontSize: '18px', fontWeight: '800', marginBottom: '8px' }}>상담 이력이 없습니다</h3>
                    <p style={{ color: 'var(--text-dim)' }}>{searchTerm ? '검색 결과가 없습니다.' : '아직 완료된 상담 내역이 없네요.'}</p>
                </div>
            )}
        </div>
    );
}
