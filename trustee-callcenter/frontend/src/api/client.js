export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('operator_token');
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('operator_token', token);
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem('operator_token');
    }

    async request(endpoint, method = 'GET', body = null) {
        const headers = {
            'Content-Type': 'application/json',
        };
        if (this.token) {
            headers['X-Operator-Token'] = this.token;
        }

        const config = {
            method,
            headers,
        };
        if (body) {
            config.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
            if (!response.ok) {
                if (response.status === 401) {
                    // Token expired logic could go here
                    console.warn('Unauthorized');
                }
                throw new Error(`API Error: ${response.status}`);
            }
            // Handle void response (200 OK but empty)
            const text = await response.text();
            return text ? JSON.parse(text) : {};
        } catch (error) {
            console.error('Request Failed:', error);
            throw error;
        }
    }

    post(endpoint, body) {
        return this.request(endpoint, 'POST', body);
    }

    get(endpoint) {
        return this.request(endpoint, 'GET');
    }
}

export const api = new ApiClient();
