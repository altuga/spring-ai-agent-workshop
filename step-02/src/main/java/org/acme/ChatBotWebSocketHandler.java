package org.acme;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatBotWebSocketHandler extends TextWebSocketHandler {

    private final ChatBotService chatBotService;

    public ChatBotWebSocketHandler(ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage("Welcome to your personal Spring Boot chat bot. What can I do for you?"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String response = chatBotService.chat(message.getPayload());
        session.sendMessage(new TextMessage(response));
    }
}
