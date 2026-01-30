import React from 'react';
import { X, AlertCircle } from 'lucide-react';

const AlertModal = ({ isOpen, onClose, title, message, type = 'error' }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[999] flex items-center justify-center p-6">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/40 backdrop-blur-sm animate-in fade-in duration-300"
        onClick={onClose}
      />
      
      {/* Modal Content */}
      <div className="relative w-full max-w-[400px] bg-white rounded-3xl shadow-2xl overflow-hidden animate-in zoom-in-95 fade-in duration-300">
        <div className="p-8 pb-6">
          <div className="flex justify-between items-start mb-6">
            <div className={`w-14 h-14 rounded-2xl flex items-center justify-center ${
              type === 'error' ? 'bg-red-50 text-red-500' : 'bg-blue-50 text-[#1A73E8]'
            }`}>
              <AlertCircle size={32} />
            </div>
            <button 
              onClick={onClose}
              className="p-2 -mr-2 rounded-full hover:bg-gray-100 transition-colors"
            >
              <X size={24} className="text-gray-400" />
            </button>
          </div>
          
          <h3 className="text-[22px] font-bold text-gray-900 mb-3 leading-tight">
            {title || (type === 'error' ? '알림' : '안내')}
          </h3>
          <p className="text-gray-500 font-medium leading-relaxed whitespace-pre-line">
            {message}
          </p>
        </div>
        
        <div className="p-8 pt-0">
          <button
            onClick={onClose}
            className="w-full h-14 bg-gray-900 text-white rounded-2xl font-bold text-base hover:bg-gray-800 active:scale-95 transition-all shadow-lg shadow-gray-200"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
};

export default AlertModal;
