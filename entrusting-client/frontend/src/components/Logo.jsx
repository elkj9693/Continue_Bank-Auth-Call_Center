import React from 'react';
import { Infinity } from 'lucide-react';

const Logo = ({ className = "", showSlogan = false }) => {
  return (
    <div className={`flex flex-col items-center gap-1 group ${className}`}>
      <div className="flex items-center gap-2.5 whitespace-nowrap">
        <div className="w-10 h-10 bg-gradient-to-br from-[#1A73E8] to-[#0D47A1] rounded-xl flex items-center justify-center shadow-lg shadow-blue-200/50 flex-shrink-0 transition-transform group-hover:scale-105 duration-300">
          <Infinity size={26} className="text-white" />
        </div>
        <span className="text-[21px] font-bold text-[#1A1A1A] tracking-tighter">
          Continue Bank
        </span>
      </div>
      {showSlogan && (
        <div className="text-[13px] font-bold text-[#1A73E8] opacity-90 tracking-tight animate-fade-in">
          당신의 금융은 멈추지 않도록, 보안은 계속됩니다.
        </div>
      )}
    </div>
  );
};

export default Logo;
