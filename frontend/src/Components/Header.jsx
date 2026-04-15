import React, { useState } from 'react';
// Убрали Inertia: import { Head, Link as InertiaLink, usePage, router } from '@inertiajs/react';
import { Link } from 'react-router-dom'; // ИСПОЛЬЗУЕМ REACT ROUTER
import {
    Box, Container, Avatar, IconButton, Typography,
} from '@mui/material';
import Badge from '@mui/material/Badge';
import Tooltip from '@mui/material/Tooltip';
import InputBase from '@mui/material/InputBase';
import { styled, alpha } from '@mui/material/styles';
import Button from '@mui/material/Button';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import Divider from '@mui/material/Divider';
import PersonAdd from '@mui/icons-material/PersonAdd';
import Settings from '@mui/icons-material/Settings';
import Logout from '@mui/icons-material/Logout';
import AddIcon from '@mui/icons-material/Add';
import SearchIcon from '@mui/icons-material/Search';

import Search from './SearchWithButton';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DashboardIcon from '@mui/icons-material/Dashboard';
import PersonIcon from '@mui/icons-material/Person';
import FavoriteIcon from '@mui/icons-material/Favorite';
import HistoryIcon from '@mui/icons-material/History';
import Notification from './NotificationBellю';
import HeaderLogo from './HeaderLogo';
import SettingsModal from './SettingsModal';
import ProfileModal from './ProfileModal';
import { useNavigate } from 'react-router-dom';
// ДОБАВИЛИ auth В ПРОПСЫ
export default function Header({ color, auth }) {
    const [anchorEl, setAnchorEl] = React.useState(null);
    
    const [settingsOpen, setSettingsOpen] = React.useState(false);
    const open = Boolean(anchorEl);

    const handleClick = (event) => setAnchorEl(event.currentTarget);
    const handleClose = () => setAnchorEl(null);

    const [selectedUser, setSelectedUser] = useState(null);
    const navigate = useNavigate();

    const handleLogout = () => {
        // Удаляем данные из памяти браузера
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        
        // Закрываем меню и перекидываем на логин
        handleClose();
        navigate('/login');
    };


    const getFullAvatarUrl = (path) => {
        if (!path) return null;
        if (path.startsWith('http') || path.startsWith('data:')) return path;
        const cleanPath = path.replace(/^\/?storage\//, '').replace(/^\//, '');
        return `https://cdn.sonzaiigi.com/${cleanPath}`;
    };
    
    return (
        <Box sx={{ position: 'sticky', top: 0, zIndex: 10, backdropFilter: 'blur(10px)', bgcolor: `hsla(${224 + color}, 71%, 4%, 0.80)` }}>
            <Container maxWidth="100%" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', height: "56px" }}>
                <HeaderLogo color={color} />
                <Search color={color} />

                {auth?.user ? (
                    <Box sx={{ display: "flex", alignItems: "center" }}>
                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            sx={{
                                background: `linear-gradient(45deg, hsl(${210 + color}, 100%, 60%) 30%, hsl(${186 + color}, 100%, 50%) 90%)`,
                                color: '#040B14', fontWeight: 'bold', borderRadius: '20px', padding: '8px 24px', textTransform: 'none',
                                border: `1px solid hsla(${0 + color}, 0%, 100%, 0.20)`, boxShadow: `0 0 10px hsla(${210 + color}, 100%, 60%, 0.50)`,
                                transition: '0.3s', '&:hover': { boxShadow: `0 0 20px hsla(${210 + color}, 100%, 60%, 0.80)` }
                            }}
                        >
                            Create
                        </Button>
                        <Notification color={color} />
                        <Tooltip title="Account">
                            <IconButton onClick={handleClick} size="small" aria-controls={open ? 'account-menu' : undefined} aria-haspopup="true" aria-expanded={open ? 'true' : undefined} sx={{ width: "40px", height: "40px", transition: '0.3s', '&:hover': { filter: `drop-shadow(0 0 8px hsla(${210 + color}, 100%, 60%, 0.60))` } }}>
<Avatar
                                    src={getFullAvatarUrl(auth.user.avatar)}
                                    sx={{ width: 32, height: 32, border: `2px solid hsl(${210 + color}, 100%, 60%)`, bgcolor: `hsl(${211 + color}, 61%, 10%)` }}
                                >
                                    {auth.user.name[0]}
                                </Avatar>
                            </IconButton>
                        </Tooltip>

                        <Menu anchorEl={anchorEl} id="account-menu" open={open} onClose={handleClose} onClick={handleClose} TransitionProps={{ timeout: 200 }} slotProps={{
                            paper: {
                                elevation: 0,
                                sx: {
                                    overflow: 'visible', backgroundColor: `hsla(${211 + color}, 61%, 10%, 0.95)`, backdropFilter: 'blur(10px)',
                                    border: `1px solid hsla(${210 + color}, 100%, 60%, 0.20)`, boxShadow: '0 4px 20px rgba(0,0,0,0.5)',
                                    mt: 1.5, color: '#F3F6F9', minWidth: '200px',
                                    '&::before': { content: '""', display: 'block', position: 'absolute', top: 0, right: 14, width: 10, height: 10, bgcolor: `hsl(${211 + color}, 61%, 10%)`, borderTop: `1px solid hsla(${210 + color}, 100%, 60%, 0.20)`, borderLeft: `1px solid hsla(${210 + color}, 100%, 60%, 0.20)`, transform: 'translateY(-50%) rotate(45deg)', zIndex: 0 },
                                    '& .MuiMenuItem-root': { fontSize: '0.95rem', color: '#B2BAC2', transition: '0.2s', margin: '4px 8px', borderRadius: '8px', '& .MuiListItemIcon-root': { color: `hsl(${210 + color}, 100%, 60%)`, minWidth: '32px' }, '&:hover': { backgroundColor: `hsla(${210 + color}, 100%, 60%, 0.10)`, color: `hsl(${186 + color}, 100%, 50%)`, '& .MuiListItemIcon-root': { color: `hsl(${186 + color}, 100%, 50%)` } } }
                                },
                            },
                        }} transformOrigin={{ horizontal: 'right', vertical: 'top' }} anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}>
                            <MenuItem onClick={() => setSelectedUser(auth.user)} src={auth.user.avatar}>
                                <ListItemIcon><AccountCircleIcon fontSize="small" /></ListItemIcon> Profile
                            </MenuItem>
                            <MenuItem onClick={handleClose}><ListItemIcon><DashboardIcon fontSize="small" /></ListItemIcon> Dashboard</MenuItem>
                            <MenuItem onClick={handleClose}><ListItemIcon><PersonIcon fontSize="small" /></ListItemIcon> Following</MenuItem>
                            <MenuItem onClick={handleClose}><ListItemIcon><FavoriteIcon fontSize="small" /></ListItemIcon> Liked Works</MenuItem>
                            <MenuItem onClick={handleClose}><ListItemIcon><HistoryIcon fontSize="small" /></ListItemIcon> History</MenuItem>
                            <Divider sx={{ my: 1, borderColor: `hsla(${210 + color}, 100%, 60%, 0.10)` }} />
                            <MenuItem onClick={() => { setSettingsOpen(true); handleClose(); }}>
                                <ListItemIcon><Settings fontSize="small" /></ListItemIcon> Settings
                            </MenuItem>
                            <MenuItem onClick={handleLogout} sx={{ '&:hover': { backgroundColor: 'rgba(255, 61, 0, 0.1) !important', color: '#FF3D00 !important', '& .MuiListItemIcon-root': { color: '#FF3D00 !important' } } }}>
                                <ListItemIcon><Logout fontSize="small" /></ListItemIcon> Logout
                            </MenuItem>
                        </Menu>
                    </Box>
                ) : (
                    // ЗАМЕНА href на to
                    <Link to="/login" className="text-white hover:text-purple-400 font-bold">Вход</Link>
                )}
            </Container>
            <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} auth={auth} />
            <ProfileModal open={!!selectedUser} onClose={() => setSelectedUser(false)} profileUser={selectedUser} onOpenSettings={() => setSettingsOpen(true)} auth={auth} />
        </Box>
    );
}
