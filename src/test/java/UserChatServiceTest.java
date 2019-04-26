import app.Main;
import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import repositories.MessageRepository;
import repositories.SentenceRepository;
import repositories.UserRepository;
import repositories.WordRepository;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.MessageService;
import services.api.UserService;
import services.impl.ChatServiceImpl;
import services.impl.ChatbotServiceImpl;
import services.impl.MessageServiceImpl;
import services.impl.UserServiceImpl;

import java.time.LocalDate;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class UserChatServiceTest {
    private final Long CHATBOT_ID = 1L;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private SentenceRepository sentenceRepository;
    @Autowired
    private MessageRepository messageRepository;
    private UserService userService;
    private MessageService messageService;
    private ChatbotService chatbotService;
    private ChatService chatService;
    private User andy;
    private User user;

    @Before
    public void initialize() {
        Main.CHATBOT_ID = CHATBOT_ID;
        userService = new UserServiceImpl(userRepository, new BCryptPasswordEncoder());
        messageService = new MessageServiceImpl(messageRepository);
        chatbotService = new ChatbotServiceImpl(sentenceRepository, wordRepository);
        chatService = new ChatServiceImpl(messageService, userService, chatbotService);
        // add users
        andy = new User(CHATBOT_ID, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        userService.addUser(andy);
        user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", LocalDate.of(1997, 10, 24));
        userService.addUser(user);
    }

    @Test
    public void testLearnHello() {
        // learn "salut"
        Message response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());

        Message message = chatService.requestMessageFromChatbot(user.getId());
        Assert.assertEquals("salut", message.getText().toLowerCase());
        Sentence sentence = message.getEquivalentSentence();
        Assert.assertNotNull(sentence);

        Assert.assertEquals(3, messageRepository.findAll().size());
        Assert.assertEquals(1, sentenceRepository.findAll().size());

        response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals("salut", response.getText().toLowerCase());
        Assert.assertEquals(sentence, response.getEquivalentSentence());
        Assert.assertEquals(1, response.getEquivalentSentence().getResponses().size());

        response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals("salut", response.getText().toLowerCase());
        Assert.assertEquals(sentence, response.getEquivalentSentence());
        Assert.assertEquals(1, response.getEquivalentSentence().getResponses().size());

        // learn "bună"
        message = chatService.requestMessageFromChatbot(user.getId());
        Assert.assertEquals("salut", message.getText().toLowerCase());

        response = chatService.addMessage("bună", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());
        sentence = sentenceRepository.getOne(message.getEquivalentSentence().getId());
        Assert.assertEquals(2, sentence.getResponses().size());

        do {
            message = chatService.requestMessageFromChatbot(user.getId());
        } while (message.getText().toLowerCase().equals("bună"));
        response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getText().toLowerCase().equals("salut") || response.getText().toLowerCase().equals("bună"));

        System.out.println("TEST PASSED: testLearnHello()");
    }
}
