import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Headphones, ShieldCheck, ArrowRight } from "lucide-react";

export default function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const response = await fetch("/api/v1/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ username, password }),
            });

            const data = await response.json();

            if (data.success) {
                // 로그인 성공 시 세션 저장
                sessionStorage.setItem("callcenter-user", data.name);
                navigate("/outbound");
            } else {
                alert(data.message || "아이디 또는 비밀번호가 올바르지 않습니다.");
            }
        } catch (error) {
            console.error("Login Error:", error);
            alert("서버 연결에 실패했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={{
            height: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: '#f2f4f6'
        }}>
            <div className="animate-up" style={{
                width: '100%',
                maxWidth: '460px',
                padding: '56px',
                background: 'white',
                borderRadius: '40px',
                boxShadow: 'var(--shadow-md)'
            }}>
                <div style={{ marginBottom: '48px' }}>
                    <div style={{ color: 'var(--primary)', marginBottom: '24px' }}>
                        <Headphones size={48} strokeWidth={3} />
                    </div>
                    <h1 style={{ fontSize: '32px', fontWeight: '800', letterSpacing: '-1px', marginBottom: '12px' }}>
                        안녕하세요,<br />오늘도 힘내세요!
                    </h1>
                    <p style={{ color: 'var(--text-dim)', fontSize: '17px', fontWeight: '600' }}>
                        Davada 콜센터 시스템에 로그인해 주세요.
                    </p>
                </div>

                <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <input
                        type="text"
                        className="input"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        placeholder="상담원 ID"
                        required
                        disabled={isLoading}
                    />
                    <input
                        type="password"
                        className="input"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="비밀번호"
                        required
                        disabled={isLoading}
                    />

                    <button
                        type="submit"
                        className="btn-toss btn-toss-primary"
                        style={{ height: '64px', borderRadius: '24px', marginTop: '16px', fontSize: '18px' }}
                        disabled={isLoading}
                    >
                        {isLoading ? '확인 중...' : (
                            <>
                                로그인 <ArrowRight size={20} />
                            </>
                        )}
                    </button>

                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', color: '#adb5bd', fontSize: '13px', marginTop: '24px', fontWeight: '600' }}>
                        <ShieldCheck size={16} />
                        보안 서버 연결됨
                    </div>
                </form>
            </div>
        </div>
    );
}
