package app;

import domain.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import services.api.UserService;

import java.time.LocalDate;
import java.util.Objects;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {"repositories"})
@EntityScan(basePackages = {"domain"})
@ComponentScan(basePackages = {"services", "facades", "controllers", "configuration", "app"})
public class Main {
    private final UserService userService;
    public static Long CHATBOT_ID;

    @Autowired
    public Main(UserService userService, Environment environment) {
        this.userService = userService;
        CHATBOT_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("chatbot.id")));

        if (userService.findAll().isEmpty()) {
            addChatbotInDb();
        }
    }

    public void addChatbotInDb() {
        final User andy = new User(CHATBOT_ID, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        userService.addUser(andy);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("Server is on.");
    }

}
