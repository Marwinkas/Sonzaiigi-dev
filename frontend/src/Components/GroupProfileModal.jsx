import React, { useState, useEffect } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import {
    Dialog, DialogContent, Box, Typography, Avatar, IconButton, 
    Button, Tabs, Tab, List, ListItem, ListItemAvatar, ListItemText,
    Switch, Divider, Tooltip, Chip, InputBase, DialogTitle, DialogActions,
    FormControlLabel, Checkbox, Select, MenuItem, CircularProgress
} from '@mui/material';
import { 
    Close, ContentCopy, Edit, PersonRemove, ExitToApp, 
    Refresh, Block, PhotoCamera, KeyboardArrowUp, KeyboardArrowDown, 
    AddCircleOutline, AutoFixHigh, Search, History, NoAccounts, HourglassEmpty, Check,Delete
} from '@mui/icons-material';
import theme from '../theme';
import api from '../api/axios';

const ROLE_PERMISSIONS = [
    { category: 'Управление сервером', perms: [
        { id: 'manageRoles', label: 'Управлять ролями' },
        { id: 'manageChat', label: 'Управлять чатом (имя, описание, аватар)' },
        { id: 'manageLinks', label: 'Управлять ссылками' },
        { id: 'readHistory', label: 'Читать историю сообщений' }
    ]},
    { category: 'Управление участниками', perms: [
        { id: 'kickMembers', label: 'Выгонять участников' },
        { id: 'banMembers', label: 'Банить участников' },
        { id: 'muteMembers', label: 'Замутить участника' },
    ]},
    { category: 'Текстовый чат', perms: [
        { id: 'sendMessages', label: 'Отправлять сообщения' },
        { id: 'attachFiles', label: 'Прикреплять файлы' },
        { id: 'sendGifs', label: 'Кидать гифки' },
        { id: 'addReactions', label: 'Добавлять реакции' },
        { id: 'mentions', label: 'Упоминания (@everyone и роли)' },
        { id: 'createPolls', label: 'Создавать опросы' },
        { id: 'bypassSlowMode', label: 'Обход медленного режима' }
    ]},
    { category: 'Модерация', perms: [
        { id: 'deleteOthersMessages', label: 'Удалять чужие сообщения' },
        { id: 'pinMessages', label: 'Закреплять сообщения' }
    ]}
];

export default function GroupProfileModal({ open, onClose, chat, auth, onChatRemoved }) {
    const [tab, setTab] = useState(0);
    const [groupData, setGroupData] = useState(null);
    const [loading, setLoading] = useState(true);

    const [memberDialogOpen, setMemberDialogOpen] = useState(false);
    const [selectedMember, setSelectedMember] = useState(null);

    const [roleDialogOpen, setRoleDialogOpen] = useState(false);
    const [selectedRole, setSelectedRole] = useState(null);
    const [roleAvatarFile, setRoleAvatarFile] = useState(null);

    const [grantableDialogOpen, setGrantableDialogOpen] = useState(false);
    const [banListDialogOpen, setBanListDialogOpen] = useState(false);
    const [auditDialogOpen, setAuditDialogOpen] = useState(false);

    // Состояния для никнейма
    const [nicknameDialogOpen, setNicknameDialogOpen] = useState(false);
    const [editNickname, setEditNickname] = useState('');

    const [isEditingGroup, setIsEditingGroup] = useState(false);
    const [editGroupName, setEditGroupName] = useState('');
    const [editGroupDesc, setEditGroupDesc] = useState('');
    const [editGroupAvatar, setEditGroupAvatar] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState(null);
    const [editLink, setEditLink] = useState('');

    const [memberSearch, setMemberSearch] = useState('');

    const fetchGroupData = async () => {
        if (!chat?.id) return;
        // Убираем setLoading(true), чтобы при фоновом обновлении (раз в 5 сек) не прыгал спиннер
        try {
            const res = await api.get(`/groups/${chat.id}`);
            setGroupData(res.data);
            
            // ✨ КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: 
            // Обновляем поля ввода только если они еще пустые (первая загрузка) 
            // или если мы НЕ находимся в режиме редактирования группы.
            if (!editGroupName) setEditGroupName(res.data.name || '');
            if (!editGroupDesc) setEditGroupDesc(res.data.description || '');
            if (!avatarPreview) setAvatarPreview(res.data.avatar || null);
            
            // Ссылку обновляем только если groupData еще не было (самый первый раз)
            if (!groupData) {
                setEditLink(res.data.invite_token || '');
            }
        } catch (error) {
            console.error("Ошибка загрузки профиля группы", error);
        } finally {
            setLoading(false);
        }
    };

    // Автоматическое обновление данных (в реальном времени)
useEffect(() => {
    if (open) {
        setLoading(true); // Показываем загрузку только при открытии
        fetchGroupData();
        const interval = setInterval(fetchGroupData, 5000);
        return () => clearInterval(interval);
    } else {
        // Сбрасываем временные состояния при закрытии, чтобы при открытии другого чата данные не перемешались
        setGroupData(null);
        setEditGroupName('');
        setEditLink('');
    }
}, [open, chat?.id]);

    if (!groupData && loading) {
        return (
            <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth PaperProps={{ sx: { bgcolor: '#0f172a', borderRadius: 4, border: '1px solid rgba(255,255,255,0.1)', height: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' } }}>
                <CircularProgress sx={{ color: '#38bdf8' }} />
            </Dialog>
        );
    }
    if (!groupData) return null;

    const myMember = groupData.members.find(m => m.id === auth?.user?.id) || { roleIds: [], individualOverrides: {}, isOwner: false };
    const isOwner = myMember.isOwner; 
    
    const getMyHighestRoleIndex = () => {
        if (isOwner) return -1; 
        if (!myMember.roleIds || !myMember.roleIds.length) return 999;
        const hierarchies = myMember.roleIds.map(rid => {
            const r = groupData.roles.find(role => role.id === rid);
            return r ? r.hierarchy : 999;
        });
        return Math.min(...hierarchies);
    };
    const myHighestIndex = getMyHighestRoleIndex();

    const canManageRole = (roleId) => {
        if (isOwner) return true;
        const targetRole = groupData.roles.find(r => r.id === roleId);
        if (!targetRole) return false;
        return myHighestIndex < targetRole.hierarchy; 
    };

    const getMyPermissions = () => {
        if (isOwner) return Object.fromEntries(ROLE_PERMISSIONS.flatMap(c => c.perms).map(p => [p.id, true]));
        let perms = {};
        if (myMember.roleIds) {
            myMember.roleIds.forEach(rid => {
                const role = groupData.roles.find(r => r.id === rid);
                if (role && role.permissions) Object.assign(perms, role.permissions);
            });
        }
        Object.assign(perms, myMember.individualOverrides || {});
        return perms;
    };
    const myPermissions = getMyPermissions();
    const hasAdminRights = myPermissions.manageRoles || myPermissions.manageChat || isOwner;

    const handleSaveGroupInfo = async () => {
        try {
            const formData = new FormData();
            formData.append('Name', editGroupName);
            formData.append('Description', editGroupDesc);
            if (editGroupAvatar) formData.append('Avatar', editGroupAvatar);

            await api.patch(`/groups/${chat.id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            setIsEditingGroup(false);
            fetchGroupData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleResetLink = async () => {
        try {
            const res = await api.post(`/groups/${chat.id}/reset-link`);
            setEditLink(res.data.new_token);
            fetchGroupData();
        } catch (error) {
            console.error(error);
        }
    };

    const handleSaveLink = async () => {
        // 1. Если пытаются сохранить пустую строку — просто возвращаем старую ссылку
        if (!editLink.trim()) {
            setEditLink(groupData.invite_token);
            return;
        }

        // Если ссылка не изменилась, ничего не делаем
        if (editLink === groupData.invite_token) return;

        try {
            const formData = new FormData();
            formData.append('InviteToken', editLink);
            
            // Отправляем на сервер
            await api.patch(`/groups/${chat.id}`, formData);
            
            // Если всё ок, обновляем данные группы (теперь в groupData будет новая ссылка)
            const res = await api.get(`/groups/${chat.id}`);
            setGroupData(res.data);
        } catch (error) {
            // 2. Если ошибка (например, 400 занято) — возвращаем то, что было в базе
            setEditLink(groupData.invite_token);
        }
};

    const handleLeave = async () => {
        if (window.confirm("Ты уверен, что хочешь покинуть группу?")) {
            try {
                await api.post(`/groups/${chat.id}/leave`);
                onChatRemoved(chat.id);
                onClose();
                
            } catch (error) {}
        }
    };

    const handleSettingsUpdate = async (field, value) => {
        try {
            setGroupData(prev => {
                const newData = { ...prev };
                if (field.startsWith('sysMsgs.')) {
                    newData.sysMsgs = { ...newData.sysMsgs, [field.split('.')[1]]: value };
                } else {
                    newData[field] = value;
                }
                return newData;
            });

            const formData = new FormData();
            if (field === 'slowMode') formData.append('SlowMode', value);
            if (field === 'sysMsgs.join') formData.append('SysMsgJoin', value);
            if (field === 'sysMsgs.leave') formData.append('SysMsgLeave', value);
            if (field === 'sysMsgs.edit') formData.append('SysMsgEdit', value);

            await api.patch(`/groups/${chat.id}`, formData);
        } catch (error) {
            fetchGroupData();  
        }
    };
const handleDeleteGroup = async () => {
        if (window.confirm("ВНИМАНИЕ! Вы уверены, что хотите полностью УДАЛИТЬ группу? Это действие нельзя отменить, все сообщения исчезнут.")) {
            try {
                // Предполагаем, что на бэкенде есть DELETE метод для /groups/{id}
                await api.delete(`/groups/${chat.id}`);
                alert("Группа успешно удалена");
                onChatRemoved(chat.id);
                onClose();
            } catch (error) {
                console.error("Ошибка при удалении группы", error);
            }
        }
    };
    const handleOpenMemberManage = (member) => {
        if (member.id === auth?.user?.id) return; 
        setSelectedMember({ ...member, individualOverrides: member.individualOverrides || {}, roleIds: member.roleIds || [] }); 
        setMemberDialogOpen(true);
    };

    const handleSaveMember = async () => {
        try {
            await api.patch(`/groups/${chat.id}/members/${selectedMember.id}`, {
                RoleIds: selectedMember.roleIds,
                OverridesJson: JSON.stringify(selectedMember.individualOverrides)
            });
            setMemberDialogOpen(false);
            fetchGroupData();
        } catch (error) {
            console.error("Ошибка сохранения прав", error);
        }
    };

    // Управление никнеймом
    const handleOpenNickname = () => {
        setEditNickname(selectedMember?.nickname || selectedMember?.name || '');
        setNicknameDialogOpen(true);
    };

    const handleSaveNickname = async () => {
        try {
            await api.patch(`/groups/${chat.id}/members/${selectedMember.id}/nickname`, {
                Nickname: editNickname
            });
            setNicknameDialogOpen(false);
            fetchGroupData();
        } catch (error) {
        }
    };

    const toggleMemberRole = (roleId) => {
        if (!canManageRole(roleId)) return;
        setSelectedMember(prev => ({
            ...prev,
            roleIds: prev.roleIds.includes(roleId) ? prev.roleIds.filter(id => id !== roleId) : [...prev.roleIds, roleId]
        }));
    };

    const toggleMemberOverride = (permId) => {
        setSelectedMember(prev => {
            const currentOverride = prev.individualOverrides[permId];
            const newOverrides = { ...prev.individualOverrides };
            
            if (currentOverride === undefined) newOverrides[permId] = false; 
            else if (currentOverride === false) newOverrides[permId] = true; 
            else delete newOverrides[permId]; 

            return { ...prev, individualOverrides: newOverrides };
        });
    };

    const handleMemberAction = async (actionType) => {
        if (!window.confirm(`Точно хочешь ${actionType === 'kick' ? 'выгнать' : 'забанить'} этого пользователя?`)) return;
        try {
            await api.post(`/groups/${chat.id}/members/${selectedMember.id}/${actionType}`);
            setMemberDialogOpen(false);
            fetchGroupData();
        } catch (error) {}
    };

    const handleUnban = async (userId) => {
        try {
            await api.post(`/groups/${chat.id}/bans/${userId}/unban`);
            fetchGroupData();
        } catch (error) {}
    };

    const handleOpenRoleManage = (role = null) => {
        setRoleAvatarFile(null); 
        if (role) {
            if (!canManageRole(role.id)) return;
            setSelectedRole({ ...role, permissions: role.permissions || {}, grantablePermissions: role.grantablePermissions || {} });
        } else {
            setSelectedRole({ isNew: true, name: "Новая роль", color: "#94a3b8", icon: null, mentionable: false, permissions: {}, canEditRoleDesign: false, grantablePermissions: {} });
        }
        setRoleDialogOpen(true);
    };

    const handleSaveRole = async () => {
        try {
            const formData = new FormData();
            formData.append('Name', selectedRole.name);
            formData.append('Color', selectedRole.color);
            formData.append('IsMentionable', selectedRole.mentionable);
            formData.append('CanEditRoleDesign', selectedRole.canEditRoleDesign);
            formData.append('PermissionsJson', JSON.stringify(selectedRole.permissions));
            formData.append('GrantablePermissionsJson', JSON.stringify(selectedRole.grantablePermissions));
            
            if (roleAvatarFile) formData.append('Icon', roleAvatarFile);

            if (selectedRole.isNew) {
                await api.post(`/groups/${chat.id}/roles`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            } else {
                await api.patch(`/groups/${chat.id}/roles/${selectedRole.id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
            }
            
            setRoleDialogOpen(false);
            fetchGroupData();
        } catch (error) {
            console.error("Ошибка сохранения роли", error);
        }
    };

    const toggleRolePermission = (permId) => {
        setSelectedRole(prev => ({
            ...prev,
            permissions: { ...prev.permissions, [permId]: !prev.permissions[permId] }
        }));
    };

    const toggleGrantablePermission = (permId) => {
        setSelectedRole(prev => ({
            ...prev,
            grantablePermissions: { ...prev.grantablePermissions, [permId]: !prev.grantablePermissions[permId] }
        }));
    };

    const moveRole = async (index, offset) => {
        const newRoles = [...groupData.roles];
        const targetIndex = index + offset;
        
        if (index === 0 || targetIndex === 0) return; 
        if (myHighestIndex >= newRoles[index].hierarchy || myHighestIndex >= newRoles[targetIndex].hierarchy) {
            return;
        }

        const temp = newRoles[index];
        newRoles[index] = newRoles[targetIndex];
        newRoles[targetIndex] = temp;

        const orderedRoleIds = newRoles.map(r => r.id);
        setGroupData(prev => ({ ...prev, roles: newRoles }));

        try {
            await api.patch(`/groups/${chat.id}/roles/reorder`, { RoleIds: orderedRoleIds });
            fetchGroupData(); 
        } catch (error) {
            fetchGroupData(); 
        }
    };

    const filteredMembers = groupData.members.filter(m => m.name.toLowerCase().includes(memberSearch.toLowerCase()));

    return (
        <ThemeProvider theme={theme}>
            <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth PaperProps={{ sx: { bgcolor: '#0f172a', borderRadius: 4, border: '1px solid rgba(255,255,255,0.1)' } }}>
                <Box sx={{ display: 'flex', alignItems: 'center', px: 3, py: 2, bgcolor: '#1e293b' }}>
                    <Typography variant="h6" sx={{ color: 'white', fontWeight: 'bold', flex: 1 }}>{groupData.name}</Typography>
                    <IconButton onClick={onClose} sx={{ color: '#94a3b8' }}><Close /></IconButton>
                </Box>

                <DialogContent sx={{ p: 0, height: 600 }}>
                    <Tabs value={tab} onChange={(e, v) => setTab(v)} variant="scrollable" scrollButtons="auto" sx={{ bgcolor: '#1e293b', borderBottom: '1px solid rgba(255,255,255,0.05)', '& .MuiTab-root': { color: '#94a3b8', textTransform: 'none', fontWeight: 'bold' }, '& .Mui-selected': { color: '#38bdf8 !important' }, '& .MuiTabs-indicator': { bgcolor: '#38bdf8' } }}>
                        <Tab label="Инфо" />
                        <Tab label={`Участники (${groupData.members.length})`} />
                        {myPermissions.manageRoles && <Tab label="Роли" />}
                        {hasAdminRights && <Tab label="Прочее" />}
                    </Tabs>

                    {/* ВКЛАДКА 1: ИНФО */}
                    {tab === 0 && (
                        <Box sx={{ p: 3 }}>
                            {!isEditingGroup ? (
                                <Box sx={{ textAlign: 'center' }}>
                                    <Avatar src={groupData.avatar} sx={{ width: 100, height: 100, mx: 'auto', mb: 2, bgcolor: '#38bdf8' }}>{groupData.name[0]}</Avatar>
                                    <Typography variant="h5" color="white" fontWeight="bold">{groupData.name}</Typography>
                                    <Typography color="#94a3b8" variant="body2" mb={3}>{groupData.description || "Нет описания"}</Typography>
                                    {myPermissions.manageChat && (
                                        <Button variant="outlined" onClick={() => setIsEditingGroup(true)} sx={{ mb: 4, color: '#38bdf8', borderColor: '#38bdf8' }}>Редактировать профиль</Button>
                                    )}
                                </Box>
                            ) : (
                                <Box sx={{ bgcolor: '#1e293b', p: 2, borderRadius: 3, mb: 4 }}>
                                    <Box sx={{ display: 'flex', gap: 2, mb: 2, alignItems: 'flex-start' }}>
                                        <Box sx={{ position: 'relative' }}>
                                            <Avatar src={avatarPreview} sx={{ width: 70, height: 70, bgcolor: '#f59e0b' }}>{editGroupName[0]}</Avatar>
                                            <IconButton component="label" sx={{ position: 'absolute', bottom: -5, right: -5, bgcolor: '#38bdf8', color: '#0f172a', width: 26, height: 26, '&:hover': {bgcolor: '#0ea5e9'} }}>
                                                <PhotoCamera sx={{ fontSize: 14 }} />
                                                <input type="file" hidden accept="image/*" onChange={e => {
                                                    const file = e.target.files[0];
                                                    if (file) {
                                                        setEditGroupAvatar(file);
                                                        setAvatarPreview(URL.createObjectURL(file));
                                                    }
                                                }} />
                                            </IconButton>
                                        </Box>
                                        <InputBase value={editGroupName} onChange={e => setEditGroupName(e.target.value)} fullWidth placeholder="Название группы" sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1, borderRadius: 2 }} />
                                    </Box>
                                    <InputBase value={editGroupDesc} onChange={e => setEditGroupDesc(e.target.value)} fullWidth multiline rows={3} placeholder="Описание" sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1, borderRadius: 2, mb: 2 }} />
                                    <Box sx={{ display: 'flex', gap: 1 }}>
                                        <Button fullWidth onClick={() => setIsEditingGroup(false)} sx={{ color: '#94a3b8' }}>Отмена</Button>
                                        <Button fullWidth variant="contained" onClick={handleSaveGroupInfo} sx={{ bgcolor: '#38bdf8', color: '#0f172a' }}>Сохранить</Button>
                                    </Box>
                                </Box>
                            )}

                            <Typography color="#38bdf8" fontSize="0.8rem" fontWeight="bold" mb={1} textTransform="uppercase">Ссылка-приглашение</Typography>
                            {myPermissions.manageLinks ? (
                                <Box sx={{ bgcolor: '#1e293b', p: 2, borderRadius: 3, mb: 3 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                        <Typography color="#94a3b8" sx={{ userSelect: 'none' }}>sonzaiigi.com/join/</Typography>
                                        <InputBase value={editLink} onChange={e => setEditLink(e.target.value.replace(/[^a-zA-Z0-9_-]/g, ''))} sx={{ color: 'white', bgcolor: '#0f172a', px: 1, py: 0.5, borderRadius: 1, flex: 1 }}/>
                                        {/* Кнопка сохранения ссылки */}
                                        <Button variant="contained" size="small" onClick={handleSaveLink} sx={{ bgcolor: '#38bdf8', color: '#0f172a', fontWeight: 'bold' }}>Сохранить</Button>
                                    </Box>
                                    <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
                                        <Button variant="outlined" size="small" onClick={handleResetLink} startIcon={<Refresh />} sx={{ color: '#a855f7', borderColor: '#a855f7', textTransform: 'none' }}>Сбросить ссылку</Button>
                                        <Button variant="contained" size="small" onClick={() => navigator.clipboard.writeText(`${window.location.origin}/join/${groupData.invite_token}`)} sx={{ bgcolor: '#10b981', color: '#0f172a', ml: 'auto', textTransform: 'none', fontWeight: 'bold' }}>Копировать</Button>
                                    </Box>
                                </Box>
                            ) : (
                                <Box sx={{ bgcolor: '#1e293b', p: 2, borderRadius: 2, display: 'flex', alignItems: 'center', mb: 3 }}>
                                    <Typography color="white" sx={{ flex: 1 }}>{window.location.origin}/join/{groupData.invite_token}</Typography>
                                    <Tooltip title="Копировать"><IconButton onClick={() => navigator.clipboard.writeText(`${window.location.origin}/join/${groupData.invite_token}`)} sx={{ color: '#38bdf8' }}><ContentCopy fontSize="small"/></IconButton></Tooltip>
                                </Box>
                            )}
                            
                            {!isOwner && <Button variant="outlined" color="error" fullWidth onClick={handleLeave} startIcon={<ExitToApp />}>Покинуть чат</Button>}
                            {isOwner && (
                                            <Button 
                                                variant="contained" 
                                                color="error" 
                                                startIcon={<Delete />} 
                                                onClick={handleDeleteGroup}
                                                sx={{ mt: 2, fontWeight: 'bold' }}
                                            >
                                                Удалить группу навсегда
                                            </Button>
                                        )}
                        </Box>
                    )}

                    {/* ВКЛАДКА 2: УЧАСТНИКИ */}
                    {tab === 1 && (
                        <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                            <Box sx={{ p: 2, borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', bgcolor: '#1e293b', borderRadius: 2, px: 2, py: 0.5 }}>
                                    <Search sx={{ color: '#94a3b8', mr: 1, fontSize: 20 }} />
                                    <InputBase value={memberSearch} onChange={(e) => setMemberSearch(e.target.value)} placeholder="Поиск участников..." sx={{ color: 'white', flex: 1, fontSize: '0.9rem' }} />
                                </Box>
                            </Box>
                            <List sx={{ p: 0, overflowY: 'auto', flex: 1 }}>
                                {filteredMembers.map(m => {
                                    const memberRoles = groupData.roles.filter(r => m.roleIds.includes(r.id));
                                    const canClick = m.id !== auth?.user?.id && (isOwner || (!m.isOwner && (myPermissions.manageRoles || myPermissions.kickMembers || myPermissions.banMembers || myPermissions.muteMembers || myPermissions.manageNicknames)));
                                    
                                    return (
                                        <ListItem key={m.id} button={canClick ? true : undefined} onClick={() => canClick && handleOpenMemberManage(m)} sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', py: 1.5, px: 3 }}>
                                            <ListItemAvatar><Avatar src={m.avatar} sx={{ bgcolor: '#38bdf8' }}>{m.name[0]}</Avatar></ListItemAvatar>
                                            <ListItemText 
                                                primary={<Typography color="white" fontWeight="bold">{m.nickname || m.name} {m.isOwner && "👑"}</Typography>}
                                                secondary={
                                                    <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5, flexWrap: 'wrap' }}>
                                                        {memberRoles.length > 0 ? memberRoles.map(r => (
                                                            <Chip key={r.id} avatar={r.icon ? <Avatar src={r.icon} /> : undefined} label={r.name} size="small" sx={{ bgcolor: `${r.color}20`, color: r.color, border: `1px solid ${r.color}50`, fontSize: '0.7rem', height: 20 }} />
                                                        )) : <Typography fontSize="0.75rem" color="#64748b">Без роли</Typography>}
                                                    </Box>
                                                }
                                            />
                                        </ListItem>
                                    );
                                })}
                            </List>
                        </Box>
                    )}

                    {/* ВКЛАДКА 3: РОЛИ */}
                    {tab === 2 && myPermissions.manageRoles && (
                        <Box sx={{ p: 0 }}>
                            <Box sx={{ p: 2, borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                <Button fullWidth variant="contained" startIcon={<AddCircleOutline />} onClick={() => handleOpenRoleManage()} sx={{ bgcolor: '#38bdf8', color: '#0f172a', fontWeight: 'bold' }}>Создать новую роль</Button>
                            </Box>
                            <Typography sx={{ color: '#64748b', fontSize: '0.75rem', px: 3, pt: 2, pb: 1, textTransform: 'uppercase' }}>Иерархия (кто выше, тот главнее)</Typography>
                            <List sx={{ p: 0 }}>
                                {groupData.roles.map((r, index) => {
                                    const disabled = !canManageRole(r.id);
                                    return (
                                    <ListItem key={r.id} sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', px: 3, opacity: disabled ? 0.5 : 1 }}>
                                        <ListItemAvatar onClick={() => !disabled && handleOpenRoleManage(r)} sx={{ cursor: disabled ? 'default' : 'pointer' }}>
                                            <Avatar src={r.icon} sx={{ bgcolor: r.color, width: 36, height: 36 }}>{r.name[0]}</Avatar>
                                        </ListItemAvatar>
                                        <ListItemText 
                                            onClick={() => !disabled && handleOpenRoleManage(r)}
                                            primary={<Typography color="white" fontWeight="bold" sx={{ cursor: disabled ? 'default' : 'pointer' }}>{r.name}</Typography>} 
                                            secondary={<Typography color={disabled ? "#ef4444" : "#94a3b8"} fontSize="0.75rem">{disabled ? "Нет прав редактировать эту роль" : "Нажми для настройки"}</Typography>} 
                                        />
                                        <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                                            <IconButton size="small" disabled={disabled || index <= 1} onClick={() => moveRole(index, -1)}>
                                                <KeyboardArrowUp fontSize="small" sx={{ color: '#94a3b8' }}/>
                                            </IconButton>
                                            <IconButton size="small" disabled={disabled || index === 0 || index === groupData.roles.length - 1} onClick={() => moveRole(index, 1)}>
                                                <KeyboardArrowDown fontSize="small" sx={{ color: '#94a3b8' }}/>
                                            </IconButton>
                                        </Box>
                                    </ListItem>
                                )})}
                            </List>
                        </Box>
                    )}

                    {/* ВКЛАДКА 4: ПРОЧЕЕ */}
                    {tab === 3 && hasAdminRights && (
                        <Box sx={{ p: 3 }}>
                            <Typography color="#38bdf8" fontWeight="bold" fontSize="0.8rem" textTransform="uppercase" mb={2}>Управление и Модерация</Typography>
                            <Box sx={{ display: 'flex', gap: 2, mb: 4 }}>
                                <Button variant="outlined" startIcon={<NoAccounts />} onClick={() => setBanListDialogOpen(true)} fullWidth sx={{ borderColor: '#ef4444', color: '#ef4444', '&:hover': { bgcolor: 'rgba(239,68,68,0.1)' } }}>Чёрный список</Button>
                                <Button variant="outlined" startIcon={<History />} onClick={() => setAuditDialogOpen(true)} fullWidth sx={{ borderColor: '#a855f7', color: '#a855f7', '&:hover': { bgcolor: 'rgba(168,85,247,0.1)' } }}>Журнал действий</Button>
                            </Box>

                            <Divider sx={{ my: 3, borderColor: 'rgba(255,255,255,0.1)' }} />

                            <Typography color="#38bdf8" fontWeight="bold" fontSize="0.8rem" textTransform="uppercase" mb={2}>Системные сообщения в чате</Typography>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                <Typography color="white" fontSize="0.9rem">Сообщения о присоединении (Добро пожаловать)</Typography>
                                <Switch checked={groupData.sysMsgs.join} onChange={(e) => handleSettingsUpdate('sysMsgs.join', e.target.checked)} color="primary" />
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                <Typography color="white" fontSize="0.9rem">Сообщения о выходе из группы</Typography>
                                <Switch checked={groupData.sysMsgs.leave} onChange={(e) => handleSettingsUpdate('sysMsgs.leave', e.target.checked)} color="primary" />
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                <Typography color="white" fontSize="0.9rem">Оповещения об изменении названия/аватарки</Typography>
                                <Switch checked={groupData.sysMsgs.edit} onChange={(e) => handleSettingsUpdate('sysMsgs.edit', e.target.checked)} color="primary" />
                            </Box>

                            <Divider sx={{ my: 3, borderColor: 'rgba(255,255,255,0.1)' }} />

                            <Typography color="#38bdf8" fontWeight="bold" fontSize="0.8rem" textTransform="uppercase" mb={2} sx={{ display: 'flex', alignItems: 'center', gap: 1 }}><HourglassEmpty fontSize="small" /> Медленный режим</Typography>
                            <Typography color="#94a3b8" fontSize="0.8rem" mb={2}>Ограничивает частоту отправки сообщений для обычных участников.</Typography>
                            <Select
                                value={groupData.slowMode || 0}
                                onChange={(e) => handleSettingsUpdate('slowMode', e.target.value)}
                                fullWidth
                                size="small"
                                sx={{ bgcolor: '#0f172a', color: 'white', '& .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.1)' }, '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: '#38bdf8' } }}
                            >
                                <MenuItem value={0}>Отключен</MenuItem>
                                <MenuItem value={10}>10 секунд</MenuItem>
                                <MenuItem value={30}>30 секунд</MenuItem>
                                <MenuItem value={60}>1 минута</MenuItem>
                                <MenuItem value={300}>5 минут</MenuItem>
                            </Select>
                        </Box>
                    )}
                </DialogContent>
            </Dialog>

            {/* --- ПОД-МОДАЛКА: УПРАВЛЕНИЕ УЧАСТНИКОМ --- */}
            <Dialog open={memberDialogOpen} onClose={() => setMemberDialogOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', minWidth: 350, borderRadius: 3 } }}>
                <DialogTitle sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', pb: 1, display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Avatar sx={{ bgcolor: '#38bdf8' }}>{selectedMember?.name[0]}</Avatar> 
                    <Box>
                        <Typography fontWeight="bold">{selectedMember?.nickname || selectedMember?.name}</Typography>
                        <Typography fontSize="0.8rem" color="#94a3b8">Управление участником</Typography>
                    </Box>
                </DialogTitle>
                <DialogContent sx={{ p: 3, maxHeight: 500, overflowY: 'auto' }}>
                    
                    {myPermissions.manageRoles && (
                        <>
                            <Typography color="#38bdf8" fontWeight="bold" fontSize="0.8rem" mb={2} textTransform="uppercase">Выданные роли</Typography>
                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 3 }}>
                                {groupData.roles.map(r => {
                                    if (r.hierarchy === 0) return null; 
                                    const hasRole = selectedMember?.roleIds.includes(r.id);
                                    const disabled = !canManageRole(r.id);

                                    return (
                                        <Box key={r.id} onClick={() => !disabled && toggleMemberRole(r.id)} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 1.5, borderRadius: 2, bgcolor: hasRole ? `${r.color}20` : 'rgba(255,255,255,0.02)', border: `1px solid ${hasRole ? r.color : 'rgba(255,255,255,0.1)'}`, cursor: disabled ? 'not-allowed' : 'pointer', opacity: disabled ? 0.5 : 1, transition: 'all 0.2s', '&:hover': { filter: disabled ? 'none' : 'brightness(1.2)' } }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                                                <Avatar src={r.icon} sx={{ width: 24, height: 24, bgcolor: r.color, fontSize: '0.8rem' }}>{r.name[0]}</Avatar>
                                                <Typography color={hasRole ? "white" : "#94a3b8"} fontWeight="bold">{r.name}</Typography>
                                            </Box>
                                            {hasRole && <Check sx={{ color: r.color }} fontSize="small" />}
                                        </Box>
                                    );
                                })}
                            </Box>
                            <Divider sx={{ my: 3, borderColor: 'rgba(255,255,255,0.1)' }} />
                        </>
                    )}

                    <Typography color="#38bdf8" fontWeight="bold" fontSize="0.8rem" mb={2} textTransform="uppercase">Исключения прав</Typography>
                    <Typography color="#94a3b8" fontSize="0.75rem" mb={2}>Крестик — строго запретить. Галочка — строго разрешить. Пусто — зависит от ролей.</Typography>
                    
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 3 }}>
                        {[
                            { id: 'sendMessages', label: 'Писать сообщения' },
                            { id: 'attachFiles', label: 'Отправлять медиа' },
                            { id: 'addReactions', label: 'Добавлять реакции' },
                            { id: 'canForward', label: 'Пересылать сообщения' }
                        ].map(perm => {
                            const state = selectedMember?.individualOverrides[perm.id];
                            return (
                                <Box key={perm.id} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 1, bgcolor: 'rgba(255,255,255,0.02)', borderRadius: 1 }}>
                                    <Typography color="white" fontSize="0.85rem">{perm.label}</Typography>
                                    <Box sx={{ display: 'flex', gap: 0.5 }}>
                                        <Button size="small" onClick={() => toggleMemberOverride(perm.id)} variant={state === false ? "contained" : "outlined"} color="error" sx={{ minWidth: 30, p: 0, height: 30 }}><Close fontSize="small"/></Button>
                                        <Button size="small" onClick={() => toggleMemberOverride(perm.id)} variant={state === undefined ? "contained" : "outlined"} sx={{ minWidth: 30, p: 0, height: 30, color: state === undefined ? '#0f172a' : '#94a3b8', borderColor: '#64748b', bgcolor: state === undefined ? '#94a3b8' : 'transparent' }}>—</Button>
                                        <Button size="small" onClick={() => toggleMemberOverride(perm.id)} variant={state === true ? "contained" : "outlined"} color="success" sx={{ minWidth: 30, p: 0, height: 30 }}><Check fontSize="small"/></Button>
                                    </Box>
                                </Box>
                            )
                        })}
                    </Box>

                    <Divider sx={{ my: 3, borderColor: 'rgba(255,255,255,0.1)' }} />

                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                        {/* Кнопка смены никнейма */}
                       
                        {myPermissions.kickMembers && <Button variant="outlined" color="error" startIcon={<PersonRemove />} onClick={() => handleMemberAction('kick')}>Выгнать участника</Button>}
                        {myPermissions.banMembers && <Button variant="contained" color="error" startIcon={<Block />} onClick={() => handleMemberAction('ban')}>Забанить навсегда</Button>}
                    </Box>

                </DialogContent>
                <DialogActions sx={{ p: 2, borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                    <Button onClick={() => setMemberDialogOpen(false)} sx={{ color: '#94a3b8' }}>Отмена</Button>
                    <Button onClick={handleSaveMember} variant="contained" sx={{ bgcolor: '#38bdf8', color: '#0f172a' }}>Сохранить</Button>
                </DialogActions>
            </Dialog>

            {/* --- НОВОЕ МОДАЛЬНОЕ ОКНО: ИЗМЕНЕНИЕ НИКНЕЙМА --- */}
            <Dialog open={nicknameDialogOpen} onClose={() => setNicknameDialogOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', minWidth: 300, borderRadius: 3 } }}>
                <DialogTitle sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>Изменить никнейм</DialogTitle>
                <DialogContent sx={{ p: 3 }}>
                    <InputBase 
                        fullWidth 
                        value={editNickname} 
                        onChange={(e) => setEditNickname(e.target.value)} 
                        placeholder="Введи новый никнейм..." 
                        sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1, borderRadius: 2, mt: 1 }} 
                    />
                </DialogContent>
                <DialogActions sx={{ p: 2, borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                    <Button onClick={() => setNicknameDialogOpen(false)} sx={{ color: '#94a3b8' }}>Отмена</Button>
                    <Button onClick={handleSaveNickname} variant="contained" sx={{ bgcolor: '#38bdf8', color: '#0f172a' }}>Сохранить</Button>
                </DialogActions>
            </Dialog>

            {/* --- ОСТАЛЬНЫЕ ОКНА (Роли, Баны, Аудит) БЕЗ ИЗМЕНЕНИЙ --- */}
            <Dialog open={roleDialogOpen} onClose={() => setRoleDialogOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', minWidth: 450, borderRadius: 3 } }}>
                <DialogTitle sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', pb: 2 }}>Настройка роли: {selectedRole?.name}</DialogTitle>
                <DialogContent sx={{ p: 3, height: 500 }}>
                    <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'flex-start' }}>
                        <Box sx={{ position: 'relative' }}>
                            <Avatar src={roleAvatarFile ? URL.createObjectURL(roleAvatarFile) : selectedRole?.icon} sx={{ width: 64, height: 64, bgcolor: selectedRole?.color, borderRadius: 2 }}>{selectedRole?.name[0] || '?'}</Avatar>
                            <IconButton component="label" sx={{ position: 'absolute', bottom: -8, right: -8, bgcolor: '#38bdf8', color: '#0f172a', width: 28, height: 28, '&:hover': {bgcolor: '#0ea5e9'}, boxShadow: '0 2px 5px rgba(0,0,0,0.5)' }}>
                                <PhotoCamera sx={{ fontSize: 16 }} />
                                <input type="file" hidden accept="image/*" onChange={e => { if (e.target.files[0]) setRoleAvatarFile(e.target.files[0]); }} />
                            </IconButton>
                        </Box>
                        <Box sx={{ flex: 1 }}>
                            <Typography color="#94a3b8" fontSize="0.8rem" mb={0.5}>Название роли</Typography>
                            <InputBase value={selectedRole?.name || ''} onChange={e => setSelectedRole(prev => ({...prev, name: e.target.value}))} fullWidth sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1, borderRadius: 1, fontWeight: 'bold' }} />
                        </Box>
                    </Box>

                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2, p: 2, bgcolor: '#0f172a', borderRadius: 2 }}>
                        <Typography color="#94a3b8" fontSize="0.9rem" flex={1}>Цвет роли</Typography>
                        <Box sx={{ position: 'relative', width: 40, height: 40, borderRadius: '50%', overflow: 'hidden', border: '2px solid rgba(255,255,255,0.2)', cursor: 'pointer' }}>
                            <input type="color" value={selectedRole?.color || '#94a3b8'} onChange={(e) => setSelectedRole(prev => ({...prev, color: e.target.value}))} style={{ position: 'absolute', top: -10, left: -10, width: 60, height: 60, cursor: 'pointer', border: 'none', padding: 0 }} />
                        </Box>
                        <Typography color="white" fontWeight="bold">{selectedRole?.color?.toUpperCase()}</Typography>
                    </Box>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, p: 2, bgcolor: 'rgba(255,255,255,0.02)', borderRadius: 2, border: '1px solid rgba(255,255,255,0.05)' }}>
                        <Box>
                            <Typography color="white" fontSize="0.9rem" fontWeight="bold">Разрешить упоминать эту роль</Typography>
                            <Typography color="#94a3b8" fontSize="0.75rem">Участники смогут тегать её через @{selectedRole?.name || 'роль'}</Typography>
                        </Box>
                        <Switch checked={!!selectedRole?.mentionable} onChange={(e) => setSelectedRole(prev => ({ ...prev, mentionable: e.target.checked }))} sx={{ '& .MuiSwitch-switchBase.Mui-checked': { color: selectedRole?.color }, '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': { backgroundColor: selectedRole?.color } }} />
                    </Box>

                    <Divider sx={{ my: 3, borderColor: 'rgba(255,255,255,0.1)' }} />
                    
                    <Typography variant="h6" color="white" mb={2}>Права доступа</Typography>
                    <Typography color="#94a3b8" fontSize="0.8rem" mb={3}>Ты можешь выдать этой роли только те права, которыми обладаешь сам.</Typography>

                    {ROLE_PERMISSIONS.map(category => {
                        const availablePerms = category.perms.filter(p => myPermissions[p.id]);
                        if (availablePerms.length === 0) return null; 

                        return (
                            <Box key={category.category} sx={{ mb: 3 }}>
                                <Typography color={selectedRole?.color || "#38bdf8"} fontWeight="bold" fontSize="0.8rem" textTransform="uppercase" mb={1}>{category.category}</Typography>
                                <Box sx={{ bgcolor: '#0f172a', borderRadius: 2, overflow: 'hidden' }}>
                                    {availablePerms.map((perm, i) => (
                                        <Box key={perm.id} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 2, borderBottom: i !== availablePerms.length - 1 ? '1px solid rgba(255,255,255,0.05)' : 'none' }}>
                                            <Typography color="white" fontSize="0.9rem">{perm.label}</Typography>
                                            <Switch checked={!!selectedRole?.permissions[perm.id]} onChange={() => toggleRolePermission(perm.id)} sx={{ '& .MuiSwitch-switchBase.Mui-checked': { color: selectedRole?.color }, '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': { backgroundColor: selectedRole?.color } }} />
                                        </Box>
                                    ))}
                                    
                                    {category.category === 'Управление сервером' && selectedRole?.permissions['manageRoles'] && availablePerms.some(p => p.id === 'manageRoles') && (
                                        <Box sx={{ p: 2, bgcolor: 'rgba(56, 189, 248, 0.05)', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                                            <Typography color="#38bdf8" fontSize="0.8rem" fontWeight="bold" mb={1}>ДОП. НАСТРОЙКИ УПРАВЛЕНИЯ РОЛЯМИ</Typography>
                                            <FormControlLabel
                                                control={<Checkbox checked={!!selectedRole?.canEditRoleDesign} onChange={(e) => setSelectedRole(prev => ({...prev, canEditRoleDesign: e.target.checked}))} sx={{ color: '#38bdf8', '&.Mui-checked': { color: '#38bdf8' } }} />}
                                                label={<Typography color="white" fontSize="0.85rem">Может менять название, цвет и иконку другим ролям</Typography>}
                                            />
                                            <Button variant="outlined" size="small" fullWidth onClick={() => setGrantableDialogOpen(true)} sx={{ mt: 1, borderColor: '#38bdf8', color: '#38bdf8' }}>Выбрать права для выдачи</Button>
                                        </Box>
                                    )}
                                </Box>
                            </Box>
                        );
                    })}
                </DialogContent>
                <DialogActions sx={{ p: 2, borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                    <Button onClick={() => setRoleDialogOpen(false)} sx={{ color: '#94a3b8' }}>Отмена</Button>
                    <Button onClick={handleSaveRole} variant="contained" sx={{ bgcolor: selectedRole?.color || '#38bdf8', color: '#0f172a', fontWeight: 'bold' }}>Сохранить роль</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={grantableDialogOpen} onClose={() => setGrantableDialogOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', minWidth: 400, borderRadius: 3 } }}>
                <DialogTitle sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>Что эта роль может выдавать?</DialogTitle>
                <DialogContent sx={{ p: 2, height: 400 }}>
                    <Typography color="#94a3b8" fontSize="0.85rem" mb={2}>
                        Здесь выбираются настройки, которые этот модератор сможет менять у других ролей.
                    </Typography>
                    <Box sx={{ bgcolor: '#0f172a', borderRadius: 2, overflow: 'hidden' }}>
                        {ROLE_PERMISSIONS.flatMap(c => c.perms).filter(p => myPermissions[p.id]).map(perm => (
                            <Box key={`grant-${perm.id}`} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 1.5, borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                <Typography color="white" fontSize="0.85rem">{perm.label}</Typography>
                                <Switch size="small" checked={!!selectedRole?.grantablePermissions[perm.id]} onChange={() => toggleGrantablePermission(perm.id)} color="primary" />
                            </Box>
                        ))}
                    </Box>
                </DialogContent>
                <DialogActions sx={{ p: 2, borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                    <Button onClick={() => setGrantableDialogOpen(false)} variant="contained" sx={{ bgcolor: '#38bdf8', color: '#0f172a' }}>Готово</Button>
                </DialogActions>
            </Dialog>

            <Dialog open={banListDialogOpen} onClose={() => setBanListDialogOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', minWidth: 400, borderRadius: 3 } }}>
                <DialogTitle sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>Чёрный список ({groupData.bannedUsers?.length || 0})</DialogTitle>
                <DialogContent sx={{ p: 0, maxHeight: 400, overflowY: 'auto' }}>
                    {!groupData.bannedUsers || groupData.bannedUsers.length === 0 ? (
                        <Typography color="#94a3b8" align="center" sx={{ mt: 5 }}>Заблокированных пользователей нет.</Typography>
                    ) : (
                        <List>
                            {groupData.bannedUsers.map(user => (
                                <ListItem key={user.id} sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                    <ListItemAvatar><Avatar src={user.avatar} sx={{ bgcolor: '#ef4444' }}>{user.name[0]}</Avatar></ListItemAvatar>
                                    <ListItemText 
                                        primary={<Typography color="white" fontWeight="bold">{user.name}</Typography>} 
                                        secondary={<Typography color="#94a3b8" fontSize="0.8rem">Причина: {user.reason} • {user.date}</Typography>} 
                                    />
                                    <Button size="small" variant="outlined" onClick={() => handleUnban(user.id)} sx={{ color: '#10b981', borderColor: '#10b981', textTransform: 'none' }}>Разбанить</Button>
                                </ListItem>
                            ))}
                        </List>
                    )}
                </DialogContent>
            </Dialog>

            <Dialog open={auditDialogOpen} onClose={() => setAuditDialogOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', minWidth: 450, borderRadius: 3 } }}>
                <DialogTitle sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>Журнал действий</DialogTitle>
                <DialogContent sx={{ p: 0, maxHeight: 500, overflowY: 'auto', bgcolor: '#0f172a' }}>
                    <List>
                        {groupData.auditLog?.map(log => (
                            <ListItem key={log.id} sx={{ borderBottom: '1px solid rgba(255,255,255,0.05)', px: 3, py: 2 }}>
                                <ListItemAvatar><Avatar sx={{ width: 32, height: 32, bgcolor: '#38bdf8' }}>{log.user[0]}</Avatar></ListItemAvatar>
                                <ListItemText 
                                    primary={
                                        <Typography color="white" fontSize="0.9rem">
                                            <span style={{ fontWeight: 'bold', color: '#38bdf8' }}>{log.user}</span> {log.action}
                                        </Typography>
                                    } 
                                    secondary={<Typography color="#64748b" fontSize="0.75rem" mt={0.5}>{log.time}</Typography>} 
                                />
                            </ListItem>
                        ))}
                    </List>
                </DialogContent>
            </Dialog>

        </ThemeProvider>
    );
}