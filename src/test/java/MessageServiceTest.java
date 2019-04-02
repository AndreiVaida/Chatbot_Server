import domain.entities.Message;
import domain.entities.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import repositories.UserRepository;
import services.api.MessageService;
import services.api.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {
    private final Long CHATBOT_ID = 1L;
    @Mock
    public UserRepository userRepository;
    private UserService userService;
    private MessageService messageService;
    private User andy;
    private User user;

    @Before
    public void initialize() {
        System.out.println(CHATBOT_ID);
        // add users
        andy = new User(CHATBOT_ID, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        //userService.addUser(andy);
        user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", LocalDate.of(1997, 10, 24));
        //userService.addUser(user);
    }

    @Test
    public void testHello() {
        // say first Hello
        Message message = new Message();
        message.setFromUser(user);
        message.setToUser(andy);
        message.setMessage("Salut");
        message.setDateTime(LocalDateTime.now());
    }
}
