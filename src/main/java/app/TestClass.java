package app;

import domain.entities.ConceptMessage;
import domain.entities.Message;
import domain.entities.User;
import org.hibernate.Hibernate;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import repositories.ConceptMessageRepository;
import repositories.MessageRepository;
import repositories.UserRepository;
import services.api.MessageService;
import services.api.UserService;

import java.time.LocalDate;

import static app.Main.CHATBOT_ID;

@Component
public class TestClass {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConceptMessageRepository conceptMessageRepository;
    private final UserService userService;
    private final MessageService messageService;
    private User andy;
    private User user;

    @Autowired
    public TestClass(UserRepository userRepository, MessageRepository messageRepository, ConceptMessageRepository conceptMessageRepository, UserService userService, MessageService messageService) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.conceptMessageRepository = conceptMessageRepository;
        this.userService = userService;
        this.messageService = messageService;
    }

    void initialize() {
        Assert.assertEquals(0, userRepository.findAll().size());
        Assert.assertEquals(0, messageRepository.findAll().size());
        andy = new User(CHATBOT_ID, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        userService.addUser(andy);
        user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", LocalDate.of(1997, 10, 24));
        userService.addUser(user);
    }

    void testLearnHello() {
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
        conceptMessage = conceptMessageRepository.getOne(message.getId());
//        Assert.assertEquals(2, conceptMessage.getResponses().size());

        do {
            message = messageService.requestMessageFromChatbot(user.getId());
        } while (message.getMessage().equals("bună"));
        response = messageService.addMessage("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getMessage().equals("salut") || response.getMessage().equals("bună"));
//        Assert.assertEquals(conceptMessage, response.getConceptMessage());

        System.out.println("TEST PASSED: testLearnHello()");
    }
}
