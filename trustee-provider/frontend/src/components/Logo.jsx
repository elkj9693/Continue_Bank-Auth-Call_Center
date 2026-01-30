import React, { useState, useEffect } from 'react';

/**
 * SSAP 로고 컴포넌트
 * - PASS 글자가 뒤집혀 SSAP가 되는 애니메이션
 * - Secure System Authentication Pass 태그라인
 */
const Logo = () => {
  const [isFlipped, setIsFlipped] = useState(false);
  const [showTagline, setShowTagline] = useState(false);

  // 로딩 시 애니메이션 실행
  useEffect(() => {
    const flipTimer = setTimeout(() => setIsFlipped(true), 500);
    const taglineTimer = setTimeout(() => setShowTagline(true), 1200);
    return () => {
      clearTimeout(flipTimer);
      clearTimeout(taglineTimer);
    };
  }, []);

  // PASS → SSAP 변환 (뒤집기)
  const originalLetters = ['P', 'A', 'S', 'S'];
  const flippedLetters = ['S', 'S', 'A', 'P']; // PASS를 뒤집으면 SSAP

  return (
    <div className="flex flex-col items-center group select-none">
      {/* 메인 로고 */}
      <div className="flex items-center gap-[3px]">
        {(isFlipped ? flippedLetters : originalLetters).map((char, index) => (
          <div
            key={index}
            className={`
              w-10 h-14 rounded-[8px] flex items-center justify-center
              shadow-[0_4px_16px_rgba(255,59,68,0.25)]
              transition-all duration-500 ease-out
              ${isFlipped 
                ? 'bg-gradient-to-br from-[#FF3B44] to-[#E62E36] scale-100' 
                : 'bg-[#FF3B44] scale-95 opacity-80'
              }
              group-hover:scale-110 group-hover:-translate-y-1
            `}
            style={{ 
              transitionDelay: `${index * 80}ms`,
              transform: isFlipped 
                ? `rotateY(0deg) translateY(0)` 
                : `rotateY(180deg) translateY(4px)`,
            }}
          >
            <span 
              className={`
                text-white text-[28px] font-[900] leading-none tracking-tighter
                transition-all duration-500
                ${isFlipped ? 'opacity-100' : 'opacity-0'}
              `}
              style={{ transitionDelay: `${index * 80 + 200}ms` }}
            >
              {char}
            </span>
          </div>
        ))}
      </div>

      {/* 태그라인 */}
      <div 
        className={`
          mt-3 flex flex-col items-center gap-1
          transition-all duration-500 ease-out
          ${showTagline ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'}
        `}
      >
        <div className="flex items-center gap-2">
          <div className="h-[1px] w-6 bg-gradient-to-r from-transparent to-gray-300"></div>
          <span className="text-[8px] font-black text-[#4B5563] tracking-[0.3em] uppercase">
            Secure System Authentication Pass
          </span>
          <div className="h-[1px] w-6 bg-gradient-to-l from-transparent to-gray-300"></div>
        </div>
      </div>
    </div>
  );
};

export default Logo;
