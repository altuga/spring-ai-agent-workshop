# Step 1 - Introduction

This step introduces Spring AI by creating a simple ChatBot service exposed via a REST API.

## ChatBot Service

The `ChatBot` service utilizes the Spring AI `ChatClient` to interact with the AI model.

```java
@Service
public class ChatBot {

    private final ChatClient chatClient;

    public ChatBot(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are an assistant helping with users.")
                .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
```

## REST Controller

The `ChatBotController` exposes the chat functionality through a simple GET endpoint.

```java
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
```

## Running the Application

You can run the application using Maven:

```bash
./mvnw spring-boot:run
```

## Testing

Once the application is running, you can test the endpoint using `curl` or your browser:

```bash
curl "http://localhost:8080/chat?message=Tell%20me%20a%20joke"
```
