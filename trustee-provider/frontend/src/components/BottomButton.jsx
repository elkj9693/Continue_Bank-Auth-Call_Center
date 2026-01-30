import React from 'react';

const BottomButton = ({ children, onClick, disabled = false, type = 'button' }) => {
  return (
    <div className="fixed bottom-0 left-1/2 -translate-x-1/2 max-w-[450px] w-full p-4 bg-gradient-to-t from-white via-white to-transparent z-20">
      <button
        type={type}
        onClick={onClick}
        disabled={disabled}
        className={`w-full py-4 rounded-2xl text-lg font-bold shadow-lg active:scale-95 transition-transform duration-200
                   ${disabled ? 'bg-gray-300 text-gray-500 cursor-not-allowed' : 'bg-gradient-to-r from-[#E50914] to-[#B71C1C] text-white shadow-red-100'}`}
      >
        {children}
      </button>
    </div>
  );
};

export default BottomButton;
