import React from 'react';
import { ShieldCheck, RefreshCcw } from 'lucide-react';

const LoadingOverlay = ({ isVisible }) => {
  if (!isVisible) return null;

  return (
    <div className="fixed inset-0 z-[1000] bg-white flex flex-col items-center justify-center animate-in fade-in duration-500 overflow-hidden">
      <style>{`
        @keyframes reverse-move-1 {
          0%, 10% { transform: translateX(0); }
          35%, 90% { transform: translateX(180px); }
          100% { transform: translateX(0); }
        }
        @keyframes reverse-move-2 {
          0%, 10% { transform: translateX(0); }
          35%, 90% { transform: translateX(60px); }
          100% { transform: translateX(0); }
        }
        @keyframes reverse-move-3 {
          0%, 10% { transform: translateX(0); }
          35%, 90% { transform: translateX(-60px); }
          100% { transform: translateX(0); }
        }
        @keyframes reverse-move-4 {
          0%, 10% { transform: translateX(0); }
          35%, 90% { transform: translateX(-180px); }
          100% { transform: translateX(0); }
        }

        .letter-box {
          width: 3.2rem;
          height: 4.2rem;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 12px;
          color: white;
          font-weight: 950;
          font-size: 2rem;
          box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
          background-color: #FF3B44;
          z-index: 10;
        }

        .box-1 { animation: reverse-move-1 4.5s infinite ease-in-out; }
        .box-2 { animation: reverse-move-2 4.5s infinite ease-in-out; }
        .box-3 { animation: reverse-move-3 4.5s infinite ease-in-out; }
        .box-4 { animation: reverse-move-4 4.5s infinite ease-in-out; }

        .connection-line {
          position: absolute;
          height: 2px;
          background: linear-gradient(to right, #FF3B44, #1A1A1A);
          opacity: 0.1;
          z-index: 5;
        }

        @keyframes pulse-text {
          0%, 20% { opacity: 0.5; transform: scale(1); }
          50%, 80% { opacity: 1; transform: scale(1.1); }
          100% { opacity: 0.5; transform: scale(1); }
        }
        .status-text {
          animation: pulse-text 5s infinite ease-in-out;
        }
      `}</style>

      {/* Hero Icon */}
      <div className="relative mb-16">
        <div className="absolute inset-0 w-32 h-32 border-2 border-red-50 rounded-full animate-ping opacity-30"></div>
        <div className="w-32 h-32 border-t-4 border-r-4 border-[#E50914] rounded-full animate-spin [animation-duration:2s]"></div>
        <div className="absolute inset-0 flex items-center justify-center">
          <ShieldCheck size={54} className="text-[#E50914]" />
        </div>
      </div>

      {/* Main Animation Stage */}
      <div className="flex flex-col items-center space-y-12">
        <div className="flex gap-4 relative py-8 px-12 bg-gray-50/50 rounded-3xl">
          <div className="letter-box box-1">P</div>
          <div className="letter-box box-2">A</div>
          <div className="letter-box box-3">S</div>
          <div className="letter-box box-4">S</div>
          
          {/* Crossing Guideline Effect (Optional Visual) */}
          <div className="absolute top-1/2 left-0 w-full h-[1px] bg-gray-200 -z-0 opacity-50"></div>
        </div>

        {/* Status Section */}
        <div className="flex flex-col items-center space-y-4">

          
          <div className="flex flex-col items-center">
            <h3 className="text-2xl font-black text-[#E50914] tracking-wider mb-2">SSAP</h3>
            <p className="text-[13px] font-black text-gray-600 uppercase tracking-[0.15em] mb-6 bg-gray-100 px-4 py-1 rounded-full">
              Secure System Authentication Pass
            </p>
            <p className="text-[17px] font-bold text-gray-700">안전하게 본인인증을 진행하고 있습니다</p>
            <p className="text-sm text-gray-400 font-medium mt-1">
              잠시만 기다려 주세요
            </p>
          </div>
        </div>
      </div>

      {/* Decorative Footer */}
      <div className="absolute bottom-16 w-full flex flex-col items-center gap-4">
        <div className="flex items-center gap-8">
          <div className="h-[1px] w-16 bg-gradient-to-r from-transparent to-red-500 opacity-20"></div>
          <span className="text-[10px] font-black tracking-[0.5em] text-red-900/40 uppercase">Sequence Reversal System</span>
          <div className="h-[1px] w-16 bg-gradient-to-l from-transparent to-red-500 opacity-20"></div>
        </div>
      </div>
    </div>
  );
};

export default LoadingOverlay;
