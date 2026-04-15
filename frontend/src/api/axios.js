import axios from 'axios';
import.meta.env.VITE_API_URL
const API_URL = import.meta.env.VITE_API_URL;
// Создаем базовый экземпляр axios
const api = axios.create({
    baseURL: API_URL, // Твой C# сервер
});

// Этот код будет срабатывать ПЕРЕД каждым запросом к C#
api.interceptors.request.use(
    (config) => {
        // Достаем токен из памяти браузера
        const token = localStorage.getItem('token');
        
        // Если токен есть, прикрепляем его как пропуск (Bearer)
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;