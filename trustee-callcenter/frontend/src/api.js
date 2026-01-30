import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
});

// Request Interceptor to add Operator Token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('operatorToken');
    if (token) {
        config.headers['X-Operator-Token'] = token;
    }
    return config;
});

// Response Interceptor to handle errors
api.interceptors.response.use(
    (response) => response.data,
    (error) => {
        // Logic to handle 401 or global errors could go here
        return Promise.reject(error);
    }
);

export default api;
