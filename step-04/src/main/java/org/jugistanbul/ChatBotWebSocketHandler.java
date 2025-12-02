package org.jugistanbul;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatBotWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;

    public ChatBotWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = "User";
        if (session.getPrincipal() != null) {
            username = session.getPrincipal().getName();
        }
        session.sendMessage(new TextMessage("Hi " + username + "! Welcome to your personal Spring Boot chat bot. What can I do for you?"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String response = chatService.chat(message.getPayload());
        session.sendMessage(new TextMessage(response));
    }
}
