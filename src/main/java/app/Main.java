package app;

import domain.entities.User;
import dtos.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import repositories.FacebookChatRepository;
import services.api.FastLearningService;
import services.api.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {"repositories"})
@EntityScan(basePackages = {"domain"})
@ComponentScan(basePackages = {"services", "facades", "controllers", "configuration", "app", "repositories"})
public class Main {
    private final UserService userService;
    private final FastLearningService fastLearningService;
    private final FacebookChatRepository facebookChatRepository;
    public static Long CHATBOT_ID;

    @Autowired
    public Main(UserService userService, FastLearningService fastLearningService, FacebookChatRepository facebookChatRepository, Environment environment) {
        this.userService = userService;
        this.fastLearningService = fastLearningService;
        this.facebookChatRepository = facebookChatRepository;
        CHATBOT_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("chatbot.id")));

        if (userService.findAll().isEmpty()) {
            addChatbotInDb();
            addAndreiInDb();
        }

//        new Thread(() -> {
//            try {
//                Thread.sleep(5000);
//                final List<MessageDto> messageDtos = facebookChatRepository.readChatFromJsonFile("message_1.json");
//                fastLearningService.addMessages(messageDtos);
//                System.out.println("Learning finished successfully");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    public void addChatbotInDb() {
        final User andy = new User(CHATBOT_ID, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        userService.addUser(andy);
    }

    public void addAndreiInDb() {
        final User user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", null);
        userService.addUser(user);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("Server is on.");
    }

}
