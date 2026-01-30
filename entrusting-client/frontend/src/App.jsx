import { Routes, Route, Navigate } from 'react-router-dom';
import Register from './pages/Register';
import Login from './pages/Login';
import AuthCallback from './pages/AuthCallback';
import Dashboard from './pages/Dashboard';
import ForgotPassword from './pages/ForgotPassword';
import FindId from './pages/FindId';
import CreateAccount from './pages/CreateAccount';
import SignupFlow from './pages/SignupFlow';
import AccountVerification from './pages/AccountVerification';
import AuthBridge from './pages/AuthBridge';
import MyPage from './pages/MyPage';

const ProtectedRoute = ({ children }) => {
  const isLoggedIn = sessionStorage.getItem('logged_in_user');
  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

function App() {
  return (
    <div className="min-h-screen bg-[#F5F6F8] flex justify-center">
      <div className="w-full max-w-[480px] min-h-screen bg-white shadow-xl shadow-gray-200/50">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/signup" element={<SignupFlow />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/find-id" element={<FindId />} />
          <Route path="/auth/callback" element={<AuthCallback />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/mypage"
            element={
              <ProtectedRoute>
                <MyPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/create-account"
            element={
              <ProtectedRoute>
                <CreateAccount />
              </ProtectedRoute>
            }
          />
          <Route
            path="/account-verification"
            element={
              <ProtectedRoute>
                <AccountVerification />
              </ProtectedRoute>
            }
          />
          <Route path="/auth/bridge" element={<AuthBridge />} />
          <Route path="/" element={<Navigate to="/login" replace />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
