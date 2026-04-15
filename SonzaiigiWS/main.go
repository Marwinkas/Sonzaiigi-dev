package main

import (
	"context"
	"encoding/json" // Нужен для json.Marshal / Unmarshal
	"fmt"
	"log"
	"net/http"
	"strconv" // Нужен для parseToInt (конвертация строки в число)
	"sync"
	"time" // Нужен для time.Now()

	"github.com/gorilla/websocket"
	"github.com/redis/go-redis/v9"
)

var ctx = context.Background()
var rdb *redis.Client // Выносим rdb в глобальную область, чтобы все функции его видели

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
}

type WSMessage struct {
	Type     string      `json:"type"`
	ChatID   int         `json:"chat_id"`
	UserID   int         `json:"user_id"`
	Username string      `json:"username"` // Убедись, что U — большая, а в теге username — маленькая!
	Data     interface{} `json:"data"`
}
func broadcastOnlineCount() {
	mutex.Lock()
	count := len(clients)
	mutex.Unlock()

	msg := WSMessage{
		Type: "online_count",
		Data: count,
	}
	payload, _ := json.Marshal(msg)
	
	// Рассылаем всем
	mutex.Lock()
	for client := range clients {
		client.WriteMessage(websocket.TextMessage, payload)
	}
	mutex.Unlock()
}
var clients = make(map[*websocket.Conn]bool)
var mutex = &sync.Mutex{}

func main() {
	// Инициализируем глобальный rdb
	rdb = redis.NewClient(&redis.Options{
		Addr: "redis:6379",
	})

	go listenToRedis()

	http.HandleFunc("/ws", handleConnections)

	fmt.Println("🚀 Go WebSocket сервер на 9999...")
	log.Fatal(http.ListenAndServe(":9999", nil))
}

func handleConnections(w http.ResponseWriter, r *http.Request) {
	// Получаем user_id из параметров ссылки: ws://.../ws?user_id=1
	userIDStr := r.URL.Query().Get("user_id")
	if userIDStr == "" {
		userIDStr = "0"
	}

	ws, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		return
	}
	defer ws.Close()

	mutex.Lock()
	clients[ws] = true
	mutex.Unlock()
broadcastOnlineCount() // <--- Юзер зашел

	defer func() {
		mutex.Lock()
		delete(clients, ws)
		mutex.Unlock()
        
        broadcastOnlineCount() // <--- Юзер вышел
		// ... остальной код (presence и т.д.) ...
	}()
	// 1. Ставим статус Online в Redis
	rdb.Set(ctx, "presence:"+userIDStr, "online", 0)
	broadcastPresence(userIDStr, "online")

	fmt.Printf("🟢 Юзер %s подключился\n", userIDStr)

	defer func() {
		mutex.Lock()
		delete(clients, ws)
		mutex.Unlock()

		// 2. При дисконнекте ставим время последнего входа
		lastSeen := time.Now().Unix()
		rdb.Set(ctx, "presence:"+userIDStr, lastSeen, 0)
		broadcastPresence(userIDStr, fmt.Sprintf("%d", lastSeen))
		fmt.Printf("🔴 Юзер %s отключился\n", userIDStr)
	}()

	for {
		var msg WSMessage
		err := ws.ReadJSON(&msg)
		if err != nil {
			break
		}

		if msg.Type == "typing" {
			payload, _ := json.Marshal(msg)
			rdb.Publish(ctx, "chat_events", payload)
		}
	}
}

func broadcastPresence(userID string, status string) {
	event := WSMessage{
		Type:   "presence",
		UserID: parseToInt(userID),
		Data:   status,
	}
	payload, _ := json.Marshal(event)
	rdb.Publish(ctx, "chat_events", payload)
}

func listenToRedis() {
	pubsub := rdb.Subscribe(ctx, "chat_events")
	defer pubsub.Close()

	for {
		msg, err := pubsub.ReceiveMessage(ctx)
		if err != nil {
			continue
		}

		mutex.Lock()
		for client := range clients {
			err := client.WriteMessage(websocket.TextMessage, []byte(msg.Payload))
			if err != nil {
				client.Close()
				delete(clients, client)
			}
		}
		mutex.Unlock()
	}
}

// Та самая функция, которой не хватало
func parseToInt(s string) int {
	i, _ := strconv.Atoi(s)
	return i
}