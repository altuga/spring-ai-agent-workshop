# Step 5 - MCP Server

Now that we know how we can use external tools we can now move on to MCP servers.
MCP servers are a way to create a tool that any language model can use.
For enterprise usage we can imagine setting up a dedicated MCP server for an external tool.

For the above use case we would also need authentication and authorization.

For more information on MCP see the [documentation](https://modelcontextprotocol.io/docs/getting-started/intro).

## Weather tool

Now that we know our location we can use it to get the weather forecast for our location.
Instead of creating a tool directly we are going to create an MCP server.

### Spring Boot project

Start by creating a new Spring Boot project.
Navigate to https://start.spring.io/ and create a new project.
Select the following dependencies:

- `Spring Web`
- `Spring Security`
- `OpenFeign`
- `LangChain4j`

Download the project, unzip it and open it in your favorite IDE.

You should set the http port to a dedicated port to avoid conflicts with the other app:

```properties
server.port=8081
```

Now you can start the MCP server:

```shell
./mvnw spring-boot:run
```

### Weather client

Create a Feign client to get the weather forecast.

```java
@FeignClient(name = "weather-client", url = "https://api.open-meteo.com")
public interface WeatherClient {

    @GetMapping("/v1/forecast")
    String forecast(@RequestParam("latitude") String latitude,
                    @RequestParam("longitude") String longitude,
                    @RequestParam("current") String current
    );
}
```

### Weather MCP Server

Now we can create the MCP server for the weather forecast.

```java
@Component
public class WeatherMcpServer {

    private final WeatherClient weatherClient;

    public WeatherMcpServer(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @Tool(name = "Current weather", description = "Get current weather forecast for a location.")
    public String forecast(String latitude, String longitude) {
        return weatherClient.forecast(latitude, longitude, "temperature_2m,wind_speed_10m,precipitation");
    }
}
```

Here we inject the weather client and use it to get the weather forecast.
Next we register the tool for the MCP server with the `@Tool` annotation.
Give it a good name and description so other languages can use it.

### Logging

Again we enable logging to audit what is happening:

```properties
logging.level.org.springframework.web=DEBUG
logging.level.dev.langchain4j=DEBUG
```

### Authentication

We now created a MCP server that can be used by other languages.
But we need to make sure that only authorized users can use it.

In your Spring Security configuration (e.g., `SecurityConfig.java`), ensure that the MCP endpoints are authenticated:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/mcp/**").authenticated()
            .anyRequest().permitAll()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    return http.build();
}
```

Furthermore, you can use the `@PreAuthorize` annotation on the tool to restrict access to specific roles if needed.

After enabling authentication we now need a bearer token to access the MCP server.
You can obtain one for testing through the Keycloak Admin Console or by logging in to the client app.

### MCP Client

Now that we have the MCP server with authentication we can use it from our AI agent.
Go back to the original AI agent project (this project) and add the following dependency (if not already present):

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-mcp</artifactId>
    <version>1.0.0-beta1</version>
</dependency>
```

To use it, you need to configure the `McpClient`. Create a configuration class (e.g., `McpConfig.java`):

```java
@Configuration
public class McpConfig {

    @Bean
    public McpClient weatherMcpClient() {
        // Replace with your MCP server URL
        String mcpUrl = "http://localhost:8081/mcp/sse"; 
        return new McpClient.Builder()
                .transport(new HttpMcpTransport(mcpUrl))
                .build();
    }
    
    // You will also need to register this tool with your AI Service
}
```

### MCP Client with authentication

If the MCP server requires authentication, you need to provide the access token.
You can implement a custom `McpTransport` or interceptor to inject the token.

### Run the AI agent

Finally, you can run the AI agent and use the `weather` tool.
Try asking it about the weather in your location.

> [!NOTE]
> You might want to tweak the system message for your AI agent.

## Next step

Now you are ready to move to the next [step](./../step-06-guardrails/README.md).
