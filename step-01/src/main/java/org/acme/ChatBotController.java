package org.acme;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatBotController {

    private final ChatBot chatBot;

    public ChatBotController(ChatBot chatBot) {
        this.chatBot = chatBot;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message", defaultValue = "Hello") String message) {
        return chatBot.chat(message);
    }
}
