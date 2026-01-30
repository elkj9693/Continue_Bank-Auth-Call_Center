import React, { createContext, useContext, useState } from 'react';
import api from '../api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [token, setToken] = useState(localStorage.getItem('operatorToken'));

    const login = async (operatorId, password) => {
        try {
            const res = await api.post('/callcenter/operator/login', { operatorId, password });
            if (res.success && res.data.operatorToken) {
                setToken(res.data.operatorToken);
                localStorage.setItem('operatorToken', res.data.operatorToken);
                return true;
            }
            return false;
        } catch (e) {
            console.error(e);
            return false;
        }
    };

    const logout = () => {
        setToken(null);
        localStorage.removeItem('operatorToken');
    };

    return (
        <AuthContext.Provider value={{ token, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
