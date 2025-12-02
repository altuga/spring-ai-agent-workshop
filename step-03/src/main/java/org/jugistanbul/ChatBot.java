package org.jugistanbul;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ChatBot {

    private final ChatClient chatClient;

    public ChatBot(@NonNull ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are an assistant helping with users.")
                .build();
    }

    public String chat(@NonNull String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}