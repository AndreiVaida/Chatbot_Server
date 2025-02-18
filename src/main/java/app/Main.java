package app;

import domain.entities.SentenceDetectionParameters;
import domain.entities.SimpleDate;
import domain.entities.User;
import dtos.admin.AddedDataStatus;
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
import repositories.SentenceDetectionParametersRepository;
import repositories.SentenceRepository;
import services.api.AdminService;
import services.api.UserService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private final SentenceRepository sentenceRepository;

    @Autowired
    public Main(UserService userService, Environment environment, AdminService adminService, SentenceDetectionParametersRepository sentenceDetectionParametersRepository, SentenceRepository sentenceRepository) {
        this.userService = userService;
        this.sentenceRepository = sentenceRepository;
        this.adminService = adminService;
        CHATBOT_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("chatbot.id")));
        USER_FOR_LEARNING_1_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("userForLearning1.id")));
        USER_FOR_LEARNING_2_ID = Long.valueOf(Objects.requireNonNull(environment.getProperty("userForLearning2.id")));

        // add users
        if (userService.findAll().isEmpty()) {
            addChatbotInDb(environment);
            addAdminInDb();
            addUserForLearningInDb();
            addAndreiInDb();
            addSomeWordsInDb();
        }
        // add sentence detection parameters
        if (sentenceDetectionParametersRepository.count() == 0) {
            addDefaultSentenceDetectionParametersInDb(sentenceDetectionParametersRepository);
        }
        // add sentences and linguistic expressions
        if (this.sentenceRepository.count() == 0) {
            addSentencesAndLinguisticExpressionsAndRejectingExpressionsInDb();
        }
    }

    private void addSentencesAndLinguisticExpressionsAndRejectingExpressionsInDb() {
        try {
            // sentences
            File file = new File("src/main/resources/informationDetectionData/PersonalInformation_Sentences_Directive.json");
            FileInputStream inputStream = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            AddedDataStatus status = adminService.addSentencesFromJsonFile(multipartFile);
            System.out.println("PersonalInformation_Sentences_Directive: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/FreeTimeInformation_Sentences_Directive.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addSentencesFromJsonFile(multipartFile);
            System.out.println("FreeTimeInformation_Sentences_Directive: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/SchoolInformation_Sentences_Directive.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addSentencesFromJsonFile(multipartFile);
            System.out.println("SchoolInformation_Sentences_Directive: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/FacultyInformation_Sentences_Directive.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addSentencesFromJsonFile(multipartFile);
            System.out.println("FacultyInformation_Sentences_Directive: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/RelationshipsInformation_Sentences_Directive.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addSentencesFromJsonFile(multipartFile);
            System.out.println("RelationshipsInformation_Sentences_Directive: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            // linguistic expressions
            file = new File("src/main/resources/informationDetectionData/PersonalInformation_LinguisticExpression_Statement.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addLinguisticExpressionsFromJsonFile(multipartFile);
            System.out.println("PersonalInformation_LinguisticExpression_Statement: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/FreeTimeInformation_LinguisticExpression_Statement.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addLinguisticExpressionsFromJsonFile(multipartFile);
            System.out.println("FreeTimeInformation_LinguisticExpression_Statement: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/SchoolInformation_LinguisticExpression_Statement.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addLinguisticExpressionsFromJsonFile(multipartFile);
            System.out.println("SchoolInformation_LinguisticExpression_Statement: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/FacultyInformation_LinguisticExpression_Statement.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addLinguisticExpressionsFromJsonFile(multipartFile);
            System.out.println("FacultyInformation_LinguisticExpression_Statement: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            file = new File("src/main/resources/informationDetectionData/RelationshipsInformation_LinguisticExpression_Statement.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addLinguisticExpressionsFromJsonFile(multipartFile);
            System.out.println("RelationshipsInformation_LinguisticExpression_Statement: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

            // rejecting expressions
            file = new File("src/main/resources/informationDetectionData/RejectingExpression.json");
            inputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(inputStream));
            status = adminService.addRejectingExpressionsFromJsonFile(multipartFile);
            System.out.println("RejectingExpression: " + status.getNumberOfAddedData() + "/" + status.getNumberOfData());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addSomeWordsInDb() {
        adminService.setSynonyms("e", "este");
    }

    public static void addDefaultSentenceDetectionParametersInDb(SentenceDetectionParametersRepository sentenceDetectionParametersRepository) {
        final double weight = 0.9;

        for (int sentenceLength = 1; sentenceLength <= 10; sentenceLength++) {
            int maxNrOfExtraWords;
            int maxNrOfUnmatchedWords;
            if (sentenceLength == 1) {
                maxNrOfExtraWords = 2;
                maxNrOfUnmatchedWords = 0;
            } else if (sentenceLength == 2) {
                maxNrOfExtraWords = 2;
                maxNrOfUnmatchedWords = 1;
            } else if (sentenceLength == 3) {
                maxNrOfExtraWords = 2;
                maxNrOfUnmatchedWords = 1;
            } else {
                maxNrOfExtraWords = (int) Math.round(sentenceLength * 0.7);
                maxNrOfUnmatchedWords = (int) Math.round(sentenceLength * 0.5);
            }
            sentenceDetectionParametersRepository.save(new SentenceDetectionParameters(sentenceLength, maxNrOfExtraWords, maxNrOfUnmatchedWords, weight));
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
        final User admin = new User(null, "admin", "24admin24", "Administrator", null, null);
        admin.setIsAdministrator(true);
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
