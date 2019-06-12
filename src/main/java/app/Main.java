package app;

import domain.entities.SimpleDate;
import domain.entities.User;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import repositories.FacebookChatRepository;
import services.api.AdminService;
import services.api.FastLearningService;
import services.api.UserService;

import java.io.File;
import java.io.FileInputStream;
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
    private final AdminService adminService;
    private final FastLearningService fastLearningService;
    private final FacebookChatRepository facebookChatRepository;

    @Autowired
    public Main(UserService userService, FastLearningService fastLearningService, FacebookChatRepository facebookChatRepository, Environment environment, AdminService adminService) {
        this.userService = userService;
        this.fastLearningService = fastLearningService;
        this.adminService = adminService;
        this.facebookChatRepository = facebookChatRepository;
        CHATBOT_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("chatbot.id")));
        USER_FOR_LEARNING_1_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("userForLearning1.id")));
        USER_FOR_LEARNING_2_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("userForLearning2.id")));

        if (userService.findAll().isEmpty()) {
            // add users
            addChatbotInDb(environment);
            addAdminInDb();
            addUserForLearningInDb();
            addAndreiInDb();
            // add Sentences, LinguisticExpressions and messages
            //uploadSentencesFile();
            //uploadLinguisticExpressionFile();
            //uploadMessagesFiles();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("Server is on.");
    }

    private void uploadSentencesFile() {
        try {
            final File file = new File("src/main/resources/informationDetectionData/PersonalInformation_Sentences_Directive.json");
            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input));
            adminService.addSentencesFromJsonFile(multipartFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
