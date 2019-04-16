import app.Main;
import domain.entities.ConceptMessage;
import domain.entities.Message;
import domain.entities.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import repositories.ConceptMessageRepository;
import repositories.MessageRepository;
import repositories.UserRepository;
import services.api.MessageService;
import services.api.UserService;
import services.impl.MessageServiceImpl;
import services.impl.UserServiceImpl;

import java.time.LocalDate;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class MessageServiceTest {
    private final Long CHATBOT_ID = 1L;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ConceptMessageRepository conceptMessageRepository;
    private UserService userService;
    private MessageService messageService;
    private User andy;
    private User user;

    @Before
    public void initialize() {
        userService = new UserServiceImpl(userRepository, new BCryptPasswordEncoder());
        messageService = new MessageServiceImpl(messageRepository, conceptMessageRepository, null, userRepository);
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
        Message response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());

        Message message = messageService.requestMessageFromChatbot(user.getId());
        Assert.assertEquals("salut", message.getMessage());
        ConceptMessage conceptMessage = message.getConceptMessage();
        Assert.assertNotNull(conceptMessage);

        Assert.assertEquals(3, messageRepository.findAll().size());
        Assert.assertNotNull(conceptMessageRepository.getOne(conceptMessage.getId()));

        response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals("salut", response.getMessage());
        Assert.assertEquals(conceptMessage, response.getConceptMessage());
        Assert.assertEquals(1, response.getConceptMessage().getResponses().size());

        response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals("salut", response.getMessage());
        Assert.assertEquals(conceptMessage, response.getConceptMessage());
        Assert.assertEquals(1, response.getConceptMessage().getResponses().size());

        // learn "bună"
        message = messageService.requestMessageFromChatbot(user.getId());
        Assert.assertEquals("salut", message.getMessage());

        response = messageService.addMessage("bună", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());
        conceptMessage = conceptMessageRepository.getOne(message.getConceptMessage().getId());
        Assert.assertEquals(2, conceptMessage.getResponses().size());

        do {
            message = messageService.requestMessageFromChatbot(user.getId());
        } while (message.getMessage().equals("bună"));
        response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getMessage().equals("salut") || response.getMessage().equals("bună"));

        System.out.println("TEST PASSED: testLearnHello()");
    }
}
