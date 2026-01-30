import React from 'react';

const FloatingInput = ({ label, value, onChange, type = 'text', id, ...props }) => {
  const inputId = id || `floating-input-${label.replace(/\s+/g, '-')}`;

  return (
    <div className="relative pt-6 pb-2 border-b-2 border-gray-200 focus-within:border-[#3182F6] transition-colors">
      <input
        id={inputId}
        type={type}
        value={value}
        onChange={onChange}
        placeholder=" "
        className="w-full bg-transparent outline-none text-xl font-medium text-[#191F28] placeholder-transparent peer"
        {...props}
      />
      <label
        htmlFor={inputId}
        className="absolute left-0 top-6 text-gray-400 text-lg transition-all duration-200 origin-[0]
                   peer-focus:top-0 peer-focus:scale-75 peer-focus:text-[#3182F6]
                   peer-not-placeholder-shown:top-0 peer-not-placeholder-shown:scale-75 peer-not-placeholder-shown:text-[#3182F6]"
      >
        {label}
      </label>
    </div>
  );
};

export default FloatingInput;
