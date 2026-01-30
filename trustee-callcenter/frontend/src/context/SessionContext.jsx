import React, { createContext, useContext, useState } from 'react';

const SessionContext = createContext(null);

export const SessionProvider = ({ children }) => {
    const [session, setSession] = useState({
        callId: null,
        customer: null,
        authTxId: null,
        isVerified: false,
    });

    const startSession = () => {
        setSession({
            callId: crypto.randomUUID(),
            customer: null,
            authTxId: null,
            isVerified: false
        });
    };

    const updateSession = (data) => {
        setSession(prev => ({ ...prev, ...data }));
    };

    const clearSession = () => {
        setSession({
            callId: null,
            customer: null,
            authTxId: null,
            isVerified: false
        });
    };

    return (
        <SessionContext.Provider value={{ session, startSession, updateSession, clearSession }}>
            {children}
        </SessionContext.Provider>
    );
};

export const useSession = () => useContext(SessionContext);
