package app;

import domain.entities.SimpleDate;
import domain.entities.User;
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

import java.util.Objects;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {"repositories"})
@EntityScan(basePackages = {"domain"})
@ComponentScan(basePackages = {"services", "facades", "controllers", "configuration", "app", "repositories"})
public class Main {
    public static Long CHATBOT_ID;
    public static Long USER_FOR_LEARNING_1_ID;
    public static Long USER_FOR_LEARNING_2_ID;
    private final UserService userService;
    private final FastLearningService fastLearningService;
    private final FacebookChatRepository facebookChatRepository;

    @Autowired
    public Main(UserService userService, FastLearningService fastLearningService, FacebookChatRepository facebookChatRepository, Environment environment) {
        this.userService = userService;
        this.fastLearningService = fastLearningService;
        this.facebookChatRepository = facebookChatRepository;
        CHATBOT_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("chatbot.id")));
        CHATBOT_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("userForLearning1.id")));
        CHATBOT_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("userForLearning2.id")));

        if (userService.findAll().isEmpty()) {
            addChatbotInDb(environment);
            addAdminInDb();
            addUserForLearningInDb();
            addAndreiInDb();
        }

//        new Thread(() -> {
//            try {
//                Thread.sleep(5000);
//                final List<MessageDto> messageDtos = facebookChatRepository.readChatFromJsonFile("message_1.json");
//                fastLearningService.addMessagesFromFile(messageDtos);
//                System.out.println("Learning finished successfully");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("Server is on.");
    }

    public void addChatbotInDb(final Environment environment) {
        final User andy = new User(CHATBOT_ID, "andy@andy.andy", "parola", "Andy", "Bot", new SimpleDate(2016, 6, 26));
        userService.addUser(andy);
        CHATBOT_ID = andy.getId();
    }

    private void addAdminInDb() {
        final User admin = new User(null, "admin", "admin", "Admin", "Suprem", null);
        userService.addUser(admin);
    }

    private void addUserForLearningInDb() {
        final User user1 = new User(null, "user1@yahoo.com", "parola", "User 1", "User 2", null);
        final User user2 = new User(null, "user2@yahoo.com", "parola", "User 2", "User 2", null);
        userService.addUser(user1);
        userService.addUser(user2);
        USER_FOR_LEARNING_1_ID = user1.getId();
        USER_FOR_LEARNING_2_ID = user2.getId();
    }

    public void addAndreiInDb() {
        final User user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", null);
        userService.addUser(user);
    }

}
