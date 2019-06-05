import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import domain.enums.ItemClass;
import domain.enums.SpeechType;
import domain.information.PersonalInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import repositories.ExpressionItemRepository;
import repositories.LinguisticExpressionRepository;
import repositories.MessageRepository;
import repositories.PersonalInformationRepository;
import repositories.SentenceRepository;
import repositories.UserRepository;
import repositories.WordRepository;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.InformationService;
import services.api.MessageService;
import services.api.UserService;
import services.impl.ChatServiceImpl;
import services.impl.ChatbotServiceImpl;
import services.impl.InformationServiceImpl;
import services.impl.MessageServiceImpl;
import services.impl.UserServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static app.Main.CHATBOT_ID;
import static domain.enums.ChatbotRequestType.LEARN_TO_SPEAK;
import static domain.enums.ItemClass.NAME;
import static domain.enums.ItemClass.NOT_AN_INFORMATION;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class ChatService_TestInformationDetection {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private SentenceRepository sentenceRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private PersonalInformationRepository personalInformationRepository;
    @Autowired
    private LinguisticExpressionRepository linguisticExpressionRepository;
    @Autowired
    private ExpressionItemRepository expressionItemRepository;
    private UserService userService;
    private MessageService messageService;
    private ChatService chatService;
    private InformationService informationService;
    private User andy;
    private User user;

    @Before
    public void initialize() {
        userService = new UserServiceImpl(userRepository, personalInformationRepository, new BCryptPasswordEncoder());
        messageService = new MessageServiceImpl(messageRepository);
        final ChatbotService chatbotService = new ChatbotServiceImpl(sentenceRepository, wordRepository, linguisticExpressionRepository);
        informationService = new InformationServiceImpl(linguisticExpressionRepository, expressionItemRepository, personalInformationRepository);
        chatService = new ChatServiceImpl(messageService, userService, chatbotService, informationService);
        // add users
        andy = new User(null, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        userService.addUser(andy);
        CHATBOT_ID = andy.getId();
        user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", LocalDate.of(1997, 10, 24));
        userService.addUser(user);

        // add data
        addLinguisticExpressions(); // TODO: requestMessageFromAndy să returneze o propoziție care îl întreabă informații
    }

    private void addLinguisticExpressions() {
        // PersonalInformation
        // PersonalInformation.firstName
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("eu", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("sunt", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.firstName
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        expressionItems.add(new ExpressionItem("e", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("numele", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("meu", NOT_AN_INFORMATION));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.firstName
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);

        // PersonalInformation.birthDay
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("pe", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, ItemClass.DATE));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.birthDay
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, ItemClass.DATE));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
    }

    @Test
    public void testLearnHello() {
        // learn "salut"
        Message response = chatService.addMessageAndGetResponse("salut", user.getId(), andy.getId());
        Assert.assertTrue(response.getIsUnknownMessage());

        Message message = chatService.requestMessageFromChatbot(user.getId(), LEARN_TO_SPEAK);
        Assert.assertEquals("salut", message.getText().toLowerCase());
        Sentence sentence = message.getEquivalentSentence();
        Assert.assertNotNull(sentence);

        Assert.assertEquals(3, messageRepository.findAll().size());
        Assert.assertEquals(1, sentenceRepository.findAll().size());

    }
}
