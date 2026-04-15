import React, { useState, useEffect } from 'react';
import { useParams, Link as RouterLink, useNavigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import {
    Box, Container, Avatar, Typography, Button, Grid, Paper, Divider, Tooltip, IconButton, CircularProgress
} from '@mui/material';
import { Edit, Email, PersonAdd, PersonRemove, Block, VolumeOff, Handshake, ArrowBack } from '@mui/icons-material';
import theme from '../../theme';
import api from '../../api/axios'; 
import SettingsModal from '../../Components/SettingsModal';

export default function Show() {
    const { username } = useParams(); // Берем ник из URL /u/:username
    const navigate = useNavigate();

    // Получаем текущего юзера из localStorage (как в Messenger)
    const userString = localStorage.getItem('user');
    const authUser = userString ? JSON.parse(userString) : null;

    const [profileUser, setProfileUser] = useState(null);
    const [interactions, setInteractions] = useState(null);
    const [loading, setLoading] = useState(true);
    const [settingsOpen, setSettingsOpen] = useState(false);

    const isMyProfile = authUser && profileUser && authUser.username === profileUser.username;

    // Загрузка данных профиля
    useEffect(() => {
        setLoading(true);
        api.get(`/users/profile/${username}`)
            .then(res => {
                setProfileUser(res.data.user);
                setInteractions(res.data.interactions);
                setLoading(false);
            })
            .catch(err => {
                console.error("Профиль не найден", err);
                setLoading(false);
            });
    }, [username]);

    const handleFollow = async () => {
        try {
            const res = await api.post(`/users/${profileUser.id}/follow`);
            setInteractions(prev => ({ ...prev, isFollowing: res.data.is_following }));
        } catch (e) { console.error(e); }
    };

    const handleBlock = async () => {
        try {
            const res = await api.post(`/users/${profileUser.id}/block`);
            setInteractions(prev => ({ ...prev, isBlocking: res.data.is_blocked }));
        } catch (e) { console.error(e); }
    };

    if (loading) return (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', bgcolor: '#0f172a' }}>
            <CircularProgress color="primary" />
        </Box>
    );

    if (!profileUser) return <Typography color="white" align="center" sx={{ mt: 5 }}>Пользователь не найден</Typography>;

    return (
        <ThemeProvider theme={theme}>
            <Box sx={{ minHeight: '100vh', bgcolor: '#0f172a', pb: 10 }}>
                {/* Шапка страницы */}
                <Box sx={{ display: 'flex', alignItems: 'center', px: 4, py: 2, bgcolor: '#1e293b', borderBottom: '1px solid rgba(255,255,255,0.05)', position: 'sticky', top: 0, zIndex: 10 }}>
                    <IconButton onClick={() => navigate(-1)} sx={{ mr: 2, color: '#94a3b8', '&:hover': { color: 'white' } }}>
                        <ArrowBack />
                    </IconButton>
                    <Typography variant="h6" sx={{ color: 'white', fontWeight: 'bold' }}>
                        Профиль {profileUser.name}
                    </Typography>
                </Box>

                <Container maxWidth="md" sx={{ mt: 4 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', pt: 5, px: 4, pb: 5, bgcolor: '#1e293b', borderRadius: 4, border: '1px solid rgba(255,255,255,0.05)', mb: 6 }}>
                        <Avatar
                            src={profileUser.avatar}
                            sx={{ width: 140, height: 140, bgcolor: '#38bdf8', color: '#0f172a', fontSize: 64, mb: 3, opacity: interactions?.isBlocking ? 0.5 : 1 }}
                        >
                            {profileUser.name[0]}
                        </Avatar>

                        <Typography variant="h4" fontWeight="bold" color={interactions?.isBlocking ? '#64748b' : '#f1f5f9'} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {profileUser.name}
                            {interactions?.isFollowing && interactions?.isFollowedByThem && !isMyProfile && !interactions?.isBlocking && (
                                <Tooltip title="Вы друзья (взаимная подписка)">
                                    <Handshake sx={{ color: '#f59e0b', fontSize: '1.8rem' }} />
                                </Tooltip>
                            )}
                        </Typography>

                        <Typography variant="h6" sx={{ color: interactions?.isBlocking ? '#64748b' : '#38bdf8', opacity: 0.8, mb: 4 }}>
                            @{profileUser.username}
                        </Typography>

                        <Box sx={{ width: '100%', maxWidth: 500 }}>
                            {isMyProfile ? (
                                <Button variant="outlined" startIcon={<Edit />} fullWidth onClick={() => setSettingsOpen(true)} sx={{ borderColor: '#38bdf8', color: '#38bdf8', py: 1.5, textTransform: 'none', fontWeight: 'bold' }}>
                                    Редактировать профиль
                                </Button>
                            ) : interactions?.isBlocking ? (
                                <Button variant="contained" startIcon={<Block />} fullWidth onClick={handleBlock} sx={{ bgcolor: '#ef4444', color: 'white', py: 1.5 }}>
                                    Разблокировать
                                </Button>
                            ) : (
                                <Grid container spacing={2}>
                                    <Grid item xs={6}>
                                        <Button 
                                            variant="contained" 
                                            fullWidth 
                                            disabled={!(interactions?.isFollowing && interactions?.isFollowedByThem)}
                                            onClick={() => {
                                                api.post(`/messages/start/${profileUser.id}`).then(res => navigate(`/messenger/${res.data.chatId}`));
                                            }}
                                            sx={{ py: 1.5, textTransform: 'none', background: interactions?.isFollowing && interactions?.isFollowedByThem ? 'linear-gradient(45deg, #f97316, #f59e0b)' : '#334155' }}
                                        >
                                            Написать
                                        </Button>
                                    </Grid>
                                    <Grid item xs={6}>
                                        <Button variant={interactions?.isFollowing ? "outlined" : "contained"} fullWidth onClick={handleFollow} sx={{ py: 1.5, textTransform: 'none' }}>
                                            {interactions?.isFollowing ? 'Отписаться' : 'Подписаться'}
                                        </Button>
                                    </Grid>
                                </Grid>
                            )}
                        </Box>
                    </Box>
                    {/* ... дальше твоя Галерея ... */}
                </Container>

                <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} auth={{user: authUser}} onUpdateUser={(newUser) => {
                    localStorage.setItem('user', JSON.stringify(newUser));
                    setProfileUser(newUser); // Обновляем текущий вид, если это наш профиль
                }} />
            </Box>
        </ThemeProvider>
    );
}