import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import {
    Box, Avatar, Typography, Button, Dialog, DialogContent, IconButton, Tooltip, Grid
} from '@mui/material';
import { Edit, Email, PersonAdd, PersonRemove, Block, VolumeOff, Close, Handshake, OpenInNew } from '@mui/icons-material';
import theme from '../theme';
import api from '../api/axios'; // ВАЖНО: используем наш настроенный API с токеном

export default function ProfileModal({ open, onClose, profileUser, onOpenSettings, auth }) {
    const navigate = useNavigate();
    const isMyProfile = auth?.user && profileUser && auth.user.username === profileUser.username;
    const [loading, setLoading] = useState(false);
    
    const [isFollowing, setIsFollowing] = useState(false);
    const [isFollowedByThem, setIsFollowedByThem] = useState(false);
    const [isMuted, setIsMuted] = useState(false);
    const [isBlocking, setIsBlocking] = useState(false);

    // 1. ЗАПРАШИВАЕМ РЕАЛЬНЫЕ ДАННЫЕ ПРИ ОТКРЫТИИ
    useEffect(() => {
        if (open && profileUser?.id && !isMyProfile) {
            api.get(`/users/${profileUser.id}/relations`).then(res => {
                setIsFollowing(res.data.is_following);
                setIsFollowedByThem(res.data.is_mutual ? res.data.is_following : false); 
                // Заглушки, если сервер пока это не возвращает:
                setIsBlocking(res.data.is_blocking || false);
                setIsMuted(res.data.is_muted || false);
            }).catch(err => console.error("Ошибка загрузки статуса", err));
        }
    }, [open, profileUser?.id]);

    const isMutual = isFollowing && isFollowedByThem;

    const getFullAvatarUrl = (path) => {
        if (!path) return null;
        if (path.startsWith('http') || path.startsWith('data:')) return path;
        const cleanPath = path.replace(/^\/?storage\//, '').replace(/^\//, '');
        return `https://cdn.sonzaiigi.com/${cleanPath}`;
    };

    const handleFollow = async () => {
        if (loading) return;
        setLoading(true);

        const oldFollowing = isFollowing;
        setIsFollowing(!oldFollowing); // Мгновенный отклик UI

        try {
            const res = await api.post(`/users/${profileUser.id}/follow`);
            
            // Сервер прислал точный ответ:
            setIsFollowing(res.data.is_following);
            
            // Если взаимно, значит он на нас тоже подписан
            if (res.data.is_mutual) {
                setIsFollowedByThem(true);
            } else {
                // Если мы отписались, взаимность пропадает
                setIsFollowedByThem(false); 
            }
        } catch (error) {
            setIsFollowing(oldFollowing); // Возвращаем кнопку при 404 или ошибке
            console.error("Ошибка при подписке:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleMute = async () => {
        setIsMuted(!isMuted);
        try {
            await api.post(`/users/${profileUser.id}/mute`);
        } catch (error) {
            setIsMuted(!isMuted);
        }
    };

const handleBlock = async () => {
        const newBlockState = !isBlocking;
        setIsBlocking(newBlockState);
        
        // Если мы блокируем, бекенд разорвет связи, поэтому сбрасываем UI
        if (newBlockState) {
            setIsFollowing(false);
            setIsFollowedByThem(false);
        }

        try {
            await api.post(`/users/${profileUser.id}/block`);
        } catch (error) {
            // В случае ошибки откатываем статус блока обратно
            setIsBlocking(!newBlockState);
            console.error("Ошибка при блокировке:", error);
        }
    };

    const handleMessageStart = async () => {
        try {
            const res = await api.post(`/messages/start/${profileUser.id}`);
            if (onClose) onClose();
            navigate(`/messenger/${res.data.chatId}`);
        } catch (error) {
            console.error("Ошибка при создании чата", error);
        }
    };

    if (!profileUser) return null;

    // Немного увеличили кнопки для удобного нажатия пальцем на телефоне
    const commonButtonStyles = {
        height: '44px', borderRadius: 1.5, textTransform: 'none', fontWeight: 'bold',
        fontSize: '0.85rem', boxShadow: 'none', display: 'flex', alignItems: 'center', 
        justifyContent: 'center', transition: 'all 0.2s', width: '100%'
    };

    return (
        <ThemeProvider theme={theme}>
            <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth PaperProps={{ sx: { maxWidth: "500px", borderRadius: 4, bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.1)', backgroundImage: 'none', overflow: 'hidden', margin: { xs: 2, sm: 4 } } }}>
                <Box sx={{ display: 'flex', alignItems: 'center', px: 3, py: 2, bgcolor: '#1e293b', borderBottom: '1px solid rgba(255,255,255,0.05)', position: 'sticky', top: 0, zIndex: 10 }}>
                    <Typography variant="h6" sx={{ color: 'white', fontWeight: 'bold', flex: 1 }}>Профиль</Typography>
                    <IconButton onClick={onClose} sx={{ color: '#94a3b8', '&:hover': { color: 'white' } }}><Close /></IconButton>
                </Box>

                <DialogContent sx={{ p: 0, bgcolor: '#0f172a' }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', pt: 4, px: { xs: 2, sm: 4 }, pb: 4, bgcolor: '#1e293b', mb: 1 }}>
                        
                        <Avatar 
                            src={getFullAvatarUrl(profileUser.avatar)} 
                            sx={{ width: 100, height: 100, bgcolor: '#38bdf8', color: '#0f172a', fontSize: 40, mb: 2, opacity: isBlocking ? 0.5 : 1 }}
                        >
                            {profileUser?.name?.[0] || profileUser?.username?.[0] || '?'}
                        </Avatar>
                        <Typography variant="h5" fontWeight="bold" color={isBlocking ? '#64748b' : '#f1f5f9'} sx={{ display: 'flex', alignItems: 'center', gap: 1, textAlign: 'center' }}>
                            {profileUser.name}
                            {isMutual && !isMyProfile && !isBlocking && (
                                <Tooltip title="Вы друзья (взаимная подписка)"><Handshake sx={{ color: '#f59e0b', fontSize: '1.2rem' }} /></Tooltip>
                            )}
                        </Typography>

                        <Typography component={Link} to={`/u/${profileUser.username}`} onClick={onClose} variant="body1" sx={{ color: isBlocking ? '#64748b' : '#38bdf8', opacity: 0.8, mb: 3, textDecoration: 'none', transition: '0.2s', '&:hover': { opacity: 1, textDecoration: 'underline' } }}>
                            @{profileUser.username}
                        </Typography>

                        <Box sx={{ width: '100%', mb: 2 }}>
                            {isMyProfile ? (
                                <Button variant="outlined" fullWidth onClick={() => { onClose(); if (onOpenSettings) onOpenSettings(); }} sx={{ ...commonButtonStyles, borderColor: '#38bdf8', color: '#38bdf8', '&:hover': { bgcolor: 'rgba(56, 189, 248, 0.1)' } }}>
                                    <Edit sx={{ fontSize: '1.2rem', mr: 1 }} /> Редактировать
                                </Button>
                            ) : isBlocking ? (
                                <Button variant="contained" fullWidth onClick={handleBlock} sx={{ ...commonButtonStyles, bgcolor: '#ef4444', color: 'white', '&:hover': { bgcolor: '#dc2626' } }}>
                                    <Block sx={{ fontSize: '1.2rem', mr: 1 }} /> Разблокировать
                                </Button>
                            ) : (
                                /* Сетка кнопок: на телефоне (xs) занимает половину ширины (6), на ПК (sm) — четверть (3) */
                                <Grid container spacing={1.5}>
                                    <Grid item xs={6} sm={3}>
                                        <Tooltip title={!isMutual ? "Вы сможете написать, когда подписка станет взаимной" : ""}>
                                            <span>
                                                <Button variant="contained" disabled={!isMutual} onClick={handleMessageStart} sx={{ ...commonButtonStyles, bgcolor: isMutual ? '#f97316' : '#334155', color: isMutual ? 'white' : '#94a3b8', '&:hover': { bgcolor: isMutual ? '#ea580c' : '#334155' }, '&.Mui-disabled': { bgcolor: '#334155', color: '#64748b' } }}>
                                                    <Email sx={{ fontSize: '1.1rem', mr: 0.5 }} /> Чат
                                                </Button>
                                            </span>
                                        </Tooltip>
                                    </Grid>
                                    
                                    <Grid item xs={6} sm={3}>
                                        <Button variant={isFollowing ? "outlined" : "contained"} onClick={handleFollow} sx={{ ...commonButtonStyles, bgcolor: isFollowing ? 'transparent' : '#38bdf8', color: isFollowing ? '#94a3b8' : '#0f172a', borderColor: isFollowing ? '#64748b' : 'transparent', '&:hover': { bgcolor: isFollowing ? 'rgba(100, 116, 139, 0.1)' : '#0ea5e9' } }}>
                                            {isFollowing ? <PersonRemove sx={{ fontSize: '1.1rem', mr: 0.5 }} /> : <PersonAdd sx={{ fontSize: '1.1rem', mr: 0.5 }} />}
                                            {isFollowing ? 'Отписка' : 'Подписка'}
                                        </Button>
                                    </Grid>

                                    <Grid item xs={6} sm={3}>
                                        <Button variant={isMuted ? "contained" : "outlined"} onClick={handleMute} sx={{ ...commonButtonStyles, borderColor: '#64748b', color: isMuted ? 'white' : '#94a3b8', bgcolor: isMuted ? '#64748b' : 'transparent', '&:hover': { bgcolor: isMuted ? '#475569' : 'rgba(100, 116, 139, 0.1)' } }}>
                                            <VolumeOff sx={{ fontSize: '1.1rem', mr: 0.5 }} /> {isMuted ? 'Снять мут' : 'Мут'}
                                        </Button>
                                    </Grid>

                                    <Grid item xs={6} sm={3}>
                                        <Button variant="outlined" onClick={handleBlock} sx={{ ...commonButtonStyles, borderColor: '#ef4444', color: '#ef4444', '&:hover': { bgcolor: 'rgba(239, 68, 68, 0.1)' } }}>
                                            <Block sx={{ fontSize: '1.1rem', mr: 0.5 }} /> Блок
                                        </Button>
                                    </Grid>
                                </Grid>
                            )}
                        </Box>

                        <Button component={Link} to={`/u/${profileUser.username}`} onClick={onClose} endIcon={<OpenInNew />} sx={{ textTransform: 'none', color: '#94a3b8', mt: 1, '&:hover': { color: '#38bdf8', bgcolor: 'transparent' } }}>
                            Посмотреть профиль
                        </Button>
                    </Box>
                </DialogContent>
            </Dialog>
        </ThemeProvider>
    );
}