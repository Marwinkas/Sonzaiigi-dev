import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../api/axios'; 
import {
    Box,
    Paper,
    Typography,
    TextField,
    Button,
    Divider,
    InputAdornment,
    IconButton
} from '@mui/material';
import { 
    AlternateEmail, 
    Lock, 
    Visibility, 
    VisibilityOff, 
    ErrorOutline 
} from '@mui/icons-material';
import GoogleIcon from '@mui/icons-material/Google';

export default function Login() {
    const navigate = useNavigate();
    useEffect(() => { document.title = "Вход | Sonzaiigi"; }, []);

    const [data, setData] = useState({ email: '', password: '' });
    const [errors, setErrors] = useState({});
    const [processing, setProcessing] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const submit = async (e) => {
        e.preventDefault();
        setProcessing(true);
        setErrors({});

        try {
            const res = await api.post('/auth/login', data);
            localStorage.setItem('token', res.data.token);
            localStorage.setItem('user', JSON.stringify(res.data.user));
            navigate('/'); 
        } catch (err) { 
            if (err.response?.status === 401) {
                setErrors({ general: err.response.data.message });
            } else {
                setErrors({ general: "Что-то пошло не так" });
            }
        } finally {
            setProcessing(false);
        }
    };

    // Единый стиль для инпутов в стиле твоего мессенджера
    const inputStyle = {
        mb: 2,
        '& .MuiOutlinedInput-root': {
            color: 'white',
            bgcolor: '#0f172a',
            borderRadius: 2,
            '& fieldset': { borderColor: 'rgba(255,255,255,0.05)' },
            '&:hover fieldset': { borderColor: 'rgba(255,255,255,0.1)' },
            '&.Mui-focused fieldset': { borderColor: '#38bdf8' },
        },
        '& .MuiInputLabel-root': { color: '#64748b' },
        '& .MuiInputLabel-root.Mui-focused': { color: '#38bdf8' },
        '& .MuiSvgIcon-root': { color: '#64748b' },
        '& .Mui-focused .MuiSvgIcon-root': { color: '#38bdf8' },
    };

    return (
        <Box sx={{ minHeight: '100vh', bgcolor: '#0b1120', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 2 }}>
            <Paper elevation={0} sx={{ 
                width: '100%', maxWidth: 400, p: 4, 
                bgcolor: '#1e293b', 
                borderRadius: 4, 
                border: '1px solid rgba(255,255,255,0.05)',
                boxShadow: '0 10px 40px rgba(0,0,0,0.5)'
            }}>
                
                <Typography variant="h5" sx={{ color: 'white', fontWeight: 'bold', mb: 3, textAlign: 'center' }}>
                    С возвращением
                </Typography>

                {errors.general && (
                    <Box sx={{ bgcolor: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: 2, p: 1.5, mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <ErrorOutline sx={{ color: '#ef4444', fontSize: 20 }} />
                        <Typography sx={{ color: '#ef4444', fontSize: '0.85rem', fontWeight: 'bold' }}>{errors.general}</Typography>
                    </Box>
                )}

                <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<GoogleIcon />}
                    href="https://sonzaiigi.com/api/auth/google"
                    sx={{ 
                        mb: 3, py: 1.2, textTransform: 'none', borderRadius: 2,
                        color: 'white', borderColor: 'rgba(255,255,255,0.1)', bgcolor: 'rgba(255,255,255,0.02)',
                        '&:hover': { borderColor: 'rgba(255,255,255,0.2)', bgcolor: 'rgba(255,255,255,0.05)' } 
                    }}
                >
                    Войти через Google
                </Button>

                <Divider sx={{ mb: 3, borderColor: 'rgba(255,255,255,0.05)', '&::before, &::after': { borderColor: 'rgba(255,255,255,0.05)' } }}>
                    <Typography sx={{ color: '#64748b', fontSize: '0.8rem', px: 1 }}>или почта</Typography>
                </Divider>

                <Box component="form" onSubmit={submit}>
                    <TextField
                        fullWidth label="Email" type="email" required
                        value={data.email} onChange={e => setData({ ...data, email: e.target.value })}
                        sx={inputStyle}
                        InputProps={{
                            startAdornment: <InputAdornment position="start"><AlternateEmail fontSize="small" /></InputAdornment>,
                        }}
                    />

                    <TextField
                        fullWidth label="Пароль" required
                        type={showPassword ? 'text' : 'password'}
                        value={data.password} onChange={e => setData({ ...data, password: e.target.value })}
                        sx={inputStyle}
                        InputProps={{
                            startAdornment: <InputAdornment position="start"><Lock fontSize="small" /></InputAdornment>,
                            endAdornment: (
                                <InputAdornment position="end">
                                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" size="small" sx={{ color: '#64748b' }}>
                                        {showPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                                    </IconButton>
                                </InputAdornment>
                            )
                        }}
                    />

                    <Button
                        type="submit" fullWidth variant="contained" disabled={processing}
                        sx={{ 
                            mt: 2, mb: 3, py: 1.2, borderRadius: 2, fontWeight: 'bold', fontSize: '1rem', textTransform: 'none',
                            bgcolor: '#38bdf8', color: '#0f172a',
                            boxShadow: '0 4px 14px rgba(56, 189, 248, 0.4)',
                            '&:hover': { bgcolor: '#0ea5e9', boxShadow: '0 6px 20px rgba(56, 189, 248, 0.6)' },
                            '&.Mui-disabled': { bgcolor: '#1e293b', color: '#64748b' }
                        }}
                    >
                        {processing ? 'Входим...' : 'Войти'}
                    </Button>

                    <Typography sx={{ color: '#94a3b8', fontSize: '0.85rem', textAlign: 'center' }}>
                        Нет аккаунта?{' '}
                        <Link to="/register" style={{ color: '#38bdf8', textDecoration: 'none', fontWeight: 'bold' }}>
                            Зарегистрироваться
                        </Link>
                    </Typography>
                </Box>
            </Paper>
        </Box>
    );
}