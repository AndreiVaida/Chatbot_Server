import app.Main;
import domain.entities.ConceptMessage;
import domain.entities.Message;
import domain.entities.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
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
import java.time.LocalDateTime;
import java.util.Objects;

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
        Assert.assertEquals(message.getMessage(), "salut");
        ConceptMessage conceptMessage = message.getConceptMessage();
        Assert.assertNotNull(conceptMessage);

        Assert.assertEquals(messageRepository.findAll().size(), 3);
        Assert.assertNotNull(conceptMessageRepository.getOne(conceptMessage.getId()));

        response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals(response.getMessage(), "salut");
        Assert.assertEquals(response.getConceptMessage(), conceptMessage);
        Assert.assertEquals(response.getConceptMessage().getResponses().size(), 1);

        response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertEquals(response.getMessage(), "salut");
        Assert.assertEquals(response.getConceptMessage(), conceptMessage);
        Assert.assertEquals(response.getConceptMessage().getResponses().size(), 1);

        // learn "bună"
        message = messageService.requestMessageFromChatbot(user.getId());
        Assert.assertEquals(message.getMessage(), "salut");

        response = messageService.addMessage("bună", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());
        message = messageService.getMessageById(message.getId());
        Assert.assertEquals(message.getConceptMessage().getResponses().size(), 2);

        do {
            message = messageService.requestMessageFromChatbot(user.getId());
        } while (message.getMessage().equals("bună"));
        response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getMessage().equals("salut") || response.getMessage().equals("bună"));
        Assert.assertEquals(response.getConceptMessage(), conceptMessage);

        System.out.println("TEST PASSED: testLearnHello()");
    }
}
