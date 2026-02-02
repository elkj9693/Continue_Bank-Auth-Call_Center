import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/ConsultBanner.css';
const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

export default function ConsultBanner() {
    const [showModal, setShowModal] = useState(false);
    const [productType, setProductType] = useState('CARD');
    const navigate = useNavigate();

    const handleApply = async (e) => {
        e.preventDefault();

        // 현재 로그인 사용자 정보 가져오기
        const userStr = sessionStorage.getItem('user');
        if (!userStr) {
            alert('로그인이 필요합니다.');
            navigate('/login');
            return;
        }

        const user = JSON.parse(userStr);

        try {
            const response = await fetch(`${API_BASE}/api/v1/leads`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    customerRef: user.userId,
                    name: user.name,
                    phone: user.phoneNumber,
                    productType: productType
                })
            });

            if (response.ok) {
                alert('상담 신청이 완료되었습니다. 빠른 시일 내에 연락드리겠습니다.');
                setShowModal(false);
            } else {
                alert('상담 신청에 실패했습니다.');
            }
        } catch (error) {
            console.error(error);
            alert('오류가 발생했습니다.');
        }
    };

    return (
        <>
            <div className="consult-banner" onClick={() => setShowModal(true)}>
                <div className="consult-content">
                    <div>
                        <h3>💬 상품 상담 신청하기</h3>
                        <p>전문 상담원이 친절하게 안내해드립니다</p>
                    </div>
                    <button className="consult-btn">신청하기 →</button>
                </div>
            </div>

            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h2>상담 신청</h2>
                        <form onSubmit={handleApply}>
                            <div className="form-group">
                                <label>상품 선택</label>
                                <select value={productType} onChange={(e) => setProductType(e.target.value)}>
                                    <option value="CARD">신용카드</option>
                                    <option value="LOAN">대출</option>
                                    <option value="DEPOSIT">예적금</option>
                                    <option value="INSURANCE">보험</option>
                                </select>
                            </div>

                            <div className="modal-actions">
                                <button type="button" onClick={() => setShowModal(false)} className="btn-cancel">
                                    취소
                                </button>
                                <button type="submit" className="btn-submit">
                                    신청하기
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </>
    );
}
