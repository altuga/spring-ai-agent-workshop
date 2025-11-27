# Step 2 - Chatbot

The next step is to build a chatbot.
This can be done by creating a new service that utilizes the Spring AI `ChatClient`.

```java
@Service
public class ChatBotService {

    private final ChatClient chatClient;

    public ChatBotService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
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

This service uses the `ChatClient` to interact with the AI model. We configure a default system message in the constructor.

Next, we need to expose this service via WebSockets. We'll create a `ChatBotWebSocketHandler` to handle the WebSocket connection and messages.

```java
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
```

And register the handler in a configuration class:

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatBotService chatBotService;

    public WebSocketConfig(ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatBotWebSocketHandler(chatBotService), "/chat-bot")
                .setAllowedOrigins("*");
    }
}
```

To enable these features, ensure you have the following dependencies in your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
</dependency>
```

Finally, we have provided a simple `index.html` in `src/main/resources/static/index.html` to interact with the bot.

Congratulations, you have built your first chatbot!
You can interact with the bot by opening your browser and navigating to http://localhost:8080/

## Model configuration

To change how the bot responds, you can modify the model parameters in the `application.properties` file.

### Temperature

The temperature of the model controls how creative the bot is.

```properties
spring.ai.ollama.chat.options.temperature=0.5
```

Try experimenting with different temperatures and see how the bot responds.
For enterprise grade chatbots avoid higher temperatures to avoid bots getting too creative.

### Configuration reference

For the rest of the workshop you can use the following configuration.

```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=llama3.2
```

## System message

You can customize the system message in the `ChatBotService` constructor:

```java
this.chatClient = chatClientBuilder
        .defaultSystem("You are a helpful and friendly AI assistant.")
        .build();
```

Try to get creative with the system message and experiment with different personas.
