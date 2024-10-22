import app.Main;
import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.SimpleDate;
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
import repositories.DexRepository;
import repositories.ExpressionItemRepository;
import repositories.LinguisticExpressionRepository;
import repositories.MessageRepository;
import repositories.PersonalInformationRepository;
import repositories.RejectingExpressionRepository;
import repositories.SentenceDetectionParametersRepository;
import repositories.SentenceRepository;
import repositories.UserRepository;
import repositories.WordRepository;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.InformationDetectionService;
import services.api.MessageService;
import services.api.UserService;
import services.impl.ChatServiceImpl;
import services.impl.ChatbotServiceImpl;
import services.impl.InformationDetectionServiceImpl;
import services.impl.MessageServiceImpl;
import services.impl.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static app.Main.CHATBOT_ID;
import static domain.enums.ChatbotRequestType.LEARN_TO_SPEAK;
import static domain.enums.MessageSource.USER_CHATBOT_CONVERSATION;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class ChatService_LearningTest {
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
    private RejectingExpressionRepository rejectingExpressionRepository;
    @Autowired
    private ExpressionItemRepository expressionItemRepository;
    @Autowired
    private SentenceDetectionParametersRepository sentenceDetectionParametersRepository;
    @Autowired
    private DexRepository dexRepository;
    private UserService userService;
    private MessageService messageService;
    //private ChatbotService chatbotService;
    private ChatService chatService;
    private User andy;
    private User user;

    @Before
    public void initialize() {
        userService = new UserServiceImpl(userRepository, personalInformationRepository, new BCryptPasswordEncoder());
        messageService = new MessageServiceImpl(messageRepository);
        final ChatbotService chatbotService = new ChatbotServiceImpl(messageService, sentenceRepository, wordRepository, sentenceDetectionParametersRepository, dexRepository, linguisticExpressionRepository);
        final InformationDetectionService informationDetectionService = new InformationDetectionServiceImpl(linguisticExpressionRepository, expressionItemRepository, personalInformationRepository, rejectingExpressionRepository);
        chatService = new ChatServiceImpl(messageService, userService, chatbotService, informationDetectionService, dexRepository);
        // add users
        andy = new User(null, "andy@andy.andy", "parola", "Andy", "Bot", new SimpleDate(2016, 6, 26));
        userService.addUser(andy);
        CHATBOT_ID = andy.getId();
        user = new User(null, "andrei_vd2006@yahoo.com", "parola", "Andrei", "Vaida", new SimpleDate(1997, 10, 24));
        userService.addUser(user);
        // SentenceDetectionParameters
        Main.addDefaultSentenceDetectionParametersInDb(sentenceDetectionParametersRepository);
    }

    @Test
    public void testLearnHello() {
        final List<String> greetings = new ArrayList<>();
        greetings.add("salut");
        greetings.add("salutare");
        greetings.add("bună");
        greetings.add("servus");
        greetings.add("hey");

        // learn "salut"
        Message response = chatService.addMessageAndIdentifyInformationAndGetResponse("salut", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(response.getIsUnknownMessage());

        Message message = chatService.requestMessageFromChatbot(user.getId(), LEARN_TO_SPEAK);
        Assert.assertTrue(greetings.contains(message.getText().toLowerCase()));
        Sentence sentence = message.getEquivalentSentence();
        Assert.assertNotNull(sentence);

        Assert.assertEquals(3, messageRepository.findAll().size());
        Assert.assertEquals(1, sentenceRepository.findAll().size());

        response = chatService.addMessageAndIdentifyInformationAndGetResponse("salut", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(greetings.contains(response.getText().toLowerCase()));
        Assert.assertEquals(sentence, response.getEquivalentSentence());
        Assert.assertEquals(1, response.getEquivalentSentence().getResponses().size());

        response = chatService.addMessageAndIdentifyInformationAndGetResponse("salut", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(greetings.contains(response.getText().toLowerCase()));
        Assert.assertEquals(sentence, response.getEquivalentSentence());
        Assert.assertEquals(1, response.getEquivalentSentence().getResponses().size());

        // learn "bună"
        message = chatService.requestMessageFromChatbot(user.getId(), LEARN_TO_SPEAK);
        Assert.assertTrue(greetings.contains(message.getText().toLowerCase()));

        response = chatService.addMessageAndIdentifyInformationAndGetResponse("bună", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(response.getIsUnknownMessage());
        sentence = sentenceRepository.getOne(message.getEquivalentSentence().getId());
        Assert.assertEquals(2, sentence.getResponses().size());

        message = chatService.addMessage("bună", andy.getId(), user.getId(), USER_CHATBOT_CONVERSATION);
        response = chatService.addMessageAndIdentifyInformationAndGetResponse("salut", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(greetings.contains(response.getText().toLowerCase()));
        Assert.assertEquals(1, message.getEquivalentSentence().getResponses().size());

        // ensure that "salut" and "bună" are responses for each other
        do {
            response = chatService.addMessageAndIdentifyInformationAndGetResponse("salut", user.getId(), andy.getId()).getMessage();
        } while (!response.getText().toLowerCase().equals("salut"));
        do {
            response = chatService.addMessageAndIdentifyInformationAndGetResponse("salut", user.getId(), andy.getId()).getMessage();
        } while (!response.getText().toLowerCase().equals("bună"));
        do {
            response = chatService.addMessageAndIdentifyInformationAndGetResponse("bună", user.getId(), andy.getId()).getMessage();
        } while (!response.getText().toLowerCase().equals("salut"));
        do {
            response = chatService.addMessageAndIdentifyInformationAndGetResponse("bună", user.getId(), andy.getId()).getMessage();
        } while (!response.getText().toLowerCase().equals("bună"));

        System.out.println("TEST PASSED: testLearnHello_AND_getToKnow()");
    }

    @Test
    public void testLearnHello_AND_getToKnow() {
        testLearnHello();
        // last message should be „Salut” or „Bună”
        final List<Message> messageList = messageService.getMessagesByUsers(andy.getId(), user.getId());
        Message message = messageList.get(messageList.size() - 1);
        Assert.assertTrue(message.getText().toLowerCase().equals("salut") || message.getText().toLowerCase().equals("bună"));
        final Sentence helloSentence = message.getEquivalentSentence();

        // add „Care e numele tău ?” as reply for „Salut”
        Message response = chatService.addMessageAndIdentifyInformationAndGetResponse("Care e numele tău ?", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(response.getIsUnknownMessage());
        Assert.assertTrue(message.getEquivalentSentence().getResponses().keySet().stream()
                .anyMatch(responseSentence -> responseSentence.getWords().size() == 5)); // „Care e numele tău ?”

        // add „Mă numesc Andrei” as reply for „Care e numele tău ?”
        message = chatService.addMessage("care e numele tău ?", andy.getId(), user.getId(), USER_CHATBOT_CONVERSATION);
        Sentence sentence = message.getEquivalentSentence();
        Assert.assertEquals(5, sentence.getWords().size()); // „Care e numele tău ?”
        response = chatService.addMessageAndIdentifyInformationAndGetResponse("Mă numesc Andrei.", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(response.getIsUnknownMessage());
        sentence = sentenceRepository.getOne(sentence.getId());
        Assert.assertEquals(1, sentence.getResponses().size()); // [„Mă numesc Andrei.”]

        // add „Eu sunt Andy.” as reply for „Mă numesc Andrei”
        message = chatService.addMessage("mă numesc andrei", andy.getId(), user.getId(), USER_CHATBOT_CONVERSATION);
        sentence = message.getEquivalentSentence();
        Assert.assertTrue(sentence.getWords().get(0).getTextWithDiacritics().toLowerCase().equals("mă")
                && sentence.getWords().get(1).getTextWithDiacritics().toLowerCase().equals("numesc")
                && sentence.getWords().get(2).getTextWithDiacritics().toLowerCase().equals("andrei"));
        response = chatService.addMessageAndIdentifyInformationAndGetResponse("Eu sunt Andy.", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(response.getIsUnknownMessage());
        sentence = sentenceRepository.getOne(sentence.getId());
        Assert.assertEquals(1, sentence.getResponses().size()); // [„Eu sunt Andy.”]

        // add „Eu sunt Andy.” as reply for „Care e numele tău ?”
        message = chatService.addMessage("care e numele tău ?", andy.getId(), user.getId(), USER_CHATBOT_CONVERSATION);
        sentence = message.getEquivalentSentence();
        Assert.assertEquals(1, sentence.getResponses().size());
        response = chatService.addMessageAndIdentifyInformationAndGetResponse("Eu sunt Andy.", user.getId(), andy.getId()).getMessage();
        Assert.assertTrue(response.getIsUnknownMessage());
        sentence = sentenceRepository.getOne(sentence.getId());
        Assert.assertEquals(2, sentence.getResponses().size()); // [„Mă numesc Andrei.”, „Eu sunt Andy.”]

        // add „Eu sunt Andrei.” as reply for „Eu sunt Andy” AND as synonym
        message = chatService.addMessage("eu sunt andy", andy.getId(), user.getId(), USER_CHATBOT_CONVERSATION);
        sentence = message.getEquivalentSentence();
        Assert.assertTrue(sentence.getWords().get(0).getText().toLowerCase().equals("eu")
                && sentence.getWords().get(1).getText().toLowerCase().equals("sunt")
                && sentence.getWords().get(2).getText().toLowerCase().equals("andy"));
        response = chatService.addMessageAndIdentifyInformationAndGetResponse("Eu sunt Andrei.", user.getId(), andy.getId()).getMessage();
        Assert.assertFalse(response.getIsUnknownMessage());
        Assert.assertTrue(response.getText().toLowerCase().contains("eu sunt"));
        sentence = sentenceRepository.getOne(sentence.getId());
        Assert.assertEquals(1, sentence.getResponses().size()); // [„Eu sunt Andrei.”]
        Assert.assertEquals(2, sentence.getSynonyms().keySet().size()); // [„Eu sunt Andrei.”] // TODO check
        Assert.assertTrue(sentence.getSynonyms().keySet().stream().anyMatch(synonym -> synonym.getWords().get(2).getText().toLowerCase().equals("andrei")));

        // test again if the chatbot really learned the previous messages
        do {
            response = chatService.addMessageAndIdentifyInformationAndGetResponse(helloSentence.getWords().get(0).getText(), user.getId(), andy.getId()).getMessage();
        } while (!response.getText().toLowerCase().contains("care e numele tău"));
        response = chatService.addMessageAndIdentifyInformationAndGetResponse("Eu sunt Andrei", user.getId(), andy.getId()).getMessage();
        Assert.assertFalse(response.getIsUnknownMessage());
        Assert.assertTrue(response.getText().toLowerCase().contains("eu sunt"));

        response = chatService.addMessageAndIdentifyInformationAndGetResponse("care e numele tău ?", user.getId(), andy.getId()).getMessage();
        Assert.assertFalse(response.getIsUnknownMessage());
        Assert.assertTrue(response.getText().toLowerCase().contains("eu sunt") || response.getText().toLowerCase().contains("mă numesc"));

        response = chatService.addMessageAndIdentifyInformationAndGetResponse("eu sunt Ion", user.getId(), andy.getId()).getMessage();
        Assert.assertFalse(response.getIsUnknownMessage());
        Assert.assertTrue(response.getText().toLowerCase().contains("eu sunt"));

        System.out.println("TEST PASSED: testLearnHello_AND_getToKnow()");
    }
}
