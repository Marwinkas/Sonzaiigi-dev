import React, { useState, useEffect, useRef,useCallback,useContext   } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import {
    Box, Container, Paper, Typography, Avatar, IconButton,
    InputBase, List, ListItemButton, ListItemAvatar, ListItemText,
    Divider, Tooltip, CircularProgress,
    Menu, MenuItem, Dialog, Popover, Button, Checkbox, FormControlLabel,
} from '@mui/material';
import {
    Send, Search, Reply, Close as CloseIcon,
    ContentCopy, Delete, Check, Edit, PushPin, ArrowDownward,
    Person, AttachFile, Mood, GifBox, AddCircle,
    AddReaction, Download, Star, StarBorder, Forward, DoneAll, Done, AccessTime, HourglassEmpty
} from '@mui/icons-material';
import EmojiPicker from 'emoji-picker-react';
import { Grid } from '@giphy/react-components';
import { GiphyFetch } from '@giphy/js-fetch-api';
import { Mic } from '@mui/icons-material';
import theme from '../theme';
import Header from '../Components/Header';
import ProfileModal from '../Components/ProfileModal';
import SettingsModal from '../Components/SettingsModal';
import api from '../api/axios';
import GroupProfileModal from '../Components/GroupProfileModal';
import { PlayerContext } from '../Components/PlayerContext';
import { useParams, useNavigate } from 'react-router-dom';
const gf = new GiphyFetch('fkmVTY91WMHE0sB6SihowYEqcmYKn8qi');
import {  Lock, DeleteOutline } from '@mui/icons-material'; // Добавили Lock и DeleteOutline
import { PlayArrow, Pause, MusicNote } from '@mui/icons-material';
import {  VolumeUp, VolumeOff,InsertDriveFile,BarChart } from '@mui/icons-material';
import { Slider } from '@mui/material';
// Читаем громкость из кэша (по умолчанию 1, то есть 100%)

// ✨ Кастомный Telegram-like Аудио Плеер
const CustomAudioPlayer = ({ src, masterSrc, title, artist, cover, duration, sizeText, pending, uploadProgress, onCancel, globalVolume, onVolumeChange })=> {
    const [isPlaying, setIsPlaying] = useState(false);
    const [currentTime, setCurrentTime] = useState(0);
    const [audioDuration, setAudioDuration] = useState(0);
    const [volume, setVolume] = useState(1);
    const [isMuted, setIsMuted] = useState(false);
    const audioRef = useRef(null);
useEffect(() => {
        if (audioRef.current) {
            audioRef.current.volume = isMuted ? 0 : globalVolume;
        }
    }, [globalVolume, isMuted]);
    const togglePlay = (e) => {
        e.stopPropagation();
        if (pending) return; // Блокируем клик, пока грузится
        if (isPlaying) audioRef.current.pause();
        else audioRef.current.play();
        setIsPlaying(!isPlaying);
    };

    const handleTimeUpdate = () => setCurrentTime(audioRef.current.currentTime);
    const handleLoadedMetadata = () => setAudioDuration(audioRef.current.duration);

    const handleSeek = (e, newValue) => {
        audioRef.current.currentTime = newValue;
        setCurrentTime(newValue);
    };

    const handleVolumeChange = (e, newValue) => {
        const vol = newValue / 100;
        onVolumeChange(vol); // ✨ Вызываем глобальную функцию
        setIsMuted(vol === 0);
    };

    const toggleMute = () => setIsMuted(!isMuted);

    const formatTime = (time) => {
        if (!time || isNaN(time)) return "0:00";
        const min = Math.floor(time / 60);
        const sec = Math.floor(time % 60);
        return `${min}:${sec < 10 ? '0' : ''}${sec}`;
    };

    return (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, minWidth: '320px', maxWidth: '1000px' }}>
            <audio 
                ref={audioRef} src={src} 
                onTimeUpdate={handleTimeUpdate} onLoadedMetadata={handleLoadedMetadata}
                onEnded={() => setIsPlaying(false)} 
                style={{ display: 'none' }} 
            />

            {/* АВАТАРКА С ПЛЕЕРОМ ИЛИ ПРОГРЕССОМ */}
            <Box sx={{ position: 'relative', width: 52, height: 52, flexShrink: 0 }}>
                <Avatar src={cover} sx={{ width: '100%', height: '100%', bgcolor: '#38bdf8', color: '#0f172a' }}>
                    {!cover && <MusicNote />}
                </Avatar>
                
                <Box 
                    onClick={pending ? onCancel : togglePlay}
                    sx={{
                        position: 'absolute', top: 0, left: 0, width: '100%', height: '100%',
                        bgcolor: isPlaying || pending ? 'rgba(0,0,0,0.5)' : 'rgba(0,0,0,0.3)', 
                        borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
                        cursor: 'pointer', transition: 'all 0.2s', '&:hover': { bgcolor: 'rgba(0,0,0,0.6)' }
                    }} 
                >
                    {pending ? (
                        <>
                            <CircularProgress variant="determinate" value={uploadProgress} size={48} sx={{ color: '#38bdf8', position: 'absolute' }} />
                            <CloseIcon sx={{ color: 'white', fontSize: 24 }} /> {/* Крестик отмены */}
                        </>
                    ) : isPlaying ? <Pause sx={{ color: 'white', fontSize: 30 }}/> : <PlayArrow sx={{ color: 'white', fontSize: 30 }}/>}
                </Box>
            </Box>

            {/* ИНФОРМАЦИЯ И ПОЛЗУНКИ */}
            <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography sx={{ color: 'white', fontWeight: 'bold', fontSize: '0.9rem', lineHeight: 1.1 }} noWrap>
                        {artist} – {title}
                    </Typography>
                    {masterSrc && !pending && (
                        <IconButton href={masterSrc} download target="_blank" size="small" sx={{ p: 0.5, color: '#94a3b8', '&:hover': { color: '#38bdf8' } }}>
                            <Download fontSize="small" sx={{ fontSize: 18 }} />
                        </IconButton>
                    )}
                </Box>
                
                {/* ПОЛЗУНОК ВРЕМЕНИ */}
                {!pending ? (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5,pr:"10px" }}>
                        <Typography sx={{ color: '#38bdf8', fontSize: '0.75rem', minWidth: '30px' }}>
                            {formatTime(currentTime)}
                        </Typography>
                        <Slider 
                            size="medium" min={0} max={audioDuration || 100} value={currentTime} onChange={handleSeek}
                            sx={{ color: '#38bdf8', p: 0, '& .MuiSlider-thumb': { width: 10, height: 10, display: 'none' }, '&:hover .MuiSlider-thumb': { display: 'block' } }} 
                        />
                        <Typography sx={{ color: '#94a3b8', fontSize: '0.75rem', minWidth: '30px', textAlign: 'right' }}>
                            {formatTime(audioDuration)}
                        </Typography>
                        
                        {/* ГРОМКОСТЬ */}
                        <Box sx={{ display: 'flex', alignItems: 'center', ml: 1, width: 120 }}>
                            <IconButton size="small" onClick={toggleMute} sx={{ p: 0, mr: 0.5, color: '#94a3b8' }}>
                                {isMuted ? <VolumeOff sx={{ fontSize: 16 }} /> : <VolumeUp sx={{ fontSize: 16 }} />}
                            </IconButton>
                            <Slider size="small" min={0} max={100} value={isMuted ? 0 : globalVolume * 100} onChange={handleVolumeChange} sx={{ color: '#94a3b8', p: 0, '& .MuiSlider-thumb': { width: 8, height: 8 } }} />
                        </Box>
                    </Box>
                ) : (
                    <Typography sx={{ color: '#38bdf8', fontSize: '0.8rem', mt: 0.5 }}>
                        Загрузка... {uploadProgress}% • {sizeText}
                    </Typography>
                )}
            </Box>
        </Box>
    );
};

export default function Messenger() {
    const [currentUser, setCurrentUser] = useState(() => {
        const userString = localStorage.getItem('user');
        return userString ? JSON.parse(userString) : null;
    });
const [globalVolume, setGlobalVolume] = useState(() => {
    const saved = localStorage.getItem('sonzaiigi_volume');
    return saved !== null ? parseFloat(saved) : 1;
});

// Функция для обновления и сохранения громкости
const handleGlobalVolumeChange = (newVolume) => {
    setGlobalVolume(newVolume);
    localStorage.setItem('sonzaiigi_volume', newVolume);
};
    const auth = { user: currentUser };

    useEffect(() => {
        document.title = "Сообщения | Sonzaiigi";
    }, []);
const [isImportStickersOpen, setIsImportStickersOpen] = useState(false);
const [stickerPackLink, setStickerPackLink] = useState('');
const [isImporting, setIsImporting] = useState(false);
    const [lastWsMessage, setLastWsMessage] = useState(null);
    const [value] = useState(30);
    const [chats, setChats] = useState([]);
    const [activeChat, setActiveChat] = useState(null);

    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [loadingMore, setLoadingMore] = useState(false);

    // Модалка создания группы
    const [isCreateGroupOpen, setIsCreateGroupOpen] = useState(false);
    const [newGroupName, setNewGroupName] = useState('');
    const [newGroupDesc, setNewGroupDesc] = useState('');
    const [newGroupAvatar, setNewGroupAvatar] = useState(null);
    const [newGroupAvatarPreview, setNewGroupAvatarPreview] = useState(null);

    const [hasMore, setHasMore] = useState(true);
    const [hasMoreDown, setHasMoreDown] = useState(false);
    const [isHistoryMode, setIsHistoryMode] = useState(false);

    const scrollRef = useRef(null);
    const lastScrollHeight = useRef(0);
    const lastMessageId = useRef(null);

    const [replyTo, setReplyTo] = useState(null);
    const [editingMessage, setEditingMessage] = useState(null);
    const [contextMenu, setContextMenu] = useState(null);

    const fileInputRef = useRef(null);
    const [attachment, setAttachment] = useState(null);
    const [attachmentPreview, setAttachmentPreview] = useState(null);
    const [attachmentType, setAttachmentType] = useState(null);
    const [fullScreenImage, setFullScreenImage] = useState(null);

    const [pinnedMessages, setPinnedMessages] = useState([]);
    const [currentPinIndex, setCurrentPinIndex] = useState(0);
    const [unreadMessages, setUnreadMessages] = useState([]);
    const [mentionIds, setMentionIds] = useState([]);
    const [showScrollDown, setShowScrollDown] = useState(false);

    const [headerMenuAnchor, setHeaderMenuAnchor] = useState(null);
    const [isProfileModalOpen, setIsProfileModalOpen] = useState(false);

    const [emojiAnchor, setEmojiAnchor] = useState(null);
    const [gifAnchor, setGifAnchor] = useState(null);
    const [gifSearch, setGifSearch] = useState('');

    const [reactionPickerAnchor, setReactionPickerAnchor] = useState(null);
    const [reactingMessageId, setReactingMessageId] = useState(null);

    const [isForwardModalOpen, setIsForwardModalOpen] = useState(false);
    const [selectedChatsForForward, setSelectedChatsForForward] = useState([]);
    const [includeAuthor, setIncludeAuthor] = useState(true);
    const [forwardMessageId, setForwardMessageId] = useState(null);
const isCancelledRef = useRef(false); // ✨ Флаг: была ли отменена запись
    const [favoriteGifs, setFavoriteGifs] = useState([]);
    const [typingUsers, setTypingUsers] = useState({});
    const wsRef = useRef(null);
    const lastTypingTime = useRef(0);
    const [onlineCount, setOnlineCount] = useState(0);
    const [selectedUser, setSelectedUser] = useState(null);
    const [settingsOpen, setSettingsOpen] = React.useState(false);

    // Стейты для удаления
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteForEveryone, setDeleteForEveryone] = useState(false);
    const [messageToDelete, setMessageToDelete] = useState(null);
    const [isGroupProfileOpen, setIsGroupProfileOpen] = useState(false);
const mediaCloseTimer = useRef(null);
    const [mediaAnchor, setMediaAnchor] = useState(null);
const [mediaTab, setMediaTab] = useState(0); // 0 - эмодзи, 1 - стикеры, 2 - гифки
const [isMediaPinned, setIsMediaPinned] = useState(false); // Чтобы окно не закрывалось после клика

const [isRecording, setIsRecording] = useState(false);
const [recordingTime, setRecordingTime] = useState(0);
const [isRecordingLocked, setIsRecordingLocked] = useState(false); // ✨ Блокировка записи
const [dragOffset, setDragOffset] = useState(0); // ✨ Сдвиг для анимации замка
const mediaRecorderRef = useRef(null);
const audioChunksRef = useRef([]);
const recordingTimerRef = useRef(null);

const startYRef = useRef(0);
const audioContextRef = useRef(null);
const analyserRef = useRef(null);
const animationFrameRef = useRef(null);
const micButtonRef = useRef(null); // Чтобы анимировать кнопку без тормозов React
const [attachAnchor, setAttachAnchor] = useState(null);
const attachCloseTimer = useRef(null);
const allFileInputRef = useRef(null); // Для документов (все файлы)
const { playTrack } = useContext(PlayerContext);
// Функции для управления всплывающим меню
const handleAttachOpen = (e) => {
    if (attachCloseTimer.current) clearTimeout(attachCloseTimer.current);
    setAttachAnchor(e.currentTarget);
};

const handleAttachClose = () => {
    attachCloseTimer.current = setTimeout(() => {
        setAttachAnchor(null);
    }, 300); // Даем 300мс, чтобы перевести курсор
};

const handleAttachEnterMenu = () => {
    if (attachCloseTimer.current) clearTimeout(attachCloseTimer.current);
};
// Вспомогательная функция для таймера (если ее еще нет)
const formatTimeStr = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
};

// 1. Стейт ширины (берем из локального хранилища или ставим 350 по умолчанию)
const [leftPanelWidth, setLeftPanelWidth] = useState(() => {
    const saved = localStorage.getItem('sonzaiigi_sidebar_width');
    return saved !== null ? parseInt(saved) : 350;
});

const isResizing = useRef(false);

// 2. Начало изменения размера
const startResizing = (e) => {
    isResizing.current = true;
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
};

// 3. Конец изменения размера
const stopResizing = useCallback(() => {
    isResizing.current = false;
    document.body.style.cursor = 'default';
    document.body.style.userSelect = 'auto';
    // Сохраняем финальный результат в память
    localStorage.setItem('sonzaiigi_sidebar_width', leftPanelWidth);
}, [leftPanelWidth]);

// 4. Сам процесс перемещения мыши
const resize = useCallback((e) => {
    if (!isResizing.current) return;
    
    let newWidth = e.clientX;

    // ✨ ЛОГИКА СХЛОПЫВАНИЯ (Snapping)
    if (newWidth < 150) {
        newWidth = 80; // Режим "Только иконки"
    } else if (newWidth < 200) {
        newWidth = 200; // Минимальный порог для текста
    } else if (newWidth > 600) {
        newWidth = 600; // Максимальная ширина
    }
    
    setLeftPanelWidth(newWidth);
}, []);

useEffect(() => {
    window.addEventListener('mousemove', resize);
    window.addEventListener('mouseup', stopResizing);
    return () => {
        window.removeEventListener('mousemove', resize);
        window.removeEventListener('mouseup', stopResizing);
    };
}, [resize, stopResizing]);

const isCollapsed = leftPanelWidth < 100;

// 5. Вешаем глобальные слушатели
useEffect(() => {
    window.addEventListener('mousemove', resize);
    window.addEventListener('mouseup', stopResizing);
    return () => {
        window.removeEventListener('mousemove', resize);
        window.removeEventListener('mouseup', stopResizing);
    };
}, [resize]);

const formatToLocalTime = (utcDate) => {
    if (!utcDate) return "";
    // Создаем объект даты. Браузер поймет, что это UTC, и сам прибавит нужные часы (+3 для тебя)
    const date = new Date(utcDate);
    
    return date.toLocaleTimeString([], { 
        hour: '2-digit', 
        minute: '2-digit' 
    });
};
const startRecording = async (e) => {
    if (e) e.preventDefault();
    if (mediaRecorderRef.current && mediaRecorderRef.current.state === "recording") return;

    // ✨ Запоминаем точку, где юзер коснулся экрана/мышки
    startYRef.current = e.touches ? e.touches[0].clientY : e.clientY;
    setIsRecordingLocked(false);
    setDragOffset(0);
    isCancelledRef.current = false;
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        
        // ✨ WEB AUDIO API: Подключаемся к микрофону, чтобы слушать громкость
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        audioContextRef.current = audioCtx;
        const source = audioCtx.createMediaStreamSource(stream);
        const analyser = audioCtx.createAnalyser();
        analyser.fftSize = 256;
        source.connect(analyser);
        analyserRef.current = analyser;

        const dataArray = new Uint8Array(analyser.frequencyBinCount);
        
        // ✨ ФУНКЦИЯ АНИМАЦИИ: Работает 60 кадров в секунду
        const animateMic = () => {
            if (!analyserRef.current) return;
            analyserRef.current.getByteFrequencyData(dataArray);
            
            // Высчитываем среднюю громкость (от 0 до 255)
            let sum = 0;
            for (let i = 0; i < dataArray.length; i++) sum += dataArray[i];
            let average = sum / dataArray.length;
            
            // Превращаем громкость в масштаб (от 1.0 до 1.4)
            let scale = 1 + (average / 255) * 0.4;
            
            // Меняем размер кнопки напрямую в DOM (чтобы не тормозил React)
            if (micButtonRef.current) {
                micButtonRef.current.style.transform = `scale(${scale})`;
            }
            
            animationFrameRef.current = requestAnimationFrame(animateMic);
        };
        animateMic(); // Запускаем цикл

        const mediaRecorder = new MediaRecorder(stream);
        mediaRecorderRef.current = mediaRecorder;
        audioChunksRef.current = [];

        mediaRecorder.ondataavailable = (event) => {
            if (event.data.size > 0) audioChunksRef.current.push(event.data);
        };

        mediaRecorder.onstop = () => {
            if (isCancelledRef.current) {
                console.log("Запись отменена, файл не будет отправлен");
                return; 
            }
            const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
            if (audioBlob.size > 10000) {
                const audioFile = new File([audioBlob], `Голосовое.webm`, { type: 'audio/webm' });
                handleSendMessage(null, null, audioFile);
            }
            stream.getTracks().forEach(track => track.stop());
            setRecordingTime(0);
        };

        mediaRecorder.start();
        setIsRecording(true);
        setRecordingTime(0);

        if (recordingTimerRef.current) clearInterval(recordingTimerRef.current);
        recordingTimerRef.current = setInterval(() => setRecordingTime(prev => prev + 1), 1000);

    } catch (err) {
        console.error("Ошибка:", err);
    }
};

// Функция остановки теперь принимает флаг `force` (принудительно) или `cancel` (отмена)
const stopRecording = (e, force = false, cancel = false) => {
    if (e) e.preventDefault();
    
    // ✨ Если запись заблокирована (свайп вверх), обычное отпускание пальца её НЕ остановит!
    if (isRecordingLocked && !force && !cancel) return;

    if (mediaRecorderRef.current && mediaRecorderRef.current.state === "recording") {
        if (cancel) {
            isCancelledRef.current = true;
        }
        setTimeout(() => {
            if (mediaRecorderRef.current && mediaRecorderRef.current.state === "recording") {
                // Если отмена — очищаем чанки, чтобы onstop ничего не отправил
                if (cancel) audioChunksRef.current = []; 
                
                mediaRecorderRef.current.stop();
                setIsRecording(false);
                setIsRecordingLocked(false);
                setDragOffset(0);
                
                if (recordingTimerRef.current) {
                    clearInterval(recordingTimerRef.current);
                    recordingTimerRef.current = null;
                }

                // Выключаем Web Audio API и сбрасываем размер кнопки
                if (animationFrameRef.current) cancelAnimationFrame(animationFrameRef.current);
                if (audioContextRef.current) audioContextRef.current.close();
                if (micButtonRef.current) micButtonRef.current.style.transform = 'scale(1)';
                
                if (mediaRecorderRef.current.stream) {
                    mediaRecorderRef.current.stream.getTracks().forEach(track => track.stop());
                }
            }
        }, cancel ? 0 : 400); // Если отмена - моментально, если отправка - ждем 400мс для "хвоста"
    }
};
const handleRecordingMove = (e) => {
    if (!isRecording || isRecordingLocked) return;
    
    // Получаем текущую координату (мышь или палец)
    const currentY = e.touches ? e.touches[0].clientY : e.clientY;
    const diff = startYRef.current - currentY;

    // Если потянули вверх больше чем на 10 пикселей - показываем анимацию замка
    if (diff > 10) {
        setDragOffset(-diff); 
    }

    // Если потянули вверх на 60 пикселей — БЛОКИРУЕМ ЗАПИСЬ!
    if (diff > 60) {
        setIsRecordingLocked(true);
        setDragOffset(0);
    }
};
useEffect(() => {
    // Если запись идет, но еще не закреплена — слушаем перемещения и отпускание везде
    if (isRecording && !isRecordingLocked) {
        const handleGlobalMove = (e) => handleRecordingMove(e);
        const handleGlobalUp = (e) => stopRecording(e, false);

        window.addEventListener('mousemove', handleGlobalMove);
        window.addEventListener('mouseup', handleGlobalUp);
        window.addEventListener('touchmove', handleGlobalMove);
        window.addEventListener('touchend', handleGlobalUp);

        return () => {
            window.removeEventListener('mousemove', handleGlobalMove);
            window.removeEventListener('mouseup', handleGlobalUp);
            window.removeEventListener('touchmove', handleGlobalMove);
            window.removeEventListener('touchend', handleGlobalUp);
        };
    }
}, [isRecording, isRecordingLocked]); // Перезапускаем при смене состояний
    useEffect(() => {
        const fetchGifs = async () => {
            const endpoint = gifSearch.trim() 
                ? `https://tenor.googleapis.com/v2/search?q=${gifSearch}&key=${TENOR_API_KEY}&limit=20`
                : `https://tenor.googleapis.com/v2/featured?key=${TENOR_API_KEY}&limit=20`;

            try {
                const res = await fetch(endpoint); 
                const data = await res.json();
                setTenorGifs(data.results || []);
            } catch (e) {
                console.error("Tenor error:", e);
            }
        };

        // Небольшой дебаунс, чтобы не спамить при каждом символе
        const timer = setTimeout(fetchGifs, 500);
        return () => clearTimeout(timer);
    }, [gifSearch]);
const handleMediaOpen = (e) => {
    if (mediaCloseTimer.current) clearTimeout(mediaCloseTimer.current);
    setMediaAnchor(e.currentTarget);
};
const [tenorGifs, setTenorGifs] = useState([]);
const TENOR_API_KEY = "ТВОЙ_API_KEY"; // Получи на Google Cloud Console (Tenor API v2)
const handleMediaClose = () => {
    if (isMediaPinned) return;
    
    // Даем небольшую паузу перед закрытием
    mediaCloseTimer.current = setTimeout(() => {
        setMediaAnchor(null);
    }, 300); 
};
const handleMediaClick = () => {
    setIsMediaPinned(true); // Фиксируем окно
};
// Функция, которая отменит закрытие, если мы навели на само окно
const handleMediaEnterWindow = () => {
    if (mediaCloseTimer.current) clearTimeout(mediaCloseTimer.current);
};

// Закрытие при клике вне окна (добавь в существующие обработчики закрытия)
const handleFullClose = () => {
    setMediaAnchor(null);
    setIsMediaPinned(false);
};
    const navigate = useNavigate();

    useEffect(() => {
        // Если пользователя нет — кидаем на логин
        if (!currentUser) {
            navigate('/login');
            return;
        }
        document.title = "Сообщения | Sonzaiigi";
    }, [currentUser, navigate]);
    const [cooldown, setCooldown] = useState(0); // Сколько секунд осталось ждать
    const handleLocalChatRemove = (chatId) => {
        setChats(prev => prev.filter(c => c.id !== chatId)); // Убираем из списка слева
        if (activeChat?.id === chatId) {
            setActiveChat(null); // Закрываем текущий чат
            setMessages([]);     // Чистим сообщения
            navigate('/messenger');
        }
    };
    const { chatId } = useParams(); 
    // Этот эффект будет тикать каждую секунду и уменьшать таймер
    useEffect(() => {
        if (cooldown <= 0) return;
        const timer = setInterval(() => setCooldown(prev => prev - 1), 1000);
        return () => clearInterval(timer);
    }, [cooldown]);

    const throttleTyping = () => {
        const now = Date.now();
        if (now - lastTypingTime.current > 3000) {
            sendTypingStatus();
            lastTypingTime.current = now;
        }
    };
    const handleChatClick = (chat) => {
        navigate(`/messenger/${chat.id}`);
        
    };
    const sendTypingStatus = () => {
        if (!wsRef.current || wsRef.current.readyState !== 1 || !activeChat) return;
        wsRef.current.send(JSON.stringify({
            type: 'typing',
            chat_id: activeChat.id,
            user_id: auth.user.id,
            username: auth.user.name
        }));
    };

    const activeChatRef = useRef(activeChat);
    useEffect(() => {
        activeChatRef.current = activeChat;
    }, [activeChat]);
    useEffect(() => {
        if (chats.length > 0 && chatId) {
            // Ищем чат в списке по ID из ссылки
            const chatFromUrl = chats.find(c => c.id === parseInt(chatId));
            if (chatFromUrl) {
                setActiveChat(chatFromUrl);
            }
        }
    }, [chatId, chats]); // Следим за изменением ссылки и списка чатов
useEffect(() => {
    if (activeChat?.id && messages.length > 0) {
        const lastMsg = messages[messages.length - 1];
        
        // Читаем, только если последнее сообщение ЧУЖОЕ и оно ВИДИМО (мы внизу)
        if (lastMsg.senderId !== auth.user.id) {
            const container = scrollRef.current;
            if (container) {
                const scrollFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
                // Если мы реально видим низ (меньше 100px до края)
                if (scrollFromBottom < 100) {
                    markAsRead(activeChat.id);
                }
            }
        }
    }
}, [messages.length, activeChat?.id]);
    useEffect(() => {
        let ws;
        let reconnectTimer;

        const connect = () => {
            if (ws) ws.close();
            const wsUrl = `wss://sonzaiigi.com/ws?user_id=${auth.user.id}`;
            ws = new WebSocket(wsUrl);
            wsRef.current = ws;

            ws.onopen = () => console.log('🚀 Go WebSocket: Соединение установлено!');

            ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    setLastWsMessage(data);
                    handleIncomingWSMessage(data);
                    if (data.type === 'chat_roles_updated') {
                        // Тихонько перезапрашиваем список чатов, чтобы обновить все права
                        api.get('/chats').then(response => {
                            setChats(response.data.chats);
                            if (activeChatRef.current?.id === data.chat_id) {
                                const updatedChat = response.data.chats.find(c => c.id === data.chat_id);
                                if (updatedChat) setActiveChat(updatedChat);
                            }
                        });
                    }
                    if (data.type === 'permissions_updated') {
                        const { chat_id, target_user_id, permissions } = data;

                        // Обновляем UI только если права изменились лично у нас
                        // (или если target_user_id не передан — значит права роли поменялись для всех)
                        if (!target_user_id || target_user_id === auth.user.id) {

                            // Обновляем список чатов
                            setChats(prev => prev.map(c =>
                                c.id === chat_id
                                    ? { ...c, permissions: permissions, can_reply: permissions.sendMessages }
                                    : c
                            ));

                            // Обновляем текущий открытый чат, чтобы кнопки тут же появились/исчезли
                            if (activeChatRef.current?.id === chat_id) {
                                setActiveChat(prev => ({
                                    ...prev,
                                    permissions: permissions,
                                    can_reply: permissions.sendMessages
                                }));
                            }
                        }
                    }
                    if (data.type === 'online_count') setOnlineCount(data.data);

                    if (data.type === 'reaction_update') {
                        const { message_id, reactions } = data;
                        setMessages(prev => prev.map(m => {
                            if (m.id === message_id) {
                                return {
                                    ...m,
                                    reactions: reactions.map(r => ({
                                        emoji: r.emoji,
                                        count: r.count,
                                        reacted_by_me: r.userIds.includes(auth.user.id)
                                    }))
                                };
                            }
                            return m;
                        }));
                    }

                    if (data.type === 'message_deleted') {
                        setMessages(prev => prev.filter(m => m.id !== data.message_id));
                    }

                    if (data.type === 'message_updated') {
                        setMessages(prev => prev.map(m =>
                            m.id === data.message_id ? { ...m, text: data.text, is_edited: true } : m
                        ));
                    }

                    if (data.type === 'chat_relation_update') {
                        const { chat_id, can_reply } = data;
                        setChats(prev => prev.map(c => c.id === chat_id ? { ...c, can_reply: can_reply } : c));
                        if (activeChatRef.current?.id === chat_id) {
                            setActiveChat(prev => ({ ...prev, can_reply: can_reply }));
                        }
                    }

                    if (data.type === 'message_pinned') {
                        setMessages(prev => prev.map(m => m.id === data.message_id ? { ...m, is_pinned: !!data.pinned_at } : m));
                        if (activeChatRef.current) {
                            api.get(`/messages/${activeChatRef.current.id}/pins`).then(res => setPinnedMessages(res.data));
                        }
                    }

                    if (data.type === 'user_updated') {
                        const { user_id, name, avatar, username } = data;
                        setMessages(prev => prev.map(m => {
                            if (m.senderId === user_id) {
                                return { ...m, senderName: name, senderAvatar: avatar, senderUsername: username };
                            }
                            if (m.reply_to && m.reply_to.senderId === user_id) {
                                return { ...m, reply_to: { ...m.reply_to, name: name } };
                            }
                            return m;
                        }));

                        setChats(prev => prev.map(c => {
                            if (!c.is_group && c.user && c.user.Id === user_id) {
                                return { ...c, name: name, avatar: avatar, user: { ...c.user, Name: name, Username: username } };
                            }
                            return c;
                        }));
                    }

                    if (data.type === 'group_updated') {
                        const { chat_id, name, avatar, description } = data; // Добавляем description из бэкенда
                        setChats(prev => prev.map(c =>
                            c.id === chat_id ? { ...c, name: name, avatar: avatar, description: description } : c
                        ));
                        if (activeChatRef.current?.id === chat_id) {
                            setActiveChat(prev => ({ ...prev, name: name, avatar: avatar, description: description }));
                        }
                    }
                    if (data.type === 'member_kicked') {
                        const { chat_id, kicked_user_id } = data;
                        // Логика обновления участников (если требуется)
                    }

                    if (data.type === 'typing') {
                        const { chat_id, user_id, username } = data;
                        if (user_id !== auth.user.id) {
                            const nameToShow = username || "Кто-то";
                            setTypingUsers(prev => ({ ...prev, [chat_id]: { ...prev[chat_id], [user_id]: nameToShow } }));
                            setTimeout(() => {
                                setTypingUsers(prev => {
                                    const chatCopy = { ...prev[chat_id] };
                                    delete chatCopy[user_id];
                                    return { ...prev, [chat_id]: chatCopy };
                                });
                            }, 3000);
                        }
                    }

                    if (data.type === 'chat_created') {
                        if (data.target_user_id === auth.user.id) {
                            setChats(prev => {
                                if (prev.some(c => c.id === data.chat.id)) return prev;
                                const normalizedChat = {
                                    ...data.chat,
                                    user: data.chat.user ? {
                                        id: data.chat.user.id || data.chat.user.Id,
                                        name: data.chat.user.name || data.chat.user.Name,
                                        username: data.chat.user.username || data.chat.user.Username,
                                        avatar: data.chat.user.avatar || data.chat.user.Avatar
                                    } : null
                                };
                                return [normalizedChat, ...prev];
                            });
                        }
                    }

                    if (data.type === 'chat_removed') {
                        if (data.target_user_id && data.target_user_id !== auth.user.id) return;
                        setChats(prev => prev.filter(c => c.id !== data.chat_id));
                        if (activeChatRef.current?.id === data.chat_id) {
                            setActiveChat(null);
                            setMessages([]);
                        }
                    }

                    if (data.type === 'messages_read') {
                        if (activeChatRef.current?.id === data.chat_id) {
                            setMessages(prev => prev.map(m => ({ ...m, is_read: true })));
                        }
                    }
                } catch (e) {
                    console.error("Ошибка парсинга данных из сокета", e);
                }
            };

            ws.onerror = (err) => {
                console.error('❌ Ошибка WebSocket:', err);
                ws.close();
            };

            ws.onclose = (e) => {
                console.log('🔴 Соединение разорвано. Пробую переподключиться через 3 секунды...');
                reconnectTimer = setTimeout(() => { connect(); }, 3000);
            };
        };

        connect();
        return () => {
            if (ws) { ws.onclose = null; ws.close(); }
            clearTimeout(reconnectTimer);
        };
    }, []);
const [isPollModalOpen, setIsPollModalOpen] = useState(false);
const [pollQuestion, setPollQuestion] = useState('');
const [pollOptions, setPollOptions] = useState([{ id: 1, text: '', imageUrl: '' }, { id: 2, text: '', imageUrl: '' }]);
const [pollMultipleChoice, setPollMultipleChoice] = useState(false);

const handleAddPollOption = () => {
    if (pollOptions.length >= 10) return; // Максимум 10 вариантов
    setPollOptions([...pollOptions, { id: Date.now(), text: '', imageUrl: '' }]);
};

const handleSendPoll = () => {
    const validOptions = pollOptions.filter(o => o.text.trim() !== '');
    if (!pollQuestion.trim() || validOptions.length < 2) return alert("Введите вопрос и минимум 2 варианта");

    const pollData = {
        Question: pollQuestion,
        IsMultipleChoice: pollMultipleChoice,
        Options: []
    };

    const formData = new FormData();
    let imageCounter = 0;

    // ✨ Собираем картинки из локального стейта и кладем в FormData
    validOptions.forEach(o => {
        const optData = { Id: o.id.toString(), Text: o.text, Voters: [] };
        
        if (o.imageFile) {
            formData.append('poll_images', o.imageFile);
            optData.ImageIndex = imageCounter;
            imageCounter++;
        }
        pollData.Options.push(optData);
    });

    formData.append('poll_json', JSON.stringify(pollData));
    
    // Закрываем и чистим модалку до отправки
    setIsPollModalOpen(false);
    setPollQuestion('');
    setPollOptions([{ id: 1, text: '', imageFile: null, imagePreview: null }, { id: 2, text: '', imageFile: null, imagePreview: null }]);

    api.post(`/messages/${activeChat.id}`, formData).then(res => {
        // ✨ ФИКС ДУБЛИКАТОВ: Проверяем, не пришло ли оно уже по сокетам
        setMessages(prev => {
            if (prev.some(m => m.id === res.data.id)) return prev;
            return [...prev, res.data];
        });
        setTimeout(() => scrollToBottom(true), 100);
    });
};
    const handleIncomingWSMessage = (data) => {
        if (data.type === 'new_message') {
            const incomingMsg = data.message;
            const targetChatId = data.chat_id;

            setChats(prev => {
                const chatIndex = prev.findIndex(c => c.id === targetChatId);
                if (chatIndex === -1) return prev;

                const updatedChat = { ...prev[chatIndex] };
                const isNotActive = activeChatRef.current?.id !== targetChatId;

                updatedChat.lastMessage = incomingMsg.text || 'Вложение';
                updatedChat.time = incomingMsg.time;

                if (isNotActive) {
                    updatedChat.unread_count = (updatedChat.unread_count || 0) + 1;
                    const mentionTag = `@${auth.user.username}`;
                    if (incomingMsg.text && incomingMsg.text.includes(mentionTag)) {
                        updatedChat.pings_count = (updatedChat.pings_count || 0) + 1;
                    }
                }

                const otherChats = prev.filter(c => c.id !== targetChatId);
                return [updatedChat, ...otherChats];
            });

        if (activeChatRef.current?.id === targetChatId) {
                    setMessages(prev => {
                        if (prev.some(m => m.id === incomingMsg.id)) return prev;

                        // ✨ ВОТ ЗДЕСЬ МАГИЯ:
                        // Если senderId совпадает с твоим ID — скроллим принудительно.
                        // Если пишет кто-то другой — скроллим, только если ты уже внизу.
                        const isMyMessage = incomingMsg.senderId === auth.user.id;
                        setTimeout(() => scrollToBottom(isMyMessage), 100);

                        if (incomingMsg.senderId === auth.user.id) {
                            const pendingMsg = prev.find(m => m.pending === true);
                            if (pendingMsg) return prev.map(m => m.pending === true ? incomingMsg : m);
                        }

                        return [...prev, incomingMsg];
                    });
                }
        }
    };

    const getTypingText = () => {
        if (!activeChat) return "";
        const users = Object.values(typingUsers[activeChat.id] || {});
        if (users.length === 0) return activeChat.is_group ? "" : "";
        if (users.length === 1) return `${users[0]} пишет...`;
        return "Несколько человек пишут...";
    };

    useEffect(() => {
        api.get('/chats').then(response => {
            setChats(response.data.chats);
        }).catch(err => console.error("Ошибка при загрузке списка чатов:", err));
    }, []);

    const handleForward = () => {
        api.post('/messages/forward', {
            message_id: forwardMessageId,
            conversation_ids: selectedChatsForForward,
            include_author: includeAuthor
        }).then(() => {
            setIsForwardModalOpen(false);
            setSelectedChatsForForward([]);
        });
    };

    useEffect(() => {
        localStorage.setItem('favoriteGifs', JSON.stringify(favoriteGifs));
    }, [favoriteGifs]);

    const groupMessages = (msgs) => {
        return msgs.reduce((groups, msg, i) => {
            // ✨ ПРОВЕРЯЕМ ВРЕМЯ: Группируем только если это тот же человек И та же минута
            const isSameGroup = i > 0 
                && msgs[i - 1].senderId === msg.senderId 
                && msgs[i - 1].time === msg.time;
                
            if (isSameGroup) {
                groups[groups.length - 1].push(msg);
            } else {
                groups.push([msg]);
            }
            return groups;
        }, []);
    };

const handleCreateGroup = () => {
        if (!newGroupName.trim()) return;

        const formData = new FormData();
        formData.append('Name', newGroupName);
        if (newGroupDesc) formData.append('Description', newGroupDesc);
        if (newGroupAvatar) formData.append('Avatar', newGroupAvatar);

        // ✨ УБРАЛИ headers, Axios сам всё сделает идеально!
        api.post('/groups/create', formData).then(res => {
            setIsCreateGroupOpen(false);
            setNewGroupName('');
            setNewGroupDesc('');
            setNewGroupAvatar(null);
            setNewGroupAvatarPreview(null);

            api.get('/chats').then(response => {
                setChats(response.data.chats);
                const createdChat = response.data.chats.find(c => c.id === res.data.chat_id);
                if (createdChat) setActiveChat(createdChat);
            });
        }).catch(err => {
            // ✨ ВОТ ЭТИ СТРОКИ ПОКАЖУТ ПРАВДУ В КОНСОЛИ БРАУЗЕРА:
            console.error("🔥 ОШИБКА 400 ОТ СЕРВЕРА:", err.response?.data);
            
            // Если сервер прислал массив ошибок валидации, покажем первую
            if (err.response?.data?.errors) {
                const firstError = Object.values(err.response.data.errors)[0];
                alert(`Ошибка: ${firstError}`);
            } else {
                alert(err.response?.data?.message || "Ошибка при создании группы");
            }
        });
    };
    const messageGroups = groupMessages(messages);

    const handleToggleFavoriteGif = () => {
        const url = contextMenu.msg.gif_url;
        api.post('/messages/toggle-favorite-gif', { gif_url: url }).then(res => {
            setFavoriteGifs(res.data.favorite_gifs);
            handleCloseMenu();
        });
    };

    const handleDownloadImage = async () => {
        const url = contextMenu.msg.image || contextMenu.msg.gif_url;
        if (!url) return;
        try {
            const response = await fetch(url);
            const blob = await response.blob();
            const blobUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = blobUrl;
            const ext = blob.type.split('/')[1] || 'png';
            link.download = `image_${Date.now()}.${ext}`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(blobUrl);
        } catch (err) {
            window.open(url, '_blank');
        }
        handleCloseMenu();
    };

    const handleCopyImage = () => {
        const url = contextMenu.msg.image || contextMenu.msg.gif_url;
        if (!url) return;
        const img = new Image();
        img.crossOrigin = "Anonymous";
        img.src = url;
        img.onload = () => {
            const canvas = document.createElement('canvas');
            canvas.width = img.width;
            canvas.height = img.height;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0);
            canvas.toBlob(async (blob) => {
                try {
                    await navigator.clipboard.write([new ClipboardItem({ 'image/png': blob })]);
                } catch (e) {
                    console.error('Ошибка буфера:', e);
                }
            }, 'image/png');
        };
        handleCloseMenu();
    };

    const processImage = (file) => {
        if (!file || !file.type.startsWith('image/')) return;
        
        // Сохраняем ОРИГИНАЛ для отправки на сервер
        setAttachment(file); 
        
        // Создаем моментальное легкое превью для интерфейса
        setAttachmentPreview(URL.createObjectURL(file)); 
    };
const handleFileSelect = (e, forceType = null) => {
    const file = e.target.files ? e.target.files[0] : e.dataTransfer.files[0];
    if (!file) return;

    if (file.type.startsWith('image/')) {
        setAttachment(file);
        setAttachmentPreview(URL.createObjectURL(file));
        setAttachmentType('image');
    } else if (file.type.startsWith('audio/')) {
        setAttachment(file);
        setAttachmentPreview(URL.createObjectURL(file));
        setAttachmentType('audio');
    } else if (file.type.startsWith('video/')) { // ✨ ДОБАВИЛИ ЭТОТ БЛОК
        setAttachment(file);
        setAttachmentPreview(URL.createObjectURL(file)); // Создаем локальную ссылку для превью
        setAttachmentType('video');
    } else {
        setAttachment(file);
        setAttachmentPreview(null);
        setAttachmentType('file');
    }
    setAttachAnchor(null);
};

const handlePaste = (e) => {
    if (e.clipboardData.files.length > 0) {
        e.preventDefault();
        // Создаем фейковый event, чтобы переиспользовать функцию
        handleFileSelect({ target: { files: e.clipboardData.files } }); 
    }
};
const handleDrop = (e) => {
    e.preventDefault();
    if (e.dataTransfer.files.length > 0) handleFileSelect(e);
};
    const handleDragOver = (e) => e.preventDefault();

    const clearAttachment = () => {
        setAttachment(null);
        setAttachmentPreview(null);
        setAttachmentType(null); // ✨ Добавили
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const loadMessagesDown = () => {
        if (!activeChat || loadingMore || !hasMoreDown) return;
        const lastId = messages[messages.length - 1]?.id;
        setLoadingMore(true);
        api.get(`/messages/${activeChat.id}/more-down?after_id=${lastId}`).then(res => {
            setMessages(prev => {
                const uniqueNew = res.data.messages.filter(nm => !prev.some(p => p.id === nm.id));
                return [...prev, ...uniqueNew];
            });
            setHasMoreDown(res.data.has_more_down);
            setLoadingMore(false);
            if (!res.data.has_more_down) setIsHistoryMode(false);
        });
    };
const loadMessages = (isFirstLoad = false) => {
    if (!activeChat || (loadingMore && !isFirstLoad)) return;
    const currentOffset = isFirstLoad ? 0 : messages.length;
    if (!isFirstLoad) setLoadingMore(true);

    api.get(`/messages/${activeChat.id}?offset=${currentOffset}`).then(res => {
        const incomingMessages = res.data.messages;

        if (isFirstLoad) {
            setMessages(incomingMessages);
            setHasMore(res.data.has_more);
            
            // Ждем отрисовки DOM
            setTimeout(() => {
                // Ищем первое сообщение, которое МЫ не читали
                const firstUnread = incomingMessages.find(m => !m.is_read && m.senderId !== auth.user.id);
                
                if (firstUnread) {
                    const el = document.getElementById(`msg-${firstUnread.id}`);
                    if (el) {
                        el.scrollIntoView({ block: 'center', behavior: 'instant' });
                        // Если мы не в самом низу, ставим флаг режима истории
                        const container = scrollRef.current;
                        if (container && (container.scrollHeight - container.scrollTop - container.clientHeight > 200)) {
                            setIsHistoryMode(true);
                        }
                        return;
                    }
                }
                // Если непрочитанных нет — просто вниз
                scrollToBottom(true);
            }, 100);

        } else {
            // Подгрузка старых сообщений (вверх)
            lastScrollHeight.current = scrollRef.current.scrollHeight;
            setMessages(prev => {
                const uniqueNew = incomingMessages.filter(nm => !prev.some(p => p.id === nm.id));
                return [...uniqueNew, ...prev];
            });
            setHasMore(res.data.has_more);
        }
        setLoadingMore(false);
    });
};
    useEffect(() => {
        if (activeChat) api.get(`/messages/${activeChat.id}/pins`).then(res => setPinnedMessages(res.data));
    }, [activeChat]);

    useEffect(() => {
        setMessages([]);
        setHasMore(true);
        setHasMoreDown(false);
        setIsHistoryMode(false);
        setReplyTo(null);
        setEditingMessage(null);
        setUnreadMessages([]);
        setMentionIds([]);
        clearAttachment();
        loadMessages(true);
    }, [activeChat?.id]);

    const handleCloseEverything = () => {
        setContextMenu(null);
        setReactionPickerAnchor(null);
        setReactingMessageId(null);
    };

    const handleScroll = (e) => {
    const container = e.target;
        const scrollFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
        
        setShowScrollDown(scrollFromBottom > 300);

        // Если пользователь "приземлился" в самый низ
        if (scrollFromBottom < 20) {
            if (isHistoryMode) setIsHistoryMode(false);
            if (unreadMessages.length > 0) setUnreadMessages([]);
            
            // ✨ Вот теперь помечаем прочитанным
            const lastMsg = messages[messages.length - 1];
            if (lastMsg && lastMsg.senderId !== auth.user.id && !lastMsg.is_read) {
                markAsRead(activeChat.id);
            }
        }
        const clearVisible = (list, setList) => {
            if (list.length === 0) return;
            setList(prev => prev.filter(id => {
                const el = document.getElementById(`msg-${id}`);
                if (!el) return true;
                const rect = el.getBoundingClientRect();
                const containerRect = container.getBoundingClientRect();
                return !(rect.top >= containerRect.top && rect.bottom <= containerRect.bottom);
            }));
        };
        clearVisible(mentionIds, setMentionIds);
        clearVisible(unreadMessages, setUnreadMessages);

        if (pinnedMessages.length > 0 && messages.length > 0) {
            let closestIndex = currentPinIndex;
            let minDistance = Infinity;
            let isAnyPinInDom = false;

            pinnedMessages.forEach((pin, index) => {
                const el = document.getElementById(`msg-${pin.id}`);
                if (el) {
                    isAnyPinInDom = true;
                    const rect = el.getBoundingClientRect();
                    const containerRect = container.getBoundingClientRect();
                    const distance = Math.abs(rect.top - containerRect.top);
                    if (distance < minDistance) { minDistance = distance; closestIndex = index; }
                }
            });

            if (!isAnyPinInDom) {
                const centerMsg = messages[Math.floor(messages.length / 2)];
                if (centerMsg) {
                    let minIdDiff = Infinity;
                    pinnedMessages.forEach((pin, index) => {
                        const diff = Math.abs(pin.id - centerMsg.id);
                        if (diff < minIdDiff) { minIdDiff = diff; closestIndex = index; }
                    });
                }
            }
            if (closestIndex !== currentPinIndex) setCurrentPinIndex(closestIndex);
        }
if (scrollFromBottom < 50) {
        setIsHistoryMode(false);
        setUnreadMessages([]);
        
        // ✨ ДОБАВЛЯЕМ ЭТО:
        // Если в чате были непрочитанные, помечаем их при достижении низа
        if (activeChat?.id) {
            markAsRead(activeChat.id);
        }
    }
        if (container.scrollTop === 0 && hasMore && !loadingMore) loadMessages();
        if (scrollFromBottom < 100 && hasMoreDown && !loadingMore) loadMessagesDown();
        if (scrollFromBottom < 50) { setIsHistoryMode(false); setUnreadMessages([]); }
    };

const scrollToBottom = (force = false) => {
    const container = scrollRef.current;
    if (!container) return;

    // Считаем расстояние от текущей позиции до самого низа
    const scrollFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
    
    // Считаем, что мы "внизу", если до конца осталось меньше 300 пикселей
    const isAtBottom = scrollFromBottom < 300;

    // Скроллим если: 
    // 1. Мы принудительно это вызвали (например, при отправке своего сообщения)
    // 2. Пользователь и так находится внизу чата
    if (force || isAtBottom) {
        setTimeout(() => {
            container.scrollTo({ 
                top: container.scrollHeight, 
                behavior: force ? 'smooth' : 'auto' 
            });
        }, 100);
    }
};
    const handleTogglePin = () => {
        const msg = contextMenu?.msg;
        if (!msg) return;
        api.post(`/messages/${msg.id}/pin`).then(res => {
            const pinnedAt = res.data.pinned_at;
            if (pinnedAt) { setPinnedMessages(prev => [{ id: msg.id, text: msg.text, pinned_at: pinnedAt }, ...prev]); }
            else { setPinnedMessages(prev => prev.filter(p => p.id !== msg.id)); setCurrentPinIndex(0); }
            setMessages(prev => prev.map(m => m.id === msg.id ? { ...m, is_pinned: !!pinnedAt } : m));
            handleCloseMenu();
        });
    };

    const handleJumpToPin = (pin) => {
        const element = document.getElementById(`msg-${pin.id}`);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'center' });
            element.style.backgroundColor = 'rgba(56, 189, 248, 0.3)';
            setTimeout(() => element.style.transition = 'background-color 0.5s', 100);
            setTimeout(() => element.style.backgroundColor = '', 1500);
        } else {
            setLoadingMore(true);
            api.get(`/messages/${activeChat.id}/context/${pin.id}`).then(res => {
                setMessages(res.data.messages);
                setHasMore(res.data.has_more_up);
                setHasMoreDown(res.data.has_more_down);
                setIsHistoryMode(true);
                setLoadingMore(false);
                setTimeout(() => {
                    const newEl = document.getElementById(`msg-${pin.id}`);
                    newEl?.scrollIntoView({ block: 'center' });
                }, 100);
            });
        }
    };

    const handleJumpToMention = () => { if (mentionIds.length > 0) handleJumpToPin({ id: mentionIds[0] }); };

    const handleContextMenu = (event, msg) => {
        event.preventDefault();
        setContextMenu(null);
        setContextMenu({
            mouseX: event.clientX,
            mouseY: event.clientY,
            msg
        });
    };

    const handleCloseMenu = () => setContextMenu(null);
    const handleCopyText = () => { navigator.clipboard.writeText(contextMenu.msg.text); handleCloseMenu(); };
    const handleReplyClick = () => { setReplyTo(contextMenu.msg); handleCloseMenu(); };
    const handleEditClick = () => { setEditingMessage(contextMenu.msg); setNewMessage(contextMenu.msg.text); setReplyTo(null); handleCloseMenu(); };

    const handleDeleteClick = () => {
        setMessageToDelete(contextMenu.msg);
        setDeleteForEveryone(false);
        setDeleteDialogOpen(true);
        handleCloseMenu();
    };

    const confirmDelete = () => {
        if (!messageToDelete) return;
        const msgId = messageToDelete.id;

        api.delete(`/messages/${msgId}?forEveryone=${deleteForEveryone}`)
            .then(() => {
                setMessages(prev => prev.filter(m => m.id !== msgId));
                setDeleteDialogOpen(false);
                setMessageToDelete(null);
            })
            .catch(err => {
                console.error("Ошибка удаления:", err);
                setDeleteDialogOpen(false);
            });
    };

    const handleReact = (messageId, emoji) => {
        api.post(`/messages/${messageId}/react`, { emoji }).then(res => {
            setMessages(prev => prev.map(m =>
                m.id === messageId ? { ...m, reactions: res.data } : m
            ));
            handleCloseMenu();
        });
    };

    const handleDeleteChat = () => {
        if (!activeChat) return;
        if (window.confirm("Вы уверены, что хотите удалить всю историю переписки? Контакт останется, но сообщения исчезнут навсегда.")) {
            api.delete(`/conversations/${activeChat.id}`).then(() => {
                setMessages([]); setPinnedMessages([]);
                setChats(prev => prev.map(c => c.id === activeChat.id ? { ...c, lastMessage: 'Нет сообщений', time: '' } : c));
                setHeaderMenuAnchor(null);
            });
        }
    };
const formatBytes = (bytes) => {
    if (bytes === 0) return '0 MB';
    const k = 1048576; // 1024 * 1024 (Сразу в Мегабайты)
    return (bytes / k).toFixed(1) + ' MB';
};
const handleCancelUpload = (msgId) => {
    const msg = messages.find(m => m.id === msgId);
    if (msg && msg.abortController) {
        msg.abortController.abort(); // Останавливает Axios запрос
    }
    // Удаляем из UI
    setMessages(prev => prev.filter(m => m.id !== msgId));
};

// ✨ Делаем функцию async
const handleSendMessage = async (e, gifUrl = null, directFile = null) => {
    if (e) e.preventDefault();

    const finalFile = directFile || attachment;
    const finalType = directFile ? 'audio' : attachmentType; 

    if (!newMessage.trim() && !finalFile && !gifUrl && !activeChat) return;
    
    const formatBytes = (bytes) => {
        if (bytes === 0) return '0 MB';
        const k = 1048576; 
        return (bytes / k).toFixed(1) + ' MB';
    };

    if (editingMessage && !gifUrl) {
        api.patch(`/messages/${editingMessage.id}`, { text: newMessage }).then(res => {
            setMessages(prev => prev.map(m => m.id === res.data.id ? { ...m, text: res.data.text, is_edited: true } : m));
            setEditingMessage(null); 
            setNewMessage('');
        });
        return;
    } 

    const textToRestore = newMessage;
    const replyToRestore = replyTo;
    const attachmentToRestore = finalFile;
    const attachmentPreviewToRestore = attachmentPreview;
    const attachmentTypeToRestore = finalType;

    const abortController = new AbortController();
    const tempId = Date.now();
    
    const optimisticMsg = {
        id: tempId,
        text: newMessage || '',
        gif_url: gifUrl,
        image: finalType === 'image' ? (directFile ? URL.createObjectURL(directFile) : attachmentPreview) : null,
        audio_stream: finalType === 'audio' ? (directFile ? URL.createObjectURL(directFile) : attachmentPreview) : null,
        audio_title: finalType === 'audio' ? (finalFile?.name || "Голосовое сообщение") : null,
        document_name: finalType === 'file' ? finalFile?.name : null,
        document_size: finalType === 'file' ? formatBytes(finalFile?.size) : null,
        senderId: auth.user.id,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        pending: true,
        is_read: false,
        attachmentType: finalType,
        uploadProgress: 0,
        loadedStr: '0 MB',
        totalStr: formatBytes(finalFile?.size || 0),
        abortController: abortController 
    };

    setMessages(prev => [...prev, optimisticMsg]);
    setTimeout(() => scrollToBottom(true), 50);

    const formData = new FormData();
    formData.append('text', newMessage || '');
    if (replyTo) formData.append('parent_id', replyTo.id);
    if (gifUrl) formData.append('gif_url', gifUrl);

    setNewMessage(''); 
    setReplyTo(null); 
    clearAttachment();

    try {
        // ✨ === ЛОГИКА ЗАГРУЗКИ ЧАНКАМИ === ✨
        if (finalFile) {
            const CHUNK_SIZE = 5 * 1024 * 1024; // 5 МБ
            const totalChunks = Math.ceil(finalFile.size / CHUNK_SIZE);
            const uploadSessionId = tempId.toString(); // Уникальный ID сборки
            let uploadedBytes = 0;

            for (let i = 0; i < totalChunks; i++) {
                // Если юзер нажал крестик во время цикла — прерываем
                if (abortController.signal.aborted) throw new Error('canceled');

                const start = i * CHUNK_SIZE;
                const end = Math.min(start + CHUNK_SIZE, finalFile.size);
                const chunk = finalFile.slice(start, end);

                const chunkData = new FormData();
                chunkData.append('chunk', chunk);
                chunkData.append('chunkIndex', i);
                chunkData.append('uploadId', uploadSessionId);

                // Отправляем кусок на новый эндпоинт
                await api.post(`/messages/${activeChat.id}/upload-chunk`, chunkData, { signal: abortController.signal });

                uploadedBytes += chunk.size;
                const percentCompleted = Math.round((uploadedBytes * 100) / finalFile.size);

                // UI обновляется без лагов (всего 1 раз на каждые 5 МБ, а не 100 раз в секунду)
                setMessages(prev => prev.map(m => 
                    m.id === tempId ? { ...m, uploadProgress: percentCompleted, loadedStr: formatBytes(uploadedBytes) } : m
                ));
            }

            // Говорим главному эндпоинту, где лежит собранный файл
            formData.append('uploaded_file_id', uploadSessionId);
            formData.append('original_file_name', finalFile.name || "Голосовое.webm");
            formData.append('content_type', finalFile.type);
        }

        // ✨ ФИНАЛЬНАЯ ОТПРАВКА (Текст + Данные о собранном файле)
        const res = await api.post(`/messages/${activeChat.id}`, formData, { signal: abortController.signal });

        setMessages(prev => {
            const alreadyExists = prev.some(m => m.id === res.data.id);
            if (alreadyExists) return prev.filter(m => m.id !== tempId);
            return prev.map(m => m.id === tempId ? res.data : m);
        });

    } catch (err) {
        if (err.name === 'CanceledError' || err.message === 'canceled') {
            setMessages(prev => prev.filter(m => m.id !== tempId));
            return;
        }

        // Возвращаем текст и файлы в инпут при ошибке
        setNewMessage(textToRestore);
        setReplyTo(replyToRestore);
        if (attachmentToRestore) {
            setAttachment(attachmentToRestore);
            setAttachmentPreview(attachmentPreviewToRestore);
            setAttachmentType(attachmentTypeToRestore);
        }

        // 👇 ДОБАВЛЕН ALERT 👇
        const errorText = err.response?.data?.message || err.response?.data || err.message;
        console.error("🔥 Ошибка при отправке:", errorText);

        if (err.response?.status === 403) {
            setMessages(prev => prev.map(m => m.id === tempId ? { ...m, text: "Ошибка: у вас нет прав", is_error: true, pending: false } : m));
        } else if (err.response?.status === 429) {
            const match = errorText.toString().match(/\d+/);
            setCooldown(match ? parseInt(match[0], 10) : 5);
            setMessages(prev => prev.filter(m => m.id !== tempId));
        } else {
            // ПОКАЗЫВАЕМ ОШИБКУ ПОЛЬЗОВАТЕЛЮ!
            alert(`Ошибка сервера: ${errorText}`);
            setMessages(prev => prev.filter(m => m.id !== tempId));
        }
    }
};
    const handleGifClick = (gif, e) => {
        e.preventDefault();
        setGifAnchor(null);
        handleSendMessage(null, gif.images.fixed_height.url);
    };

    useEffect(() => {
        if (lastScrollHeight.current > 0 && !loadingMore) {
            const newHeight = scrollRef.current.scrollHeight;
            scrollRef.current.scrollTop = newHeight - lastScrollHeight.current;
            lastScrollHeight.current = 0;
        }
    }, [messages, loadingMore]);

const markAsRead = (chatId) => {
    if (!chatId || messages.length === 0) return;

    // Берем ID самого последнего сообщения в массиве (оно в самом низу)
    const lastMsgId = messages[messages.length - 1].id;
    
    // Если это наше сообщение, нам нечего читать
    if (messages[messages.length - 1].senderId === auth.user.id && !messages[messages.length - 1].is_read) {
        // Пропускаем, если последнее сообщение отправили мы
    }

    // Обновляем счетчик в списке чатов локально
    setChats(prev => prev.map(c =>
        c.id === chatId ? { ...c, unread_count: 0, pings_count: 0 } : c
    ));

    // Отправляем запрос с ID последнего сообщения
    api.post(`/conversations/${chatId}/read/${lastMsgId}`).catch(err => console.error("Ошибка чтения:", err));
};


    useEffect(() => {
        if (activeChat?.id && messages.length > 0) {
            const lastMsg = messages[messages.length - 1];
            if (lastMsg.senderId !== auth.user.id) {
                markAsRead(activeChat.id);
            }
        }
    }, [messages.length]);
    //<Header color={value} auth={auth} />
    return (
        <ThemeProvider theme={theme}>
            <Box sx={{ height: '100vh', bgcolor: `hsl(${214 + value}, 67%, 5%)`, display: 'flex', flexDirection: 'column', overflow: 'hidden' }} onDragOver={handleDragOver} onDrop={handleDrop}>



                <Container maxWidth={false} sx={{ mt: 2, mb: 2, flex: 1, display: 'flex', overflow: 'hidden' }}>
                    <Paper sx={{ flex: 1, display: 'flex', borderRadius: 4, overflow: 'hidden', bgcolor: '#1e293b', border: '1px solid rgba(255,255,255,0.05)', height: 'calc(100vh - 40px)' }}>

                        {/* ЛЕВАЯ ПАНЕЛЬ ЧАТОВ */}
                        <Box sx={{ width: { xs: '80px', md: `${leftPanelWidth}px` }, borderRight: '1px solid rgba(255,255,255,0.05)', display: 'flex', flexDirection: 'column', bgcolor: '#0f172a',transition: isResizing.current ? 'none' : 'width 0.1s' }}>
                           {!isCollapsed && <Box sx={{ p: 2, display: { xs: 'none', md: 'block' } }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', bgcolor: '#1e293b', borderRadius: 5, px: 2, py: 1 }}>
                                    <Search sx={{ color: '#94a3b8', mr: 1, fontSize: 20 }} />
                                    <InputBase placeholder="Поиск..." sx={{ color: 'white', flex: 1, fontSize: '0.9rem' }} />
                                </Box>
                                <Button
                                    fullWidth
                                    startIcon={<AddCircle />}
                                    onClick={() => setIsCreateGroupOpen(true)}
                                    sx={{ mt: 1, bgcolor: '#38bdf8', color: '#0f172a', fontWeight: 'bold', textTransform: 'none', '&:hover': { bgcolor: '#0ea5e9' } }}
                                >
                                    Создать группу
                                </Button>
                            </Box>}
                            <List sx={{ flex: 1, overflowY: 'auto' }}>
                                {chats?.map((chat) => (
                                    <ListItemButton key={chat.id} selected={activeChat?.id === chat.id} onClick={() => handleChatClick(chat)} sx={{ px: 2, py: 1.5 }}>
                                        <ListItemAvatar>
                                            <Avatar src={chat.avatar}>{chat.name?.[0]}</Avatar>
                                        </ListItemAvatar>
                                        {!isCollapsed && (
                                         <>
                                        <ListItemText
                                            sx={{ display: { xs: 'none', md: 'block' } }}
                                            primary={<Typography noWrap sx={{ color: 'white', fontWeight: 600 }}>{chat.name}</Typography>}
                                            secondary={<Typography noWrap sx={{ color: '#64748b', fontSize: '0.8rem' }}>{chat.lastMessage}</Typography>}
                                        />
                                        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 0.5 }}>
                                            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 0.5 }}>
                                                <Typography sx={{ color: '#64748b', fontSize: '0.75rem' }}>{formatToLocalTime(chat.time)}</Typography>

                                                <Box sx={{ display: 'flex', gap: 0.5 }}>
                                                    {/* КРАСНЫЙ КРУЖОЧЕК: PINGS (@упоминания) */}
                                                    {chat.pings_count > 0 && (
                                                        <Box sx={{
                                                            bgcolor: '#ef4444',
                                                            color: 'white',
                                                            borderRadius: '50%',
                                                            width: 18, height: 18,
                                                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                                                            fontSize: '0.7rem', fontWeight: 'bold'
                                                        }}>
                                                            @
                                                        </Box>
                                                    )}

                                                    {/* ГОЛУБОЙ КРУЖОЧЕК: UNREAD (сообщения) */}
                                                    {chat.unread_count > 0 && (
                                                        <Box sx={{
                                                            bgcolor: '#38bdf8',
                                                            color: '#0f172a',
                                                            borderRadius: '10px',
                                                            px: 0.8, height: 18,
                                                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                                                            fontSize: '0.7rem', fontWeight: 'bold'
                                                        }}>
                                                            {chat.unread_count}
                                                        </Box>
                                                    )}
                                                </Box>
                                            </Box>
                                       
                                        
                                        </Box>
                                         </>
                                            )}
                                    </ListItemButton>
                                ))}
                            </List>
                        </Box>
                        <Box
                            onMouseDown={startResizing}
                            sx={{
                                width: '4px',
                                cursor: 'col-resize',
                                bgcolor: 'transparent',
                                transition: 'background-color 0.2s',
                                '&:hover': {
                                    bgcolor: '#38bdf8', // Подсвечиваем голубым при наведении
                                },
                                display: { xs: 'none', md: 'block' }, // На мобилках не нужно
                                zIndex: 10,
                                marginLeft: '-2px', // Наплываем на границу для удобного хвата
                                marginRight: '-2px',
                            }}
                        />
                        {/* ПРАВАЯ ПАНЕЛЬ (ЧАТ) */}
                        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', bgcolor: '#1e293b', position: 'relative' }}>
                            {activeChat ? (
                                <>
                                    {/* ИНТЕРАКТИВНАЯ ШАПКА ЧАТА */}
                                    <Box sx={{ p: 1.5, px: 3, borderBottom: '1px solid rgba(255,255,255,0.05)', bgcolor: '#0f172a', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                        <Box onClick={() => {
                                            if (activeChat.is_group) {
                                                setIsGroupProfileOpen(true);
                                            } else {
                                                // Найдите этот блок в Messenger.js и замените:
                                                setSelectedUser({
                                                    // Было: id: activeChat?.id (это ID комнаты)
                                                    // Стало: берем Id из объекта пользователя внутри чата
                                                    id: activeChat?.user?.Id || activeChat?.user?.id,
                                                    name: activeChat?.name,
                                                    avatar: activeChat?.avatar,
                                                    username: activeChat?.user?.username,
                                                });
                                            }
                                        }} sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer', '&:hover': { opacity: 0.8 }, transition: 'opacity 0.2s' }}>
                                            {activeChat.is_group && <Avatar src={activeChat.avatar} sx={{ mr: 2, bgcolor: activeChat.is_group ? '#f59e0b' : '#38bdf8', color: '#0f172a' }}>
                                                {activeChat.name?.[0]}
                                            </Avatar>}
                                            <Box>
                                                <Typography sx={{ color: 'white', fontWeight: 'bold', lineHeight: 1.2 }}>{activeChat.name}</Typography>
                                                {activeChat.is_group && <Typography sx={{ color: activeChat.is_group ? '#979797' : '#38bdf8', fontSize: '0.75rem' }}>
                                                    {activeChat.description || 'Нет описания'}
                                                </Typography>}
                                                <Typography sx={{ color: '#38bdf8', fontSize: '0.75rem' }}>
                                                    {getTypingText()}
                                                </Typography>
                                            </Box>
                                        </Box>
                                    </Box>

                                    {/* ПАНЕЛЬ ЗАКРЕПА */}
                                    {pinnedMessages.length > 0 && (
                                        <Box sx={{
                                            bgcolor: '#0f172a', borderBottom: '1px solid rgba(255,255,255,0.05)',
                                            px: 2, py: 1, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer',
                                            '&:hover': { bgcolor: 'rgba(255,255,255,0.02)' }
                                        }} onClick={() => handleJumpToPin(pinnedMessages[currentPinIndex])}>
                                            <PushPin sx={{ color: '#38bdf8', fontSize: 18, transform: 'rotate(45deg)' }} />
                                            <Box sx={{ flex: 1, overflow: 'hidden' }}>
                                                <Typography sx={{ color: '#38bdf8', fontSize: '0.8rem', fontWeight: 'bold' }}>Закрепленное сообщение #{pinnedMessages.length - currentPinIndex}</Typography>
                                                <Typography noWrap sx={{ color: 'white', fontSize: '0.85rem', opacity: 0.8 }}>{pinnedMessages[currentPinIndex]?.text || 'Вложение'}</Typography>
                                            </Box>
                                            {pinnedMessages.length > 1 && (
                                                <IconButton size="small" onClick={(e) => { e.stopPropagation(); setCurrentPinIndex((prev) => (prev + 1) % pinnedMessages.length); }}>
                                                    <CloseIcon fontSize="small" sx={{ transform: 'rotate(45deg)', color: '#94a3b8' }} />
                                                </IconButton>
                                            )}
                                        </Box>
                                    )}

                                    {/* ОБЛАСТЬ СООБЩЕНИЙ */}
                                    <Box ref={scrollRef} onScroll={handleScroll} sx={{ flex: 1, p: 3, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 0, bgcolor: '#0b1120', pb: 4 }}>
                                        {loadingMore && <Box sx={{ textAlign: 'center', py: 1 }}><CircularProgress size={20} color="primary" /></Box>}

                                        {messageGroups.map((group, groupIndex) => {
    const firstMsg = group[0];
    const isMe = firstMsg.senderId === auth.user.id;
    const displayName = firstMsg.senderNickname || firstMsg.senderName;

    return (
        <Box
            key={groupIndex}
            sx={{
                display: 'flex',
                flexDirection: 'row',
                alignItems: 'flex-start',
                gap: 2,
                mt: 2, // Отступ между группами сообщений
                width: '100%',
                '&:hover': { bgcolor: 'rgba(255,255,255,0.02)' }, // Очень легкая подсветка при наведении, как в Discord
                px: 1,
                py: 0.5,
                borderRadius: 1,
            }}
        >
            {/* 1. АВАТАРКА СЛЕВА */}
            <Avatar
                src={firstMsg.senderAvatar}
                sx={{
                    width: 40, height: 40,
                    bgcolor: '#94a3b8',
                    cursor: 'pointer',
                    mt: 0.5, // Слегка опускаем, чтобы выровнять по тексту никнейма
                    transition: 'all 0.2s',
                    '&:hover': { opacity: 0.8, transform: 'scale(1.05)' }
                }}
                onClick={() => setSelectedUser({
                    id: firstMsg.senderId,
                    name: firstMsg.senderName,
                    avatar: firstMsg.senderAvatar,
                    username: firstMsg.senderUsername,
                })}
            >
                {firstMsg.senderName?.[0]}
            </Avatar>

            {/* 2. КОНТЕЙНЕР КОНТЕНТА */}
            <Box sx={{ display: 'flex', 
    flexDirection: 'column', 
    width: 'fit-content', // ✨ Обволакивает самое широкое сообщение
    maxWidth: 'calc(100% - 60px)', // Оставляем место под аватарку
    position: 'relative'}}>
                
                {/* ШАПКА: НИК И ВРЕМЯ */}
                <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1, mb: 0.5 }}>
                    <Typography
                        sx={{
                            color: isMe ? '#38bdf8' : '#9572A0', // Твой цвет и цвет собеседника
                            fontSize: '1rem',
                            fontWeight: 'bold',
                            cursor: 'pointer',
                            '&:hover': { textDecoration: 'underline' }
                        }}
                        onClick={() => setSelectedUser({
                            id: firstMsg.senderId,
                            name: firstMsg.senderName,
                            avatar: firstMsg.senderAvatar,
                            username: firstMsg.senderUsername,
                        })}
                    >
                        {displayName}
                    </Typography>
                    <Typography sx={{ color: '#64748b', fontSize: '0.75rem' }}>
                        {formatToLocalTime(firstMsg.time)}
                    </Typography>
                </Box>

                {/* САМИ СООБЩЕНИЯ (Без пузырей, просто текст вниз) */}
                {group.map((msg, msgIndex) => {
                const isLastInGroup = msgIndex === group.length - 1;
                
                return (
                    
                    <Box
                        key={msg.id}
                        onContextMenu={(e) => handleContextMenu(e, msg)}
                        id={`msg-${msg.id}`}
                        sx={{
                            position: 'relative',
                            color: 'white',
                            width: '100%',
                            mt: 0.3, // Минимальный отступ между строками
                            '&:hover .msg-actions': { opacity: 1 } // Показываем иконки статуса при наведении
                        }}
                    >
                        {/* ПЕРЕСЛАНО */}
                        {msg.forwarded_from && (
                            <Typography sx={{ fontSize: '0.75rem', color: "#4EA4F2", fontWeight: "bold", mb: 0.5 }}>
                                Переслано от {msg.forwarded_from}
                            </Typography>
                        )}

                        {/* ОТВЕТ */}
                        {msg.reply_to && (
                            <Box sx={{ mb: 0.5, pl: 1.5, borderLeft: '3px solid', borderColor: isMe ? '#38bdf8' : '#64748b', opacity: 0.7 }}>
                                <Typography sx={{ fontSize: '0.75rem', fontWeight: 'bold' }}>{msg.reply_to.name}</Typography>
                                <Typography noWrap sx={{ fontSize: '0.75rem' }}>{msg.reply_to.text || 'Вложение'}</Typography>
                            </Box>
                        )}

                        {/* КАРТИНКА / ГИФКА */}
                        {/* КАРТИНКА / ГИФКА */}
{(msg.image || msg.gif_url) && (
    <Box sx={{ mt: 0.5, mb: msg.text ? 1 : 0, borderRadius: 2, overflow: 'hidden', display: 'inline-block', position: 'relative' }}>
        <img
            src={msg.gif_url || msg.image_thumb || msg.image}
            alt="Attachment"
            onLoad={() => scrollToBottom(false)}
            onClick={() => !msg.pending && setFullScreenImage(msg)}
            style={{
                maxWidth: '400px', maxHeight: '400px', width: 'auto', height: 'auto',
                display: 'block', borderRadius: '8px', transition: 'all 0.3s',
                cursor: msg.pending ? 'default' : 'pointer',
                // ✨ МАГИЯ РАЗМЫТИЯ ЗДЕСЬ
                filter: msg.pending ? 'blur(4px) brightness(0.6)' : 'none',
            }}
        />
        
        {/* ✨ UI Загрузки Картинки (Кружок + Отмена + Текст) */}
        {msg.pending && msg.attachmentType === 'image' && (
            <Box sx={{
                position: 'absolute', top: 0, left: 0, width: '100%', height: '100%',
                display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', zIndex: 10
            }}>
                <Box 
                    onClick={() => handleCancelUpload(msg.id)}
                    sx={{
                        position: 'relative', width: 48, height: 48, bgcolor: 'rgba(0,0,0,0.5)', 
                        borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
                        cursor: 'pointer', '&:hover': { bgcolor: 'rgba(0,0,0,0.7)' }
                    }}
                >
                    <CircularProgress variant="determinate" value={msg.uploadProgress || 0} size={48} sx={{ color: 'white', position: 'absolute' }} />
                    <CloseIcon sx={{ color: 'white', fontSize: 24 }} />
                </Box>
                <Box sx={{ mt: 1, bgcolor: 'rgba(0,0,0,0.5)', px: 1, py: 0.2, borderRadius: 2 }}>
                    <Typography sx={{ color: 'white', fontSize: '0.75rem', fontWeight: 'bold' }}>
                        {msg.uploadProgress}% ({msg.loadedStr} / {msg.totalStr})
                    </Typography>
                </Box>
            </Box>
        )}
    </Box>
)}


{/* ✨ ВИДЕО ПЛЕЕР */}
{/* ✨ ВИДЕО ПЛЕЕР */}
{(msg.video_master || (msg.pending && msg.attachmentType === 'video')) && (
    <Box sx={{ mt: 0.5, mb: msg.text ? 1 : 0, borderRadius: 2, overflow: 'hidden', position: 'relative', maxWidth: '400px' }}>
        
        <video
            // 👇 Берем прямую ссылку video_master 👇
            src={msg.pending ? msg.image : msg.video_master}
            controls={!msg.pending}
            style={{ 
                width: '100%', 
                maxHeight: '400px', 
                borderRadius: '8px', 
                filter: msg.pending ? 'blur(4px) brightness(0.6)' : 'none',
                backgroundColor: '#000'
            }}
        />

        {/* UI Загрузки */}
        {msg.pending && msg.attachmentType === 'video' && (
            <Box sx={{
                position: 'absolute', top: 0, left: 0, width: '100%', height: '100%',
                display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', zIndex: 10
            }}>
                <Box 
                    onClick={() => handleCancelUpload(msg.id)}
                    sx={{
                        position: 'relative', width: 48, height: 48, bgcolor: 'rgba(0,0,0,0.5)', 
                        borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
                        cursor: 'pointer', '&:hover': { bgcolor: 'rgba(0,0,0,0.7)' }
                    }}
                >
                    <CircularProgress variant="determinate" value={msg.uploadProgress || 0} size={48} sx={{ color: '#38bdf8', position: 'absolute' }} />
                    <CloseIcon sx={{ color: 'white', fontSize: 24 }} />
                </Box>
                <Box sx={{ mt: 1, bgcolor: 'rgba(0,0,0,0.5)', px: 1, py: 0.2, borderRadius: 2 }}>
                    <Typography sx={{ color: 'white', fontSize: '0.75rem', fontWeight: 'bold' }}>
                        {msg.uploadProgress}% ({msg.loadedStr} / {msg.totalStr})
                    </Typography>
                </Box>
            </Box>
        )}
    </Box>
)}
                {/* АУДИО СООБЩЕНИЕ */}
{/* АУДИО СООБЩЕНИЕ */}
                {msg.audio_stream && (
    <Box sx={{ /* твои стили */ }}>
        <Button 
            variant="contained" 
            startIcon={<PlayArrow />}
            onClick={(e) => {
                e.stopPropagation();
                const audioMessages = messages.filter(m => m.audio_stream);
                playTrack(msg, audioMessages);
            }}
            sx={{ bgcolor: '#38bdf8', color: '#0f172a', fontWeight: 'bold' }}
        >
            Слушать в AIMP-мини
        </Button>
    </Box>
)}
{msg.document_url && (
    <Box sx={{ 
        mt: 0.5, mb: msg.text ? 1 : 0, p: 1.5, 
        bgcolor: 'rgba(56, 189, 248, 0.05)', 
        border: '1px solid rgba(56, 189, 248, 0.2)',
        borderRadius: 2, 
        display: 'flex', alignItems: 'center', gap: 2,
        minWidth: '250px', maxWidth: '400px',
        transition: 'all 0.2s', '&:hover': { bgcolor: 'rgba(56, 189, 248, 0.1)' }
    }}>
        <Box sx={{ 
            width: 48, height: 48, borderRadius: '50%', 
            bgcolor: '#38bdf8', color: '#0f172a', 
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 
        }}>
            <InsertDriveFile sx={{ fontSize: 26 }} />
        </Box>
        <Box sx={{ flex: 1, overflow: 'hidden' }}>
            <Typography noWrap sx={{ color: 'white', fontWeight: 'bold', fontSize: '0.9rem' }}>
                {msg.document_name}
            </Typography>
            <Typography sx={{ color: '#94a3b8', fontSize: '0.8rem' }}>
                {msg.pending ? `${msg.loadedStr} / ${msg.totalStr}` : msg.document_size}
            </Typography>
            {/* Прогресс бар загрузки файла */}
            {msg.pending && (
                <Box sx={{ width: '100%', height: 4, bgcolor: 'rgba(255,255,255,0.1)', borderRadius: 2, mt: 0.5, overflow: 'hidden' }}>
                    <Box sx={{ width: `${msg.uploadProgress}%`, height: '100%', bgcolor: '#38bdf8', transition: 'width 0.2s' }} />
                </Box>
            )}
        </Box>
        
        {/* Если файл грузится - крестик отмены. Если загружен - кнопка скачивания */}
        {msg.pending ? (
            <IconButton onClick={() => handleCancelUpload(msg.id)} sx={{ color: '#ef4444' }}>
                <CloseIcon />
            </IconButton>
        ) : (
            <IconButton href={msg.document_url} download target="_blank" sx={{ color: '#38bdf8', bgcolor: 'rgba(56, 189, 248, 0.1)' }}>
                <Download />
            </IconButton>
        )}
    </Box>
)}

{/* ОПРОСЫ (POLLS) */}
{/* ОПРОСЫ (POLLS) */}
{msg.poll && (() => {
    // ✨ ФИКС РЕГИСТРА: Берем с большой ИЛИ с маленькой буквы
    const pollOptions = msg.poll.Options || msg.poll.options || [];
    const pollQuestion = msg.poll.Question || msg.poll.question || "Опрос";
    const isMultiChoice = msg.poll.IsMultipleChoice ?? msg.poll.isMultipleChoice;

    // Вспомогательная функция для сборки ссылки на CDN
    const getPollImageUrl = (path) => {
        if (!path) return null;
        if (path.startsWith('http')) return path;
        return `https://cdn.sonzaiigi.com/${path.replace(/^\//, '')}`;
    };

    const totalVotes = pollOptions.reduce((sum, o) => sum + (o.Voters || o.voters || []).length, 0);

    return (
        <Box sx={{ mt: 0.5, mb: msg.text ? 1 : 0, p: 2, bgcolor: '#1e293b', borderRadius: 2, border: '1px solid rgba(255,255,255,0.05)', minWidth: '400px', maxWidth: '1000px' }}>
            <Typography sx={{ color: 'white', fontWeight: 'bold', fontSize: '1rem', mb: 0.5 }}>
                📊 {pollQuestion}
            </Typography>
            <Typography sx={{ color: '#94a3b8', fontSize: '0.75rem', mb: 2 }}>
                {isMultiChoice ? 'Множественный выбор' : 'Одиночный выбор'}
            </Typography>

            {pollOptions.map((opt) => {
                const optId = opt.Id || opt.id;
                const optText = opt.Text || opt.text;
                const optVoters = opt.Voters || opt.voters || [];
                const optImage = opt.ImageUrl || opt.imageUrl;
                
                const percent = totalVotes === 0 ? 0 : Math.round((optVoters.length / totalVotes) * 100);
                const isMyVote = optVoters.includes(auth.user.id);

                return (
                    <Box 
                        key={optId} 
                        onClick={() => {
                            api.post(`/messages/${msg.id}/vote`, { OptionId: optId }).then(res => {
                                setMessages(prev => prev.map(m => m.id === msg.id ? { ...m, poll: res.data } : m));
                            });
                        }}
                        sx={{ position: 'relative', p: 1.5, mb: 1, borderRadius: 2, cursor: 'pointer', overflow: 'hidden', border: isMyVote ? '1px solid #38bdf8' : '1px solid rgba(255,255,255,0.1)', transition: 'all 0.2s', '&:hover': { bgcolor: 'rgba(255,255,255,0.02)' } }}
                    >
                        {totalVotes > 0 && (
                            <Box sx={{ position: 'absolute', top: 0, left: 0, height: '100%', width: `${percent}%`, bgcolor: isMyVote ? 'rgba(56, 189, 248, 0.2)' : 'rgba(255,255,255,0.05)', zIndex: 0, transition: 'width 0.4s ease-out' }} />
                        )}
                        
                        <Box sx={{ position: 'relative', zIndex: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                {/* ✨ РЕНДЕР КАРТИНКИ ИЗ S3 */}
{/* ✨ РЕНДЕР КАРТИНКИ ИЗ S3 (С возможностью увеличения) */}
{optImage && (() => {
    // Берем большую картинку, а если это старый опрос без нее — фоллбек на маленькую
    const optImageView = opt.ImageViewUrl || opt.imageViewUrl || optImage; 

    return (
        <Box 
            onClick={(e) => {
                e.stopPropagation(); // ✨ КРИТИЧНО ВАЖНО: блокируем клик, чтобы не засчитался голос!
                // Имитируем структуру обычного сообщения, чтобы модалка сработала
                setFullScreenImage({ image_view: getPollImageUrl(optImageView) }); 
            }}
            sx={{ 
                width: 150, height: 150, flexShrink: 0, 
                position: 'relative', cursor: 'zoom-in',
                '&::after': { // Делаем легкое затемнение при наведении, чтобы было понятно, что кликабельно
                    content: '""', position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', 
                    bgcolor: 'rgba(0,0,0,0)', transition: 'background-color 0.2s', borderRadius: '4px'
                },
                '&:hover::after': { bgcolor: 'rgba(0,0,0,0.2)' }
            }}
        >
            <img 
                src={getPollImageUrl(optImage)} 
                alt="opt" 
                style={{ width: '100%', height: '100%', borderRadius: 4, objectFit: 'cover' }} 
            />
        </Box>
    );
})()}
                                <Typography sx={{ color: 'white', fontSize: '0.9rem', fontWeight: isMyVote ? 'bold' : 'normal' }}>
                                    {optText}
                                </Typography>
                            </Box>
                            {totalVotes > 0 && (
                                <Typography sx={{ color: isMyVote ? '#38bdf8' : '#94a3b8', fontSize: '0.85rem', fontWeight: 'bold' }}>
                                    {percent}%
                                </Typography>
                            )}
                        </Box>
                    </Box>
                );
            })}
            <Typography sx={{ color: '#64748b', fontSize: '0.75rem', mt: 1, textAlign: 'right' }}>
                Всего голосов: {totalVotes}
            </Typography>
        </Box>
    );
})()}

                        {/* ТЕКСТ СООБЩЕНИЯ И СТАТУС ПРОЧТЕНИЯ */}
                        {/* ТЕКСТ СООБЩЕНИЯ И СТАТУС ПРОЧТЕНИЯ */}
                        <Box sx={{ display: 'flex', alignItems: 'flex-end', gap: 1 }}>
                            
                            {msg.text && (
                                <Typography sx={{
                                    fontSize: '0.95rem',
                                    wordBreak: 'break-word',
                                    color: msg.is_error ? '#ef4444' : '#e2e8f0',
                                    fontWeight: msg.is_error ? 'bold' : 'normal',
                                    lineHeight: 1.4
                                }}>
                                    {msg.is_error
                                        ? msg.text
                                        : msg.text.split(' ').map((word, idx) => word.startsWith('@') ? <span key={idx} style={{ color: '#38bdf8', fontWeight: 'bold' }}>{word} </span> : word + ' ')}
                                </Typography>
                            )}
                            
                            {/* ✨ СТАТУС: Показываем только если это ТВОЕ сообщение И оно ПОСЛЕДНЕЕ в группе */}
                            {isMe && isLastInGroup && (
                                        <Box 
                                            className="msg-actions" 
                                            sx={{ 
                                                display: 'flex', 
                                                opacity: msg.pending ? 1 : 0.5, 
                                                transition: 'opacity 0.2s', 
                                                mb: 0.3,
                                                ml: 'auto', // ✨ Прижимает к правому краю fit-content блока
                                                pl: 1 // Небольшой отступ от текста
                                            }}
                                        >
                                            {!msg.pending && (msg.is_read ? <DoneAll sx={{ fontSize: 16, color: '#38bdf8' }} /> : <Done sx={{ fontSize: 16, color: '#94a3b8' }} />)}
                                            {msg.pending && <AccessTime sx={{ fontSize: 14, color: '#94a3b8' }} />}
                                        </Box>
                                    )}

                        </Box>
                        {/* РЕАКЦИИ */}
                        {msg.reactions && msg.reactions.length > 0 && (
                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: '6px', mt: 1 }}>
                                {msg.reactions.map((r) => {
                                    const bgColors = { active: '#3F96D0', inactive: 'rgba(255,255,255,0.05)' };
                                    const textColors = { active: '#0f172a', inactive: '#cbd5e1' };
                                    const borderColors = { active: '#3F96D0', inactive: 'rgba(255,255,255,0.1)' };

                                    return (
                                        <Box
                                            key={r.emoji}
                                            onClick={(e) => { e.stopPropagation(); handleReact(msg.id, r.emoji); }}
                                            sx={{
                                                display: 'flex', alignItems: 'center', gap: 0.5, px: 1, py: 0.3,
                                                borderRadius: '8px', bgcolor: r.reacted_by_me ? bgColors.active : bgColors.inactive,
                                                border: '1px solid', borderColor: r.reacted_by_me ? borderColors.active : borderColors.inactive,
                                                cursor: 'pointer', transition: 'all 0.2s',
                                                '&:hover': { transform: 'scale(1.05)' }
                                            }}
                                        >
                                            <Typography sx={{ fontSize: '1rem', lineHeight: 1 }}>{r.emoji}</Typography>
                                            <Typography sx={{ fontSize: '0.8rem', fontWeight: 'bold', color: r.reacted_by_me ? textColors.active : textColors.inactive }}>
                                                {r.count}
                                            </Typography>
                                        </Box>
                                    );
                                })}
                            </Box>
                        )}
                    </Box>
                );
                
                })}
                



            </Box>
        </Box>
    );
})}
                                    </Box>

                                    <Box sx={{ position: 'absolute', bottom: 120, right: 20, display: 'flex', flexDirection: 'column', gap: 1, alignItems: 'center' }}>
                                        {mentionIds.length > 0 && (
                                            <Tooltip title="Перейти к упоминанию" placement="left">
                                                <Box sx={{ position: 'relative' }}>
                                                    <IconButton onClick={handleJumpToMention} sx={{ bgcolor: '#1e293b', color: '#38bdf8', border: '2px solid #38bdf8', '&:hover': { bgcolor: '#334155' }, boxShadow: '0 4px 12px rgba(0,0,0,0.5)' }}>
                                                        <Typography sx={{ fontWeight: 'bold', fontSize: '1.2rem' }}>@</Typography>
                                                    </IconButton>
                                                    <Box sx={{ position: 'absolute', top: -5, right: -5, bgcolor: '#ef4444', color: 'white', borderRadius: '50%', minWidth: 20, height: 20, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.7rem', fontWeight: 'bold', px: 0.5 }}>{mentionIds.length}</Box>
                                                </Box>
                                            </Tooltip>
                                        )}
                                        {showScrollDown && (
                                            <Box sx={{ position: 'relative', mt: 1 }}>
                                                <IconButton onClick={() => { setIsHistoryMode(false); loadMessages(true); setShowScrollDown(false); setUnreadMessages([]); }} sx={{ bgcolor: '#38bdf8', color: '#0f172a', '&:hover': { bgcolor: '#7dd3fc' }, boxShadow: '0 4px 12px rgba(0,0,0,0.5)' }}>
                                                    <ArrowDownward />
                                                </IconButton>
                                                {unreadMessages.length > 0 && (
                                                    <Box sx={{ position: 'absolute', top: -8, left: -8, bgcolor: '#10b981', color: 'white', borderRadius: '10px', minWidth: 22, height: 22, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.75rem', fontWeight: 'bold', px: 1, boxShadow: '0 2px 5px rgba(0,0,0,0.3)', border: '2px solid #1e293b' }}>
                                                        {unreadMessages.length}
                                                    </Box>
                                                )}
                                            </Box>
                                        )}
                                    </Box>

                                    {/* ПОДВАЛ (Поле ввода) */}
                                    <Box sx={{ p: 2, bgcolor: '#0f172a', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                                        {attachment && (
                                                <Box sx={{ p: 1, px: 2, bgcolor: 'rgba(56, 189, 248, 0.05)', borderLeft: '3px solid #38bdf8', display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1, borderRadius: '4px 4px 0 0' }}>
                                                    <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                                                        {attachmentType === 'image' || attachmentType === 'video' ? (
                                                                <video src={attachmentPreview} style={{ height: '60px', borderRadius: '4px', objectFit: 'cover' }} />
                                                            ): (
                                                            <Box sx={{ height: '60px', width: '60px', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#1e293b', borderRadius: '4px' }}>
                                                                <InsertDriveFile sx={{ color: '#38bdf8', fontSize: 32 }} />
                                                            </Box>
                                                        )}
                                                        <Box>
                                                            <Typography sx={{ color: '#38bdf8', fontSize: '0.85rem', fontWeight: 'bold' }}>
                                                                {attachmentType === 'image' ? 'Изображение' : attachmentType === 'audio' ? 'Аудио' : attachmentType === 'video' ? 'Видео' : 'Документ'}
                                                            </Typography>
                                                            <Typography sx={{ color: '#94a3b8', fontSize: '0.75rem' }}>{attachment.name} ({formatBytes(attachment.size)})</Typography>
                                                        </Box>
                                                    </Box>
                                                    <IconButton size="small" onClick={clearAttachment} sx={{ color: '#64748b' }}><CloseIcon fontSize="small" /></IconButton>
                                                </Box>
                                            )}
                                        {editingMessage && (
                                            <Box sx={{ p: 1, px: 2, bgcolor: 'rgba(56, 189, 248, 0.05)', borderLeft: '3px solid #38bdf8', display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, overflow: 'hidden' }}>
                                                    <Edit sx={{ color: '#38bdf8', fontSize: 18 }} />
                                                    <Box sx={{ overflow: 'hidden' }}>
                                                        <Typography sx={{ color: '#38bdf8', fontSize: '0.85rem', fontWeight: 'bold' }}>Редактировать сообщение</Typography>
                                                        <Typography noWrap sx={{ color: '#94a3b8', fontSize: '0.8rem' }}>{editingMessage.text}</Typography>
                                                    </Box>
                                                </Box>
                                                <IconButton size="small" onClick={() => { setEditingMessage(null); setNewMessage(''); }} sx={{ color: '#64748b' }}><CloseIcon fontSize="small" /></IconButton>
                                            </Box>
                                        )}
                                        {replyTo && !editingMessage && (
                                            <Box sx={{ p: 1, px: 2, bgcolor: 'rgba(255,255,255,0.05)', borderLeft: '3px solid #38bdf8', display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1, borderRadius: '4px 4px 0 0' }}>
                                                <Box sx={{ overflow: 'hidden' }}>
                                                    <Typography sx={{ color: '#38bdf8', fontSize: '0.85rem', fontWeight: 'bold' }}>Ответ {replyTo.senderId === auth.user.id ? 'себе' : replyTo.name || activeChat.name}</Typography>
                                                    <Typography noWrap sx={{ color: '#94a3b8', fontSize: '0.8rem' }}>{replyTo.text || 'Вложение'}</Typography>
                                                </Box>
                                                <IconButton size="small" onClick={() => setReplyTo(null)} sx={{ color: '#64748b' }}><CloseIcon fontSize="small" /></IconButton>
                                            </Box>
                                        )}

                                        {activeChat.can_reply ? (
        <Box component="form" onSubmit={handleSendMessage} sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            <Box sx={{ flex: 1, position: 'relative', display: 'flex', alignItems: 'center', bgcolor: '#1e293b', borderRadius: 2 }}>
                {/* СКРЫТЫЕ ИНПУТЫ */}
                {/* 1. Мультимедиа (только фото и звук) */}
                <input type="file" accept="image/*,audio/*,video/*" style={{ display: 'none' }} ref={fileInputRef} onChange={(e) => handleFileSelect(e)} />
                {/* 2. Все файлы (Документы) */}
                <input type="file" accept="*" style={{ display: 'none' }} ref={allFileInputRef} onChange={(e) => handleFileSelect(e)} />

                <IconButton 
                    // Открываем при наведении
                    onMouseEnter={handleAttachOpen}
                    onMouseLeave={handleAttachClose}
                    // При обычном клике - выбираем любой файл (Документ)
                    onClick={() => allFileInputRef.current.click()} 
                    sx={{ color: attachAnchor ? '#38bdf8' : '#94a3b8', marginLeft:"8px", '&:hover': { color: '#38bdf8' } }}
                >
                    <AttachFile />
                </IconButton>

                {/* МЕНЮ ПРИ НАВЕДЕНИИ */}
                <Popover
                    open={Boolean(attachAnchor)}
                    anchorEl={attachAnchor}
                    onClose={() => setAttachAnchor(null)}
                    disableRestoreFocus
                    sx={{ pointerEvents: 'none' }}
                    anchorOrigin={{ vertical: 'top', horizontal: 'left' }}
                    transformOrigin={{ vertical: 'bottom', horizontal: 'left' }}
                    PaperProps={{
                        onMouseEnter: handleAttachEnterMenu,
                        onMouseLeave: handleAttachClose,
                        sx: { 
                            pointerEvents: 'auto', 
                            bgcolor: '#1e293b', 
                            color: 'white', 
                            margin: "-6px 0 0 -8px"
                        }
                    }}
                >
                    <List sx={{ p: 0.5 }}>
                        <MenuItem onClick={() => fileInputRef.current.click()} sx={{ borderRadius: 1, gap: 1.5 }}>
                            <AddCircle sx={{ color: '#38bdf8' }} /> Multimedia
                        </MenuItem>
                        <MenuItem onClick={() => allFileInputRef.current.click()} sx={{ borderRadius: 1, gap: 1.5 }}>
                            <InsertDriveFile sx={{ color: '#10b981' }} /> Document
                        </MenuItem>
                  
                        <MenuItem onClick={() => { setAttachAnchor(null); setIsPollModalOpen(true); }} sx={{ borderRadius: 1, gap: 1.5 }}>
                            <BarChart sx={{ color: '#f59e0b', transform: 'rotate(90deg)' }} /> Poll
                        </MenuItem>
                    </List>
                </Popover>
                {/* ИНПУТ */}
                <InputBase
                    value={newMessage}
                    onChange={(e) => {
                        setNewMessage(e.target.value);
                        throttleTyping();
                    }}
                    onPaste={handlePaste}
                    placeholder="Написать сообщение..."
                    fullWidth
                    sx={{ color: 'white', px: 2, py: 1,  
                        'input:focus': {
                        outline: 'none',     // Убирает стандартный контур браузера
                        boxShadow: 'none',   // Убирает тень MUI
                        border: 'none'       // На случай, если это border
                    },
                    'input:focus-visible': {
                        outline: 'none',     // Убирает стандартный контур браузера
                        boxShadow: 'none',   // Убирает тень MUI
                        border: 'none'       // На случай, если это border
                    }  
                }}
                />

                {/* КНОПКА СКРЕПКИ ВНУТРИ ИНПУТА СПРАВА */}
                

                {/* КНОПКА ЭМОДЗИ/МЕДИА СПРАВА ВНУТРИ ИНПУТА */}
                <IconButton 
                onMouseEnter={handleMediaOpen}
                    onMouseLeave={handleMediaClose}
                    onClick={handleMediaClick}
                    sx={{ color: mediaAnchor ? '#38bdf8' : '#94a3b8', mr: 1 }}
                >
                    <Mood />
                </IconButton>
            </Box>

{/* КНОПКА ОТПРАВКИ ИЛИ МИКРОФОН */}
{/* КОНТЕЙНЕР ДЛЯ ОТСЛЕЖИВАНИЯ СВАЙПОВ И КНОПКИ */}
{/* КОНТЕЙНЕР ДЛЯ КНОПКИ (События теперь в useEffect) */}
<Box sx={{ display: 'flex', alignItems: 'center', position: 'relative' }}>
    
    {/* Анимация таймера */}
    {isRecording && (
        <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
            <Typography sx={{ color: '#ef4444', fontWeight: 'bold', fontSize: '0.9rem', animation: 'pulse 1.5s infinite' }}>
                {isRecordingLocked ? '' : ''} {formatTimeStr(recordingTime)}
            </Typography>
            
            {isRecordingLocked && (
                <IconButton size="small" onClick={(e) => stopRecording(e, true, true)} sx={{ color: '#94a3b8', ml: 1, '&:hover': { color: '#ef4444' } }}>
                    <DeleteOutline fontSize="small" />
                </IconButton>
            )}
        </Box>
    )}

    {/* Анимация замочка */}
    {isRecording && !isRecordingLocked && dragOffset < -10 && (
        <Box sx={{
            position: 'absolute',
            top: -60 + dragOffset, 
            right: 8,
            display: 'flex', flexDirection: 'column', alignItems: 'center',
            opacity: Math.min(1, Math.abs(dragOffset) / 50),
            pointerEvents: 'none',
            zIndex: 1000
        }}>
            <Lock sx={{ color: isRecordingLocked ? '#38bdf8' : '#94a3b8', fontSize: 20, mb: 0.5 }} />
            <ArrowDownward sx={{ color: '#94a3b8', fontSize: 16, transform: 'rotate(180deg)' }} />
        </Box>
    )}

    {/* САМА КНОПКА */}
    <Box ref={micButtonRef} sx={{ transition: isRecording ? 'none' : 'transform 0.2s ease-out', zIndex: 1001 }}>
        <IconButton 
            // Только старт записи оставляем на кнопке
            onMouseDown={(!newMessage.trim() && !attachment && !isRecording) ? startRecording : undefined}
            onTouchStart={(!newMessage.trim() && !attachment && !isRecording) ? startRecording : undefined}
            
            onClick={(e) => {
                if (newMessage.trim() || attachment) {
                    handleSendMessage(e);
                } else if (isRecordingLocked) {
                    // Если закреплено — клик отправляет
                    stopRecording(e, true, false); 
                }
            }}
            sx={{ 
                bgcolor: isRecording ? '#ef4444' : '#38bdf8', 
                color: isRecording ? 'white' : '#040b14', 
                touchAction: 'none', 
                userSelect: 'none',
                width: 48, height: 48,
                boxShadow: isRecording ? '0 0 20px rgba(239, 68, 68, 0.6)' : 'none',
                '&:hover': { bgcolor: isRecording ? '#dc2626' : '#7dd3fc' }
            }}
        >
            {editingMessage ? <Check /> : 
                ((newMessage.trim() || attachment) ? <Send /> : 
                (isRecordingLocked ? <Send sx={{ transform: 'translateX(2px)' }} /> : <Mic />)
            )}
        </IconButton>
    </Box>
</Box>
        </Box>
    ) : (
                                            <Typography align="center" color="error" sx={{ fontSize: '0.85rem', opacity: 0.8, py: 1 }}>
                                                {activeChat.is_group ? "Вам запрещено отправлять сообщения" : "Вы не можете писать в этот чат (требуется взаимная подписка)"}
                                            </Typography>
                                        )}
                                    </Box>
                                </>
                            ) : (
                                <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>Выберите чат для начала общения</Box>
                            )}
                        </Box>
                    </Paper>
                </Container>
            </Box>

            <Dialog open={isCreateGroupOpen} onClose={() => setIsCreateGroupOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', borderRadius: 3, p: 2, minWidth: 350 } }}>
                <Typography variant="h6" fontWeight="bold" mb={2}>Новая группа</Typography>

                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
                    <Avatar src={newGroupAvatarPreview} sx={{ width: 80, height: 80, bgcolor: '#38bdf8', fontSize: 32, mb: 1 }}>
                        {newGroupName ? newGroupName[0] : <AddCircle />}
                    </Avatar>
                    <Button component="label" size="small" sx={{ textTransform: 'none', color: '#38bdf8' }}>
                        Загрузить фото
                        <input type="file" hidden accept="image/*" onChange={(e) => {
                            const file = e.target.files[0];
                            if (file) {
                                setNewGroupAvatar(file);
                                setNewGroupAvatarPreview(URL.createObjectURL(file));
                            }
                        }} />
                    </Button>
                </Box>

                <InputBase
                    value={newGroupName}
                    onChange={e => setNewGroupName(e.target.value)}
                    placeholder="Название группы..."
                    fullWidth
                    sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1, borderRadius: 2, mb: 1.5 }}
                />
                <InputBase
                    value={newGroupDesc}
                    onChange={e => setNewGroupDesc(e.target.value)}
                    placeholder="Описание (необязательно)..."
                    fullWidth
                    multiline
                    rows={2}
                    sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1, borderRadius: 2, mb: 3 }}
                />

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                    <Button onClick={() => setIsCreateGroupOpen(false)} sx={{ color: '#94a3b8', textTransform: 'none' }}>Отмена</Button>
                    <Button onClick={handleCreateGroup} variant="contained" disabled={!newGroupName.trim()} sx={{ bgcolor: '#38bdf8', color: '#0f172a', textTransform: 'none', '&:hover': { bgcolor: '#0ea5e9' } }}>Создать</Button>
                </Box>
            </Dialog>

            <Menu anchorEl={headerMenuAnchor} open={Boolean(headerMenuAnchor)} onClose={() => setHeaderMenuAnchor(null)} transformOrigin={{ horizontal: 'right', vertical: 'top' }} anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', border: '1px solid rgba(255,255,255,0.1)', boxShadow: '0 4px 20px rgba(0,0,0,0.5)', minWidth: 150, mt: 1 } }}>
                <MenuItem onClick={() => { setIsProfileModalOpen(true); setHeaderMenuAnchor(null); }} sx={{ gap: 1.5, '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' } }}>
                    <Person fontSize="small" sx={{ color: '#38bdf8' }} /> Посмотреть профиль
                </MenuItem>
                <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)' }} />
                <MenuItem onClick={handleDeleteChat} sx={{ gap: 1.5, color: '#ef4444', '&:hover': { bgcolor: 'rgba(239, 68, 68, 0.1)' } }}>
                    <Delete fontSize="small" /> Очистить историю
                </MenuItem>
            </Menu>

            <Menu
                open={contextMenu !== null}
                onClose={handleCloseMenu}
                anchorReference="anchorPosition"
                anchorPosition={contextMenu !== null ? { top: contextMenu.mouseY, left: contextMenu.mouseX } : undefined}
                onContextMenu={(e) => {
                    e.preventDefault();
                    handleCloseMenu();
                }}
                slotProps={{
                    backdrop: {
                        onContextMenu: (e) => {
                            e.preventDefault();
                            handleCloseMenu();
                        }
                    }
                }}
                PaperProps={{
                    sx: {
                        bgcolor: '#1e293b',
                        color: 'white',
                        border: '1px solid rgba(255,255,255,0.1)',
                        boxShadow: '0 4px 20px rgba(0,0,0,0.5)',
                        minWidth: 150,
                        borderRadius: "16px"
                    }
                }}
            >
                {activeChat?.permissions?.addReactions !== false && (
                    <>
                        <Box sx={{ display: 'flex', gap: 0.5, px: 2, py: 1, bgcolor: 'rgba(255,255,255,0.02)', alignItems: 'center' }}>
                            {['❤️', '👍', '🔥', '😭', '🥰', '⚡️'].map(emoji => (
                                <IconButton key={emoji} size="small" onClick={() => handleReact(contextMenu.msg.id, emoji)} sx={{ fontSize: '1.2rem', '&:hover': { bgcolor: 'rgba(56, 189, 248, 0.1)' } }}>
                                    {emoji}
                                </IconButton>
                            ))}
                            <Divider orientation="vertical" flexItem sx={{ borderColor: 'rgba(255,255,255,0.1)', mx: 0.5 }} />
                            <IconButton size="small" onClick={(e) => { setReactionPickerAnchor(e.currentTarget); setReactingMessageId(contextMenu.msg.id); }} sx={{ bgcolor: 'rgba(255,255,255,0.05)', '&:hover': { bgcolor: 'rgba(56, 189, 248, 0.2)' } }}>
                                <AddReaction fontSize="small" sx={{ color: '#94a3b8' }} />
                            </IconButton>
                        </Box>
                        <Divider sx={{ borderColor: 'rgba(255,255,255,0.1)' }} />
                    </>
                )}
                <MenuItem onClick={handleReplyClick} sx={{ gap: 1.5 }}><Reply fontSize="small" sx={{ color: '#38bdf8' }} /> Ответить</MenuItem>
                {activeChat?.permissions?.canForward !== false && (
                    <MenuItem onClick={() => { setForwardMessageId(contextMenu.msg.id); setIsForwardModalOpen(true); handleCloseMenu(); }} sx={{ gap: 1.5 }}>
                        <Forward fontSize="small" sx={{ color: '#38bdf8' }} /> Переслать
                    </MenuItem>
                )}
                <MenuItem onClick={handleCopyText} sx={{ gap: 1.5 }}><ContentCopy fontSize="small" sx={{ color: '#94a3b8' }} /> Копировать текст</MenuItem>
                {(contextMenu?.msg?.image || contextMenu?.msg?.gif_url) && [
                    <MenuItem key="download" onClick={handleDownloadImage} sx={{ gap: 1.5 }}><Download fontSize="small" sx={{ color: '#10b981' }} /> Скачать изображение</MenuItem>,
                    <MenuItem key="copy-img" onClick={handleCopyImage} sx={{ gap: 1.5 }}><ContentCopy fontSize="small" sx={{ color: '#f59e0b' }} /> Копировать изображение</MenuItem>
                ]}
                {contextMenu?.msg?.gif_url && (
                    <MenuItem key="fav-gif" onClick={handleToggleFavoriteGif} sx={{ gap: 1.5 }}>
                        {favoriteGifs.includes(contextMenu.msg.gif_url) ? <><Star fontSize="small" sx={{ color: '#f59e0b' }} /> Убрать из избранного</> : <><StarBorder fontSize="small" sx={{ color: '#f59e0b' }} /> В избранное</>}
                    </MenuItem>
                )}
                {activeChat?.permissions?.pinMessages !== false && <MenuItem onClick={handleTogglePin} sx={{ gap: 1.5 }}>
                    <PushPin fontSize="small" sx={{ color: contextMenu?.msg.is_pinned ? '#38bdf8' : '#94a3b8', transform: 'rotate(45deg)' }} />
                    <Typography>{contextMenu?.msg.is_pinned ? 'Открепить' : 'Закрепить'}</Typography>
                </MenuItem>}
                {contextMenu?.msg?.senderId === auth?.user?.id && [
                    <Divider key="div" sx={{ borderColor: 'rgba(255,255,255,0.1)' }} />,
                    <MenuItem key="edit" onClick={handleEditClick} sx={{ gap: 1.5 }}><Edit fontSize="small" sx={{ color: '#38bdf8' }} /> Редактировать</MenuItem>,
                ]}
                <MenuItem onClick={handleDeleteClick} sx={{ gap: 1.5, color: '#ef4444' }}>
                    <Delete fontSize="small" sx={{ color: '#ef4444' }} /> Удалить
                </MenuItem>
            </Menu>

            <Popover open={Boolean(emojiAnchor)} anchorEl={emojiAnchor} onClose={() => setEmojiAnchor(null)} anchorOrigin={{ vertical: 'top', horizontal: 'center' }} transformOrigin={{ vertical: 'bottom', horizontal: 'center' }} sx={{ mt: -1 }}>
                <EmojiPicker theme="dark" onEmojiClick={(e) => setNewMessage(prev => prev + e.emoji)} />
            </Popover>

            <Popover open={Boolean(gifAnchor)} anchorEl={gifAnchor} onClose={() => setGifAnchor(null)} anchorOrigin={{ vertical: 'top', horizontal: 'center' }} transformOrigin={{ vertical: 'bottom', horizontal: 'center' }} sx={{ mt: -1 }} PaperProps={{ sx: { bgcolor: '#0f172a', border: '1px solid rgba(255,255,255,0.1)', borderRadius: 2 } }}>

            </Popover>
<Popover
    open={Boolean(mediaAnchor)}
    anchorEl={mediaAnchor}
    onClose={handleFullClose}
    // Эти настройки важны, чтобы не было конфликтов с курсором
    disableRestoreFocus
    sx={{ pointerEvents: 'none' }} 
    PaperProps={{
        onMouseEnter: handleMediaEnterWindow, // ✨ Отменяем закрытие
        onMouseLeave: handleMediaClose,      // ✨ Снова запускаем таймер при уходе
        sx: { 
            pointerEvents: 'auto', // Возвращаем кликабельность окну
            bgcolor: '#1e293b', 
            border: '1px solid rgba(255,255,255,0.1)', 
            margin: "-6px 0 0 8px",
            borderRadius: 3, 
            mb: 1 
        }
    }}
    anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
    transformOrigin={{ vertical: 'bottom', horizontal: 'right' }}
>
    <Box sx={{ width: 340, height: 450, display: 'flex', flexDirection: 'column' }}>
        
        {/* КОНТЕНТ В ЗАВИСИМОСТИ ОТ ТАБА */}
        <Box sx={{ flex: 1, overflow: 'hidden' }}>
            {mediaTab === 0 && (
                <Box sx={{
        width: '100%',
        height: '100%',
        // Инъекция стилей во внутренние классы пикера
        '& .epr-header': {
            display: 'flex',
            flexDirection: 'column-reverse', // ✨ Меняем местами Поиск и Категории
            gap: "0"
        },
        '& .epr-header div[id="epr-category-nav-id"]': {
            paddingTop: '12px',
            paddingBottom: '0px',
        },
        '& .epr-emoji-category-label': {
            fontSize: '0.75rem', // Уменьшаем заголовки категорий (Люди, Природа и т.д.)
            height: '30px',
        },
        '& .epr-search-container': {
            padding: '0',
        },
        '& .epr-search-container input': {
            paddingLeft: '40px',
        },
        '& .epr-category-navigation': {
            padding: '2px 10px',
            backgroundColor: 'transparent',
        },
        '& .epr-category-navigation': {
            padding: '2px 10px',
            backgroundColor: 'transparent',
        },

        // Настройка самих эмодзи
        '--epr-emoji-size': '22px', // ✨ Уменьшаем размер (было ~30px), теперь влезет больше колонок
        '--epr-category-navigation-button-size': '25px', // Уменьшаем иконки категорий
    }}>
            <EmojiPicker 
                theme="dark" 
                width="100%" 
                height="100%"
                // Настройки интерфейса
                searchPlaceHolder="Поиск..."
                previewConfig={{ showPreview: false }} 
                skinTonesDisabled
                // Кастомизация стиля
                style={{
                '--epr-emoji-size': '32px',        // Размер самого смайла
                '--epr-emoji-padding': '4px',      // Отступ между ними
                '--epr-bg-color': '#1e293b',
                '--epr-category-label-bg-color': '#1e293b',
                '--epr-search-input-bg-color': '#0f172a',
                '--epr-highlight-color': '#38bdf8',
                '--epr-search-input-border-radius': '10px',
                '--epr-horizontal-padding': '12px',
                    border: 'none', // Убираем стандартную рамку пикера
                }}
                onEmojiClick={(e) => setNewMessage(prev => prev + e.emoji)} 
            />
            </Box>
            )}
            {mediaTab === 1 && (
                <Box sx={{ p: 5, textAlign: 'center', color: '#64748b' }}>
                    <Typography sx={{ mb: 2 }}>Твои стикеры</Typography>
                    
                    {/* ✨ НАША НОВАЯ КНОПКА */}
                    <Button 
                        variant="outlined" 
                        onClick={() => setIsImportStickersOpen(true)}
                        sx={{ 
                            color: '#38bdf8', 
                            borderColor: 'rgba(56, 189, 248, 0.5)',
                            textTransform: 'none',
                            '&:hover': { borderColor: '#38bdf8', bgcolor: 'rgba(56, 189, 248, 0.1)' }
                        }}
                    >
                        Импорт из Telegram
                    </Button>
                </Box>
            )}
            {mediaTab === 2 && (
                <Box sx={{ p: 2, height: '100%', display: 'flex', flexDirection: 'column' }}>
                    <InputBase 
                        placeholder="Поиск GIF в Tenor..." 
                        value={gifSearch} 
                        onChange={(e) => setGifSearch(e.target.value)} 
                        fullWidth 
                        sx={{ mb: 1, color: 'white', bgcolor: '#0f172a', px: 1.5, py: 0.5, borderRadius: 2 }} 
                    />
                    <Box sx={{ flex: 1, overflowY: 'auto', overflowX: 'hidden' }}>
                        {/* Сетка гифок */}
                        <Box sx={{ 
                            display: 'grid', 
                            gridTemplateColumns: 'repeat(2, 1fr)', 
                            gap: '8px',
                            '& img': {
                                width: '100%',
                                height: '120px',
                                objectFit: 'cover',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                transition: 'transform 0.2s',
                                '&:hover': { transform: 'scale(1.02)', filter: 'brightness(1.1)' }
                            }
                        }}>
                            {/* Если поиск пустой и есть избранное — показываем избранное, иначе результаты Tenor */}
                            {!gifSearch && favoriteGifs.length > 0 ? (
                                favoriteGifs.map(url => (
                                    <img key={url} src={url} alt="Fav" onClick={() => handleSendMessage(null, url)} />
                                ))
                            ) : (
                                tenorGifs.map(gif => (
                                    <img 
                                        key={gif.id} 
                                        // В Tenor v2 используем tinygif для превью (он легкий)
                                        src={gif.media_formats.tinygif.url} 
                                        alt={gif.content_description}
                                        onClick={() => {
                                            handleSendMessage(null, gif.media_formats.gif.url);
                                            handleFullClose(); // Закрываем поповер после выбора
                                        }} 
                                    />
                                ))
                            )}
                        </Box>
                        
                        {tenorGifs.length === 0 && gifSearch && (
                            <Typography sx={{ color: '#64748b', textAlign: 'center', mt: 4 }}>Ничего не найдено</Typography>
                        )}
                    </Box>
                </Box>
            )}
        </Box>

        {/* НИЖНЯЯ ПАНЕЛЬ ПЕРЕКЛЮЧЕНИЯ (ТАБЫ) */}
        <Box sx={{ display: 'flex', justifyContent: 'space-around', bgcolor: '#0f172a', p: 0.5 }}>
            <IconButton onClick={() => setMediaTab(0)} sx={{ color: mediaTab === 0 ? '#38bdf8' : '#64748b' }}>
                <Mood fontSize="medium" />
            </IconButton>
            <IconButton onClick={() => setMediaTab(1)} sx={{ color: mediaTab === 1 ? '#38bdf8' : '#64748b' }}>
                <StarBorder fontSize="medium" />
            </IconButton>
            <IconButton onClick={() => setMediaTab(2)} sx={{ color: mediaTab === 2 ? '#38bdf8' : '#64748b' }}>
                <GifBox fontSize="medium" />
            </IconButton>
        </Box>
    </Box>
</Popover>
            <Menu
                open={Boolean(reactionPickerAnchor)}
                onClose={handleCloseEverything}
                anchorReference="anchorPosition"
                anchorPosition={
                    contextMenu !== null
                        ? { top: contextMenu.mouseY, left: contextMenu.mouseX }
                        : undefined
                }
                transitionDuration={250}
                MenuListProps={{ disablePadding: true }}
                PaperProps={{
                    sx: {
                        bgcolor: '#161d27',
                        borderRadius: '16px',
                        boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
                        border: '1px solid rgba(255,255,255,0.1)',
                        overflow: 'hidden',
                        transformOrigin: 'top left'
                    }
                }}
            >
                <Box sx={{ p: 0, lineHeight: 0 }}>
                    <EmojiPicker
                        theme="dark"
                        width={320}
                        height={400}
                        skinTonesDisabled
                        searchDisabled={false}
                        style={{
                            '--epr-bg-color': '#1e293b',
                            '--epr-category-label-bg-color': '#1e293b',
                            '--epr-text-color': '#fff',
                        }}
                        onEmojiClick={(e) => {
                            handleReact(reactingMessageId, e.emoji);
                            handleCloseEverything();
                        }}
                    />
                </Box>
            </Menu>

            <Dialog open={Boolean(fullScreenImage)} onClose={() => setFullScreenImage(null)} maxWidth="xl" PaperProps={{ sx: { bgcolor: 'transparent', boxShadow: 'none', overflow: 'hidden', display: 'flex', justifyContent: 'center', alignItems: 'center' } }}>
                <Box sx={{ position: 'relative', display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column', gap: 2 }}>
                    <IconButton onClick={() => setFullScreenImage(null)} sx={{ position: 'absolute', top: 8, right: 8, color: 'white', bgcolor: 'rgba(0,0,0,0.5)', '&:hover': { bgcolor: 'rgba(0,0,0,0.8)' } }}><CloseIcon /></IconButton>
                    
                    {/* ✨ При клике грузим VIEW (до 1920px) */}
                    <img 
                        src={fullScreenImage?.gif_url || fullScreenImage?.image_view || fullScreenImage?.image} 
                        alt="Full screen view" 
                        style={{ maxWidth: '90vw', maxHeight: '85vh', objectFit: 'contain', borderRadius: '8px' }} 
                    />
                    
                    {/* ✨ Кнопка для скачивания MASTER (4K Оригинала) */}
                    {fullScreenImage && !fullScreenImage.gif_url && fullScreenImage.image_master && (
                        <Button 
                            variant="contained" 
                            startIcon={<Download />}
                            href={fullScreenImage.image_master} 
                            download
                            target="_blank"
                            sx={{ bgcolor: '#38bdf8', color: '#0f172a', fontWeight: 'bold', '&:hover': { bgcolor: '#0ea5e9' } }}
                        >
                            Скачать оригинал (Master)
                        </Button>
                    )}
                </Box>
            </Dialog>

            {activeChat && activeChat.user && (
                <ProfileModal open={isProfileModalOpen} onClose={() => setIsProfileModalOpen(false)} profileUser={activeChat.user} auth={auth} />
            )}

            <Dialog open={isForwardModalOpen} onClose={() => setIsForwardModalOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', borderRadius: 3, width: 400 } }}>
                <Box sx={{ p: 2 }}>
                    <Typography variant="h6" fontWeight="bold">Переслать сообщение</Typography>
                    <List sx={{ maxHeight: 300, overflow: 'auto', my: 2 }}>
                        {chats?.map(chat => (
                            <ListItemButton key={chat.id} onClick={() => { handleChatClick(chat); setSelectedChatsForForward(prev => prev.includes(chat.id) ? prev.filter(id => id !== chat.id) : [...prev, chat.id]); }}>
                                <Avatar src={chat.avatar} sx={{ mr: 2 }} />
                                <ListItemText primary={chat.name} />
                                <Checkbox checked={selectedChatsForForward.includes(chat.id)} sx={{ color: '#38bdf8' }} />
                            </ListItemButton>
                        ))}
                    </List>
                    <Box sx={{ px: 1, mb: 2 }}>
                        <FormControlLabel control={<Checkbox checked={includeAuthor} onChange={e => setIncludeAuthor(e.target.checked)} sx={{ color: '#38bdf8' }} />} label={<Typography sx={{ fontSize: '0.9rem' }}>Показывать автора сообщения</Typography>} />
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                        <Button onClick={() => setIsForwardModalOpen(false)} sx={{ color: '#94a3b8' }}>Отмена</Button>
                        <Button variant="contained" onClick={handleForward} disabled={selectedChatsForForward.length === 0} sx={{ bgcolor: '#38bdf8', color: '#0f172a', fontWeight: 'bold' }}>
                            Переслать ({selectedChatsForForward.length})
                        </Button>
                    </Box>
                </Box>
            </Dialog>
            <SettingsModal open={settingsOpen} onClose={() => setSettingsOpen(false)} auth={auth} />
            <ProfileModal
                open={Boolean(selectedUser)}
                onClose={() => setSelectedUser(null)}
                profileUser={selectedUser}
                onOpenSettings={() => setSettingsOpen(true)}
                auth={auth}
                onUpdateUser={setCurrentUser}
            />

            {/* ДИАЛОГ УДАЛЕНИЯ СООБЩЕНИЯ */}
            <Dialog
                open={deleteDialogOpen}
                onClose={() => setDeleteDialogOpen(false)}
                PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', borderRadius: 3, p: 2, minWidth: 320 } }}
            >
                <Typography variant="h6" fontWeight="bold" mb={1}>Удалить сообщение?</Typography>
                <Typography sx={{ color: '#94a3b8', fontSize: '0.9rem', mb: 2 }}>
                    Вы уверены, что хотите удалить это сообщение?
                </Typography>

                {/* ЧЕКБОКС: ПОКАЗЫВАЕТСЯ ТОЛЬКО АВТОРУ СООБЩЕНИЯ */}
                {(messageToDelete?.senderId === auth.user.id || activeChat?.permissions?.deleteOthersMessages) && (
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={deleteForEveryone}
                                onChange={(e) => setDeleteForEveryone(e.target.checked)}
                                sx={{ color: '#ef4444', '&.Mui-checked': { color: '#ef4444' } }}
                            />
                        }
                        label={<Typography sx={{ fontSize: '0.9rem' }}>Удалить у всех</Typography>}
                        sx={{ mb: 2 }}
                    />
                )}

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                    <Button onClick={() => setDeleteDialogOpen(false)} sx={{ color: '#94a3b8', textTransform: 'none' }}>Отмена</Button>
                    <Button onClick={confirmDelete} variant="contained" sx={{ bgcolor: '#ef4444', color: 'white', textTransform: 'none', '&:hover': { bgcolor: '#dc2626' } }}>
                        Удалить
                    </Button>
                </Box>
            </Dialog>


            {/* ✨ МОДАЛКА ИМПОРТА СТИКЕРОВ */}
            <Dialog 
                open={isImportStickersOpen} 
                onClose={() => !isImporting && setIsImportStickersOpen(false)}
                PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', borderRadius: 3, p: 3, minWidth: 350 } }}
            >
                <Typography variant="h6" fontWeight="bold" mb={1}>Импорт стикерпака</Typography>
                <Typography sx={{ color: '#94a3b8', fontSize: '0.85rem', mb: 3 }}>
                    Вставь ссылку на пак из Telegram (например, https://t.me/addstickers/BlueArchive82)
                </Typography>

                <InputBase
                    value={stickerPackLink}
                    onChange={e => setStickerPackLink(e.target.value)}
                    placeholder="https://t.me/addstickers/..."
                    fullWidth
                    disabled={isImporting}
                    sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1.5, borderRadius: 2, mb: 3 }}
                />

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
                    <Button 
                        onClick={() => setIsImportStickersOpen(false)} 
                        sx={{ color: '#94a3b8', textTransform: 'none' }}
                        disabled={isImporting}
                    >
                        Отмена
                    </Button>
                    <Button 
                        variant="contained" 
                        disabled={!stickerPackLink.trim() || isImporting}
                        onClick={() => {
                            setIsImporting(true);
                            // 🚀 Отправляем на бэкенд
                            api.post('/stickers/import', { link: stickerPackLink })
                                .then(res => {
                                    alert('Стикерпак успешно загружен!');
                                    setIsImportStickersOpen(false);
                                    setStickerPackLink('');
                                })
                                .catch(err => {
                                    alert(err.response?.data?.message || 'Ошибка загрузки');
                                })
                                .finally(() => setIsImporting(false));
                        }}
                        sx={{ bgcolor: '#38bdf8', color: '#0f172a', fontWeight: 'bold', textTransform: 'none' }}
                    >
                        {isImporting ? <CircularProgress size={24} color="inherit" /> : 'Загрузить'}
                    </Button>
                </Box>
            </Dialog>

<Dialog open={isPollModalOpen} onClose={() => setIsPollModalOpen(false)} PaperProps={{ sx: { bgcolor: '#1e293b', color: 'white', borderRadius: 3, p: 2, minWidth: 400 } }}>
    <Typography variant="h6" fontWeight="bold" mb={2}>Создать опрос</Typography>
    
    <InputBase value={pollQuestion} onChange={e => setPollQuestion(e.target.value)} placeholder="Ваш вопрос..." fullWidth sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1.5, borderRadius: 2, mb: 2, fontWeight: 'bold' }} />
    
    <Typography sx={{ color: '#94a3b8', fontSize: '0.8rem', mb: 1, textTransform: 'uppercase' }}>Варианты ответа</Typography>
{pollOptions.map((opt, idx) => (
    <Box key={opt.id} sx={{ display: 'flex', gap: 1, mb: 1, alignItems: 'center' }}>
        
        {/* ✨ КНОПКА ЗАГРУЗКИ ИЛИ ПРЕВЬЮ ФОТО */}
        <Box sx={{ flexShrink: 0 }}>
            {opt.imagePreview ? (
                <Box sx={{ position: 'relative', width: 42, height: 42 }}>
                    <img src={opt.imagePreview} alt="preview" style={{ width: '100%', height: '100%', borderRadius: 6, objectFit: 'cover' }} />
                    <IconButton 
                        size="small" 
                        onClick={() => setPollOptions(prev => prev.map(o => o.id === opt.id ? { ...o, imageFile: null, imagePreview: null } : o))}
                        sx={{ position: 'absolute', top: -8, right: -8, bgcolor: 'rgba(0,0,0,0.7)', p: 0.3, '&:hover': { bgcolor: '#ef4444' } }}
                    >
                        <CloseIcon sx={{ fontSize: 14, color: 'white' }}/>
                    </IconButton>
                </Box>
            ) : (
                <IconButton component="label" sx={{ color: '#94a3b8', bgcolor: '#0f172a', borderRadius: 2, width: 42, height: 42, '&:hover': { color: '#38bdf8' } }}>
            
                    <input type="file" hidden accept="image/*" onChange={(e) => {
                        const file = e.target.files[0];
                        if (file) {
                            setPollOptions(prev => prev.map(o => o.id === opt.id ? { ...o, imageFile: file, imagePreview: URL.createObjectURL(file) } : o));
                        }
                    }} />
                </IconButton>
            )}
        </Box>

        <InputBase 
            value={opt.text} 
            onChange={e => setPollOptions(prev => prev.map(o => o.id === opt.id ? { ...o, text: e.target.value } : o))} 
            placeholder={`Вариант ${idx + 1}`} 
            fullWidth sx={{ bgcolor: '#0f172a', color: 'white', px: 2, py: 1.2, borderRadius: 2 }} 
        />
        
        {pollOptions.length > 2 && (
            <IconButton size="small" onClick={() => setPollOptions(prev => prev.filter(o => o.id !== opt.id))} sx={{ color: '#ef4444' }}>
                <CloseIcon />
            </IconButton>
        )}
    </Box>
))}
    
    {pollOptions.length < 10 && (
        <Button startIcon={<AddCircle />} onClick={handleAddPollOption} sx={{ color: '#38bdf8', textTransform: 'none', mt: 1, mb: 2 }}>
            Добавить вариант
        </Button>
    )}

    <FormControlLabel 
        control={<Checkbox checked={pollMultipleChoice} onChange={e => setPollMultipleChoice(e.target.checked)} sx={{ color: '#38bdf8' }} />} 
        label="Выбор нескольких вариантов" 
    />

    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 3 }}>
        <Button onClick={() => setIsPollModalOpen(false)} sx={{ color: '#94a3b8', textTransform: 'none' }}>Отмена</Button>
        <Button onClick={handleSendPoll} variant="contained" sx={{ bgcolor: '#38bdf8', color: '#0f172a', textTransform: 'none', fontWeight: 'bold', '&:hover': { bgcolor: '#0ea5e9' } }}>Создать</Button>
    </Box>
</Dialog>
            {activeChat?.is_group && (
                <GroupProfileModal
                    open={isGroupProfileOpen}
                    onClose={() => setIsGroupProfileOpen(false)}
                    chat={activeChat}
                    auth={auth}
                    lastWsMessage={lastWsMessage}
                    onChatRemoved={handleLocalChatRemove}
                />
            )}
        </ThemeProvider>
    );
}