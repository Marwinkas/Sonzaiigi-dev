import './bootstrap';
import './app.css'; // Подключаем твой Tailwind
import './theme.js';

import React, { useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

// Импортируем твои страницы
import Messenger from './Pages/Messenger';
import Register from './Pages/Auth/Register';
import Login from './Pages/Auth/Login';
import JoinGroup from './Pages/JoinGroup';
import Show from './Pages/Profile/Show';
import { PlayerProvider } from './Components/PlayerContext';
import FloatingPlayer from './Components/FloatingPlayer';
// ✨ КОМПОНЕНТ ЗАЩИТЫ МАРШРУТОВ
// Он оборачивает приватные страницы и проверяет логин
const RequireAuth = ({ children }) => {
    // Проверяем, есть ли токен или данные юзера в хранилище
    const isAuthenticated = localStorage.getItem('token') !== null;

    if (!isAuthenticated) {
        // Если не авторизован, перекидываем на логин (replace убирает страницу из истории браузера)
        return <Navigate to="/login" replace />;
    }

    // Если всё ок — показываем страницу (children)
    return children;
};

// ✨ КОМПОНЕНТ ВЫХОДА
// При заходе на /logout он чистит память и кидает на страницу логина
const Logout = () => {
    useEffect(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        // Если нужно, тут можно еще дернуть API бэкенда: axios.post('/api/auth/logout')
    }, []);

    return <Navigate to="/login" replace />;
};

createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <PlayerProvider>
        <BrowserRouter>
        <FloatingPlayer />
            <Routes>
                {/* 🔒 ПРИВАТНЫЕ МАРШРУТЫ (Обернуты в RequireAuth) */}
                <Route path="/" element={<RequireAuth><Messenger /></RequireAuth>} />
                <Route path="/messenger" element={<RequireAuth><Messenger /></RequireAuth>} />
                <Route path="/messenger/:chatId" element={<RequireAuth><Messenger /></RequireAuth>} />
                <Route path="/join/:token" element={<RequireAuth><JoinGroup /></RequireAuth>} />
                <Route path="/u/:username" element={<RequireAuth><Show /></RequireAuth>} />

                {/* 🌍 ПУБЛИЧНЫЕ МАРШРУТЫ (Доступны всем) */}
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<Login />} />
                
                {/* 🚪 МАРШРУТ ВЫХОДА */}
                <Route path="/logout" element={<Logout />} />
            </Routes>
        </BrowserRouter>
        </PlayerProvider>
    </React.StrictMode>
);