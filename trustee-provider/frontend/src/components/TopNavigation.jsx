import React from 'react';
import { ChevronLeft, X } from 'lucide-react';
import Logo from './Logo';

const TopNavigation = ({ onBack, onClose }) => {
  return (
    <div className="fixed top-0 max-w-[450px] w-full h-14 bg-white flex items-center justify-between px-4 shadow-sm z-10">
      <div className="flex items-center">
        {onBack && <ChevronLeft size={24} className="text-[#191F28] cursor-pointer" onClick={onBack} />}
      </div>
      <div className="absolute left-1/2 -translate-x-1/2">
        <Logo />
      </div>
      {onClose && <X size={24} className="text-[#191F28] cursor-pointer" onClick={onClose} />}
    </div>
  );
};

export default TopNavigation;
