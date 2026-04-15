import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios'; // Наш настроенный клиент с токеном
import { Box, CircularProgress, Typography, Button } from '@mui/material';

export default function JoinGroup() {
    // Достаем токен прямо из URL (react-router-dom сам его вырежет)
    const { token } = useParams();
    const navigate = useNavigate();
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!token) return;

        // Отправляем токен на C#
        api.post(`/groups/join/${token}`)
            .then(res => {
                // Если всё отлично, сервер нас добавил. Перекидываем в мессенджер!
                navigate('/messenger');
            })
            .catch(err => {
                console.error('Ошибка входа в группу:', err);
                if (err.response?.status === 401) {
                    setError('Сначала нужно войти в аккаунт.');
                } else {
                    setError('Не удалось найти группу. Возможно, ссылка устарела.');
                }
            });
    }, [token, navigate]);

    return (
        <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', bgcolor: '#0f172a', color: 'white' }}>
            {error ? (
                <>
                    <Typography color="error" variant="h6" mb={3}>{error}</Typography>
                    <Button variant="outlined" onClick={() => navigate('/login')} sx={{ color: '#38bdf8', borderColor: '#38bdf8' }}>
                        Войти или вернуться
                    </Button>
                </>
            ) : (
                <>
                    <CircularProgress sx={{ color: '#38bdf8', mb: 3 }} />
                    <Typography variant="h6">Присоединяемся к группе...</Typography>
                </>
            )}
        </Box>
    );
}