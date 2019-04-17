import app.Main;
import domain.entities.Message;
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
import repositories.UserRepository;
import services.api.ChatService;
import services.api.UserService;
import services.impl.ChatServiceImpl;
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
    private MessageRepository messageRepository;
    @Autowired
    private ConceptMessageRepository conceptMessageRepository;
    private UserService userService;
    private ChatService chatService;
    private User andy;
    private User user;

    @Before
    public void initialize() {
        userService = new UserServiceImpl(userRepository, new BCryptPasswordEncoder());
        chatService = new ChatServiceImpl(conceptMessageRepository, null, chatMessageService, userRepository);
        // add users
        andy = new User(CHATBOT_ID, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        userService.addUser(andy);
        user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", LocalDate.of(1997, 10, 24));
        userService.addUser(user);
        Main.CHATBOT_ID = CHATBOT_ID;
    }

    @Test
    public void testLearnHello() {
        // learn "salut"
        Message response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());

        Message message = chatService.requestMessageFromChatbot(user.getId());
        Assert.assertEquals("salut", message.getText());
        ConceptMessage conceptMessage = message.getConceptMessage();
        Assert.assertNotNull(conceptMessage);

        Assert.assertEquals(3, messageRepository.findAll().size());
        Assert.assertNotNull(conceptMessageRepository.getOne(conceptMessage.getId()));

        response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals("salut", response.getText());
        Assert.assertEquals(conceptMessage, response.getConceptMessage());
        Assert.assertEquals(1, response.getConceptMessage().getResponses().size());

        response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals("salut", response.getText());
        Assert.assertEquals(conceptMessage, response.getConceptMessage());
        Assert.assertEquals(1, response.getConceptMessage().getResponses().size());

        // learn "bună"
        message = chatService.requestMessageFromChatbot(user.getId());
        Assert.assertEquals("salut", message.getText());

        response = chatService.addMessage("bună", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());
        conceptMessage = conceptMessageRepository.getOne(message.getConceptMessage().getId());
        Assert.assertEquals(2, conceptMessage.getResponses().size());

        do {
            message = chatService.requestMessageFromChatbot(user.getId());
        } while (message.getText().equals("bună"));
        response = chatService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getText().equals("salut") || response.getText().equals("bună"));

        System.out.println("TEST PASSED: testLearnHello()");
    }
}
