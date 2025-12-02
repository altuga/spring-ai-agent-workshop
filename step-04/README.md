# Step 4 - Tools

AI agents can leverage tools to help them perform their tasks.
Instead of hardcoding the workflow, the language model can decide when and how to use a tool.
Check the [documentation](https://docs.spring.io/spring-ai/reference/api/tools.html) on function calling (also called tools).

## Tools

### Location tool

We are going to let the chatbot figure out where we are.
And ask for recommendations based on our location.
First we need an external system that we can call.
We will use an endpoint that returns our current public IP address and location.

You can check it out here:
http://ip-api.com/

To integrate it in our application we first have to create a service for it:

```java
@Service
public class IPLookupService implements Function<String, String> {

    private final RestClient restClient;

    public IPLookupService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://ip-api.com").build();
    }

    @Override
    public String apply(String s) {
        return getLocation();
    }

    public String getLocation() {
        return restClient.get()
                .uri("/json")
                .retrieve()
                .body(String.class);
    }
}
```

Next, we should register the tool as a Bean with `@Description` to tell Spring AI that it should be used as a tool.
Give it a good description so that the model can learn what it does.

```java
@Configuration
public class AppConfig {

    @Bean
    @Description("Get location based on public IP")
    public Function<String, String> ipLookupService(IPLookupService service) {
        return service;
    }
}
```

Finally, we have to register the tool in our `ChatClient`:

```java
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are a helpful bot that helps users with recommendations about their location.")
                .defaultFunctions("ipLookupService")
                .build();
    }
    // ...
}
```

This is all it takes to connect to an external system!
Spring AI takes care of the rest.

> [!Note]
> This naively gets the location from the IP address of the server and not the client.
> For demo purposes this is fine.

Now you can try asking some questions about your location.

You should check the logs to see how the tool was used.

## System message

Now is probably a good time to be more specific with the system message.
In the system message we can give the bot some more instructions.

```java
.defaultSystem("""
    You are a helpful bot that helps users with recommendations about their location.
    You can get their location and extract the latitude and longitude.
""")
```

Experiment with it and see how it works:

- try making it respond more intelligently or specific
- restrict its replies to a certain location
- ..

## Optional: Adding Security

If you want to secure your chatbot, you can add Spring Security.

First, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Then, create a `SecurityConfig` class to define a user:

```java
package org.jugistanbul;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .formLogin(withDefaults())
                .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```

## Next step

Now you are ready to move to the next [step](./../step-05-mcp-server/README.md).
