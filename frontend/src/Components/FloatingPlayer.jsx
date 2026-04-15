import React, { useContext, useState, useEffect, useRef } from 'react';
import { Rnd } from 'react-rnd';
import { Box, Typography, IconButton, Slider } from '@mui/material';
import { 
    PlayArrow, Pause, SkipNext, SkipPrevious, 
    Close, Settings, VolumeUp, VolumeDown, VolumeOff, 
    CropSquare, Minimize
} from '@mui/icons-material';
import { PlayerContext } from './PlayerContext';

// Функция для генерации красивой волны.
// Создает псевдослучайную, но постоянную для каждого трека волну, 
// имитируя настоящие перепады громкости (амплитуду).
const generateWaveform = (seedStr, count = 100) => {
    let seed = 0;
    if (seedStr) {
        for (let i = 0; i < seedStr.length; i++) seed += seedStr.charCodeAt(i);
    }
    
    const getSeededRandom = () => {
        let x = Math.sin(seed++) * 10000;
        return x - Math.floor(x);
    };

    const data = [];
    for (let i = 0; i < count; i++) {
        // Создаем огибающую, чтобы по краям волна была чуть тише, а в центре плотнее
        const envelope = Math.sin((i / count) * Math.PI) * 0.7 + 0.3; 
        const noise = 0.1 + 0.9 * getSeededRandom();
        data.push(Math.max(0.1, envelope * noise)); 
    }
    return data;
};

export default function FloatingPlayer() {
    const { 
        currentTrack, isPlaying, isPlayerVisible, togglePlay, 
        nextTrack, prevTrack, closePlayer, audioRef 
    } = useContext(PlayerContext);
    
    const [isMini, setIsMini] = useState(false);
    const [progress, setProgress] = useState(0);
    const [duration, setDuration] = useState(0);
    const [volume, setVolume] = useState(100);
    const [isMuted, setIsMuted] = useState(false);
    
    const [dominantColor, setDominantColor] = useState('#2d1b19');
    const [waveData, setWaveData] = useState([]);

    const [rndState, setRndState] = useState(() => {
        const saved = localStorage.getItem('sonzaiigi_player_pos');
        return saved ? JSON.parse(saved) : { width: 400, height: 450, x: window.innerWidth - 420, y: 100 };
    });

    const saveState = (newPos) => {
        const updated = { ...rndState, ...newPos };
        setRndState(updated);
        localStorage.setItem('sonzaiigi_player_pos', JSON.stringify(updated));
    };

    // Генерируем уникальную волну для текущего трека
    useEffect(() => {
        if (currentTrack) {
            setWaveData(generateWaveform(currentTrack.audio_title || currentTrack.id, 100));
        }
    }, [currentTrack]);

    // Твоя магия извлечения цвета
    useEffect(() => {
        if (!currentTrack?.audio_cover) return;

        const img = new Image();
        img.crossOrigin = "Anonymous";
        img.src = currentTrack.audio_cover;
        
        img.onload = () => {
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            canvas.width = img.width;
            canvas.height = img.height;
            ctx.drawImage(img, 0, 0);
            
            try {
                const imgData = ctx.getImageData(0, 0, canvas.width, canvas.height).data;
                let r = 0, g = 0, b = 0, count = 0;
                for (let i = 0; i < imgData.length; i += 40) {
                    r += imgData[i];
                    g += imgData[i + 1];
                    b += imgData[i + 2];
                    count++;
                }
                setDominantColor(`rgb(${Math.floor((r/count)*0.6)}, ${Math.floor((g/count)*0.6)}, ${Math.floor((b/count)*0.6)})`);
            } catch (e) {
                console.log("Не удалось получить цвет обложки", e);
            }
        };
    }, [currentTrack?.audio_cover]);

    // Логика аудио
    useEffect(() => {
        const audio = audioRef.current;
        const handleTimeUpdate = () => setProgress(audio.currentTime);
        const handleLoadedMetadata = () => setDuration(audio.duration);
        
        audio.addEventListener('timeupdate', handleTimeUpdate);
        audio.addEventListener('loadedmetadata', handleLoadedMetadata);
        audio.addEventListener('ended', nextTrack);

        setProgress(audio.currentTime);

        return () => {
            audio.removeEventListener('timeupdate', handleTimeUpdate);
            audio.removeEventListener('loadedmetadata', handleLoadedMetadata);
            audio.removeEventListener('ended', nextTrack);
        };
    }, [audioRef, nextTrack, currentTrack]);

    if (!isPlayerVisible || !currentTrack) return null;

    const handleSeek = (e, newValue) => {
        audioRef.current.currentTime = newValue;
        setProgress(newValue);
    };

    const handleVolumeChange = (e, newValue) => {
        const vol = newValue / 100;
        audioRef.current.volume = vol;
        setVolume(newValue);
        if (newValue > 0 && isMuted) setIsMuted(false);
        if (newValue === 0 && !isMuted) setIsMuted(true);
    };

    const toggleMute = () => {
        if (isMuted) {
            setIsMuted(false);
            audioRef.current.volume = volume > 0 ? volume / 100 : 0.5;
            if (volume === 0) setVolume(50);
        } else {
            setIsMuted(true);
            audioRef.current.volume = 0;
        }
    };

    const formatTime = (time) => {
        if (!time || isNaN(time)) return "0:00";
        const m = Math.floor(time / 60);
        const s = Math.floor(time % 60);
        return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    const currentSize = { width: rndState.width, height: rndState.height };

    // --- Компонент реалистичной волны с эффектом закрашивания ---
    const WaveformProgressBar = ({ activeColor, inactiveColor }) => {
        // Вычисляем, какая доля трека проиграна (от 0 до 1)
        const playedRatio = duration > 0 ? progress / duration : 0;

        return (
            <Box sx={{ position: 'relative', flex: 1, height: 36, display: 'flex', alignItems: 'center' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', height: '100%', gap: '1px' }}>
                    {waveData.map((val, i) => {
                        // Если индекс палочки меньше процента проигрывания — красим ярко
                        const isPlayed = (i / waveData.length) <= playedRatio;
                        return (
                            <Box 
                                key={i}
                                sx={{
                                    flex: 1,
                                    height: `${val * 100}%`,
                                    bgcolor: isPlayed ? activeColor : inactiveColor,
                                    borderRadius: '1px',
                                    transition: 'background-color 0.1s'
                                }}
                            />
                        );
                    })}
                </Box>

                {/* Невидимый слайдер поверх для перемотки мышкой */}
                <Slider 
                    min={0} max={duration || 100} value={progress} onChange={handleSeek}
                    sx={{ 
                        position: 'absolute', width: '100%', height: '100%', opacity: 0, p: 0, m: 0,
                        '& .MuiSlider-thumb': { width: 0, height: 0 }
                    }}
                />
            </Box>
        );
    };

    return (
        <Rnd
            size={currentSize}
            position={{ x: rndState.x, y: rndState.y }}
            onDragStop={(e, d) => saveState({ x: d.x, y: d.y })}
            onResizeStop={(e, direction, ref, delta, position) => {
                if (!isMini) saveState({ width: parseInt(ref.style.width), height: parseInt(ref.style.height), ...position });
            }}
            minWidth={350}
            minHeight={450}
            bounds="window"
            dragHandleClassName="drag-handle"
            enableResizing={!isMini}
            style={{ zIndex: 9999, position: 'fixed' }}
        >
            <Box sx={{
                width: '100%', height: '100%', 
                bgcolor: dominantColor, 
                borderRadius: 2, 
                boxShadow: '0 10px 40px rgba(0,0,0,0.8)',
                display: 'flex', flexDirection: 'column', overflow: 'hidden',
                border: '1px solid rgba(0,0,0,0.5)',
                transition: 'background-color 0.5s ease'
            }}>
                {/* --- СИСТЕМНАЯ ШАПКА --- */}
                <Box className="drag-handle" sx={{ 
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center', 
                    height: '28px', bgcolor: 'rgba(0,0,0,0.3)', cursor: 'grab', '&:active': { cursor: 'grabbing' }
                }}>
                    <Typography sx={{ color: 'rgba(255,255,255,0.7)', fontSize: '0.7rem', ml: 1, fontWeight: 'bold' }}>
                        ≡ Sonzaiigi Player
                    </Typography>
                    <Box sx={{ display: 'flex' }}>
                        <IconButton size="small" onClick={() => setIsMini(!isMini)} sx={{ color: 'rgba(255,255,255,0.7)', p: 0.2, mr: 0.5 }}>
                            {isMini ? <CropSquare sx={{ fontSize: 14 }} /> : <Minimize sx={{ fontSize: 14, mb: 1 }} />}
                        </IconButton>
                        <IconButton size="small" onClick={closePlayer} sx={{ color: 'rgba(255,255,255,0.7)', p: 0.2, mr: 0.5 }}>
                            <Close sx={{ fontSize: 16 }} />
                        </IconButton>
                    </Box>
                </Box>

                {/* ==============================================
                    РЕЖИМ 1: ПОЛНЫЙ 
                ============================================== */}
                (
                    <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', p: 3,paddingTop:"0"  }}>
                        <Box sx={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', mb: 3}}>
                            <img 
                                src={currentTrack.audio_cover} 
                                alt="cover" 
                                style={{ maxHeight: '100%',width:"60%" , maxWidth: '100%', objectFit: 'contain', boxShadow: '0 4px 20px rgba(0,0,0,0.6)', border: '2px solid rgba(255,255,255,0.05)' }} 
                            />
                        </Box>

                        <Box sx={{ textAlign: 'center', mb: 3 }}>
                            <Typography noWrap sx={{ color: 'white', fontSize: '1.1rem', fontWeight: 'bold' }}>{currentTrack.audio_title || 'Unknown Track'}</Typography>
                            <Typography noWrap sx={{ color: 'rgba(255,255,255,0.7)', fontSize: '0.85rem', mt: 0.5 }}>{currentTrack.audio_artist || 'Unknown Artist'}</Typography>
                        </Box>

                        {/* ПРОГРЕСС БАР (ВОЛНА) */}
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                            <Typography sx={{ color: 'rgba(255,255,255,0.6)', fontSize: '0.75rem', width: 25 }}>{formatTime(progress)}</Typography>
                            <WaveformProgressBar activeColor="#ffb74d" inactiveColor="rgba(255, 183, 77, 0.3)" />
                            <Typography sx={{ color: 'rgba(255,255,255,0.6)', fontSize: '0.75rem', width: 25, textAlign: 'right' }}>{formatTime(duration)}</Typography>
                        </Box>

                        {/* НИЖНЯЯ ПАНЕЛЬ */}
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                            {/* Лево: Громкость */}
                            <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                                <IconButton size="small" onClick={toggleMute} sx={{ color: 'rgba(255,255,255,0.7)', p: 0 }}>
                                    {isMuted || volume === 0 ? <VolumeOff sx={{ fontSize: 18 }} /> : volume < 50 ? <VolumeDown sx={{ fontSize: 18 }} /> : <VolumeUp sx={{ fontSize: 18 }} />}
                                </IconButton>
                                <Slider 
                                    size="small" min={0} max={100} value={isMuted ? 0 : volume} onChange={handleVolumeChange}
                                    sx={{ width: "60%",height:"5px",borderRadius:"5px", color: 'rgba(255,255,255,0.7)', p: 0, '& .MuiSlider-thumb': { width: 10, height: 10, opacity: 0, transition: '0.2s', '&:hover': { opacity: 1 } }, '& .MuiSlider-track': { border: 'none' } }} 
                                />
                            </Box>

                            {/* Центр: Управление */}
                            <Box sx={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 1 }}>
                                <IconButton size="small" onClick={prevTrack} sx={{ color: 'white' }}><SkipPrevious fontSize="medium" /></IconButton>
                                <IconButton onClick={togglePlay} sx={{ color: 'white' }}>
                                    {isPlaying ? <Pause fontSize="large" /> : <PlayArrow fontSize="large" />}
                                </IconButton>
                                <IconButton size="small" onClick={nextTrack} sx={{ color: 'white' }}><SkipNext fontSize="medium" /></IconButton>
                            </Box>

                            {/* Право: Настройки */}
                            <Box sx={{ flex: 1, display: 'flex', justifyContent: 'flex-end' }}>
                                <IconButton size="small" sx={{ color: 'rgba(255,255,255,0.7)' }}><Settings sx={{ fontSize: 20 }} /></IconButton>
                            </Box>
                        </Box>
                    </Box>
                )
            </Box>
        </Rnd>
    );
}