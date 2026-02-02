import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import OutboundPage from "./pages/OutboundPage";
import AuditView from "./pages/AuditView";
import {
  PhoneCall,
  History,
  LogOut,
  LayoutDashboard,
  Headphones
} from "lucide-react";
import "./index.css"; // Ensure global styles are loaded

function ProtectedLayout({ children }) {
  const user = sessionStorage.getItem("callcenter-user");
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const handleLogout = () => {
    if (window.confirm("로그아웃 하시겠습니까?")) {
      sessionStorage.removeItem("callcenter-user");
      window.location.href = "/login";
    }
  };

  return (
    <div className="app-layout">
      {/* Fixed Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="brand-title">
            <Headphones size={24} strokeWidth={2.5} />
            <span>Davada</span>
          </div>
          <div style={{ fontSize: '12px', color: 'rgba(255,255,255,0.6)', marginTop: '4px', fontWeight: '500' }}>
            Continue Bank 전담팀
          </div>
        </div>

        <nav className="nav-menu">
          <Link
            to="/outbound"
            className={`nav-item ${location.pathname === '/outbound' ? 'active' : ''}`}
          >
            <PhoneCall size={20} />
            <span>상담 하기</span>
          </Link>
          <Link
            to="/logs"
            className={`nav-item ${location.pathname === '/logs' ? 'active' : ''}`}
          >
            <History size={20} />
            <span>상담 내역</span>
          </Link>
          <div style={{ marginTop: 'auto' }}>
            {/* Spacer or additional items */}
          </div>
        </nav>

        <div className="sidebar-footer">
          <div className="agent-profile">
            <div className="avatar">{user.charAt(0).toUpperCase()}</div>
            <div style={{ flex: 1, overflow: 'hidden' }}>
              <div style={{ fontSize: '14px', fontWeight: '700', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user}님</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'var(--success)', fontWeight: '600' }}>
                <div className="status-dot"></div>
                온라인
              </div>
            </div>
            <button
              onClick={handleLogout}
              style={{ background: 'none', border: 'none', color: '#adb5bd', cursor: 'pointer', padding: '4px' }}
              title="로그아웃"
            >
              <LogOut size={18} />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="main-container">
        <header className="top-bar">
          <div className="page-title">
            {location.pathname === '/outbound' ? 'Continue Bank' :
              location.pathname === '/logs' ? '상담 이력' : '대시보드'}
          </div>
          <div style={{ color: 'var(--text-dim)', fontSize: '14px', fontWeight: '600' }}>
            {new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })}
          </div>
        </header>

        <main className="content-body">
          {children}
        </main>
      </div>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/outbound"
          element={
            <ProtectedLayout>
              <OutboundPage />
            </ProtectedLayout>
          }
        />
        <Route
          path="/logs"
          element={
            <ProtectedLayout>
              <AuditView />
            </ProtectedLayout>
          }
        />
        <Route path="/" element={<Navigate to="/outbound" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
