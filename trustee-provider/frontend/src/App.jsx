import React, { useState, useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import IdentityVerification from './pages/IdentityVerification';
import OtpInput from './pages/OtpInput';
import LoadingOverlay from './components/LoadingOverlay';

function App() {
  const [isTransitioning, setIsTransitioning] = useState(true);

  useEffect(() => {
    // Simulate security connection delay when entering SSAP
    const timer = setTimeout(() => {
      setIsTransitioning(false);
    }, 3000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="min-h-screen bg-[#F5F6F8] flex justify-center relative">
      <LoadingOverlay isVisible={isTransitioning} />
      
      <div className="w-full max-w-[480px] min-h-screen bg-white shadow-xl shadow-gray-200/50">
        <Routes>
          <Route path="/" element={<IdentityVerification />} />
          <Route path="/verify" element={<IdentityVerification />} />
          <Route path="/verify/otp" element={<OtpInput />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
