import React, { createContext, useState, useRef, useEffect } from 'react';

export const PlayerContext = createContext();

export const PlayerProvider = ({ children }) => {
    // 1. Читаем данные из памяти при запуске
    const [playlist, setPlaylist] = useState(() => {
        const saved = localStorage.getItem('sonzaiigi_playlist');
        return saved ? JSON.parse(saved) : [];
    });

    const [currentIndex, setCurrentIndex] = useState(() => {
        const saved = localStorage.getItem('sonzaiigi_current_index');
        return saved ? parseInt(saved) : -1;
    });

    const [isPlayerVisible, setIsPlayerVisible] = useState(() => {
        return localStorage.getItem('sonzaiigi_player_visible') === 'true';
    });

    // Автоплей после перезагрузки браузеры блокируют, так что ставим false
    const [isPlaying, setIsPlaying] = useState(false); 
    const audioRef = useRef(new Audio());

    const currentTrack = currentIndex >= 0 ? playlist[currentIndex] : null;
    const [playerSettings, setPlayerSettings] = useState(() => {
        const saved = localStorage.getItem('sonzaiigi_player_settings');
        return saved ? JSON.parse(saved) : {
            showCover: true,       // Показывать обложку
            showControls: true,    // Показывать кнопки управления
            bgColor: '#0f172a',    // Базовый цвет фона
            bgOpacity: 0.95,       // Непрозрачность (0-1)
            blurAmount: 10         // Сила размытия фона (px)
        };
    });

    // 2. ✨ Сохраняем настройки при изменении
    useEffect(() => {
        localStorage.setItem('sonzaiigi_player_settings', JSON.stringify(playerSettings));
    }, [playerSettings]);

    const updatePlayerSetting = (key, value) => {
        setPlayerSettings(prev => ({ ...prev, [key]: value }));
    };
    // 2. Сохраняем видимость и плейлист при любых изменениях
    useEffect(() => {
        localStorage.setItem('sonzaiigi_player_visible', isPlayerVisible);
    }, [isPlayerVisible]);

    useEffect(() => {
        localStorage.setItem('sonzaiigi_playlist', JSON.stringify(playlist));
        localStorage.setItem('sonzaiigi_current_index', currentIndex);
    }, [playlist, currentIndex]);

    // 3. ✨ Главная магия: смена трека и восстановление времени
    useEffect(() => {
        if (currentTrack) {
            audioRef.current.src = currentTrack.audio_stream || currentTrack.audio_master;
            
            const savedTime = localStorage.getItem('sonzaiigi_player_time');
            const savedTrackId = localStorage.getItem('sonzaiigi_last_track_id');

            // Если это тот же трек, который играл до F5 — восстанавливаем секунду
            if (savedTime && savedTrackId == currentTrack.id) {
                audioRef.current.currentTime = parseFloat(savedTime);
            } else {
                // Иначе начинаем с нуля и запоминаем новый трек
                audioRef.current.currentTime = 0;
                localStorage.setItem('sonzaiigi_last_track_id', currentTrack.id);
            }

            if (isPlaying) {
                audioRef.current.play().catch(e => console.error("Auto-play prevented", e));
            }
        }
    }, [currentTrack]); // Срабатывает только при смене трека

    // 4. Управление воспроизведением
    useEffect(() => {
        if (isPlaying) {
            audioRef.current.play().catch(e => console.error("Play error", e));
        } else {
            audioRef.current.pause();
        }
    }, [isPlaying]);

    const playTrack = (track, queue = []) => {
        if (queue.length > 0) {
            setPlaylist(queue);
            const index = queue.findIndex(t => t.id === track.id);
            setCurrentIndex(index >= 0 ? index : 0);
        } else {
            setPlaylist([track]);
            setCurrentIndex(0);
        }
        setIsPlayerVisible(true);
        setIsPlaying(true);
        localStorage.setItem('sonzaiigi_last_track_id', track.id); // Обновляем активный трек
    };

    const togglePlay = () => setIsPlaying(!isPlaying);
    
    const nextTrack = () => {
        if (currentIndex < playlist.length - 1) {
            setCurrentIndex(currentIndex + 1);
            setIsPlaying(true);
        }
    };

    const prevTrack = () => {
        if (currentIndex > 0) {
            setCurrentIndex(currentIndex - 1);
            setIsPlaying(true);
        }
    };

    const closePlayer = () => {
        setIsPlayerVisible(false);
        setIsPlaying(false);
        audioRef.current.pause();
    };

    return (
        <PlayerContext.Provider value={{
            currentTrack, playlist, isPlaying, isPlayerVisible, audioRef,
            playTrack, togglePlay, nextTrack, prevTrack, closePlayer,playerSettings, updatePlayerSetting
        }}>
            {children}
        </PlayerContext.Provider>
    );
};