import app.Main;
import domain.entities.Address;
import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.RejectingExpression;
import domain.entities.Sentence;
import domain.entities.SimpleDate;
import domain.entities.User;
import domain.entities.Word;
import domain.enums.Gender;
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
import static domain.enums.ChatbotRequestType.GET_INFORMATION_FROM_USER;
import static domain.enums.ItemClass.NAME;
import static domain.enums.ItemClass.NOT_AN_INFORMATION;
import static domain.enums.LocalityType.RURAL;
import static domain.enums.SpeechType.DIRECTIVE;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class ChatService_InformationDetectionTest {
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
    private ChatService chatService;
    private InformationDetectionService informationDetectionService;
    private User andy;
    private User user;

    @Before
    public void initialize() {
        userService = new UserServiceImpl(userRepository, personalInformationRepository, new BCryptPasswordEncoder());
        messageService = new MessageServiceImpl(messageRepository);
        final ChatbotService chatbotService = new ChatbotServiceImpl(messageService, sentenceRepository, wordRepository, sentenceDetectionParametersRepository, dexRepository, linguisticExpressionRepository);
        informationDetectionService = new InformationDetectionServiceImpl(linguisticExpressionRepository, expressionItemRepository, personalInformationRepository, rejectingExpressionRepository);
        chatService = new ChatServiceImpl(messageService, userService, chatbotService, informationDetectionService, dexRepository);
        // add users
        andy = new User(null, "andy@andy.andy", "parola", "Andy", "Bot", new SimpleDate(2016, 6, 26));
        userService.addUser(andy);
        CHATBOT_ID = andy.getId();
        user = new User(null, "andrei_vd2006@yahoo.com", "parola", null, null, null);
        userService.addUser(user);

        // add data
        addLinguisticExpressions_STATEMENT();
        addRejectingExpressions();
        addSentences_DIRECTIVE();
        Main.addDefaultSentenceDetectionParametersInDb(sentenceDetectionParametersRepository);
    }

    private void addLinguisticExpressions_STATEMENT() {
        // PersonalInformation
        // PersonalInformation.firstName
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("eu", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("sunt", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.firstName
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        expressionItems.add(new ExpressionItem("e", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("numele", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("meu", NOT_AN_INFORMATION));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.firstName
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);

        // PersonalInformation.birthDay
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("pe", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, ItemClass.DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.birthDay
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, ItemClass.DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.homeAddress.planet
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("pe", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, ItemClass.NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("homeAddress.planet");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.homeAddress.planet
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, ItemClass.NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("homeAddress.planet");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);
        // PersonalInformation.homeAddress.streetNumber
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("la", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, ItemClass.NUMBER));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("homeAddress.streetNumber");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationDetectionService.addLinguisticExpression(linguisticExpression);
    }

    private void addRejectingExpressions() {
        rejectingExpressionRepository.save(new RejectingExpression("nu știu"));
        rejectingExpressionRepository.save(new RejectingExpression("nu zic"));
        rejectingExpressionRepository.save(new RejectingExpression("nu spun"));
        rejectingExpressionRepository.save(new RejectingExpression("nu cred"));
        rejectingExpressionRepository.save(new RejectingExpression("nu stau"));
        rejectingExpressionRepository.save(new RejectingExpression("secret"));
        rejectingExpressionRepository.save(new RejectingExpression("vrei ști"));
        rejectingExpressionRepository.save(new RejectingExpression("tu"));
        rejectingExpressionRepository.save(new RejectingExpression("?"));
    }

    private void addSentences_DIRECTIVE() {
        // PersonalInformation
        // PersonalInformation.firstName
        final Word word_Care = new Word("care");
        final Word word_Este = new Word("este");
        final Word word_Numele = new Word("numele");
        final Word word_Tău = new Word("tău");
        final Word word_semnulÎntrebării = new Word("?");
        List<Word> words = new ArrayList<>();
        words.add(word_Care);
        words.add(word_Este);
        words.add(word_Numele);
        words.add(word_Tău);
        words.add(word_semnulÎntrebării);
        Sentence sentence = new Sentence(words, DIRECTIVE, PersonalInformation.class, "firstName");
        sentenceRepository.save(sentence);

        // PersonalInformation.birthDay
        final Word word_Când = new Word("când");
        final Word word_Ziua = new Word("ziua");
        final Word word_Ta = new Word("ta");
        words = new ArrayList<>();
        words.add(word_Când);
        words.add(word_Este);
        words.add(word_Ziua);
        words.add(word_Ta);
        words.add(word_semnulÎntrebării);
        sentence = new Sentence(words, DIRECTIVE, PersonalInformation.class, "birthDay");
        sentenceRepository.save(sentence);

        // PersonalInformation.homeAddress.planet
        final Word word_Pe = new Word("Pe");
        final Word word_Ce = new Word("ce");
        final Word word_Stradă = new Word("stradă");
        final Word word_Stai = new Word("stai");
        words = new ArrayList<>();
        words.add(word_Pe);
        words.add(word_Ce);
        words.add(word_Stradă);
        words.add(word_Stai);
        words.add(word_semnulÎntrebării);
        sentence = new Sentence(words, DIRECTIVE, PersonalInformation.class, "homeAddress.planet");
        sentenceRepository.save(sentence);

        // PersonalInformation.homeAddress.streetNumber
        final Word word_Număr = new Word("număr");
        words = new ArrayList<>();
        words.add(word_Ce);
        words.add(word_Număr);
        words.add(word_semnulÎntrebării);
        sentence = new Sentence(words, DIRECTIVE, PersonalInformation.class, "homeAddress.streetNumber");
        sentenceRepository.save(sentence);
    }

    @Test
    public void testDetectPersonalInformation_birthDay() {
        user.getPersonalInformation().setFirstName("First Name");
        user.getPersonalInformation().setSurname("Surname");
        user.getPersonalInformation().setGender(Gender.MALE);
        user.getPersonalInformation().setBirthDay(null);
        userService.updateUser(user);
        // the chatbot request the bith day
        Message messageFromChatbot = chatService.requestMessageFromChatbot(user.getId(), GET_INFORMATION_FROM_USER);
        Assert.assertEquals(DIRECTIVE, messageFromChatbot.getEquivalentSentence().getSpeechType());
        Assert.assertEquals(PersonalInformation.class, messageFromChatbot.getEquivalentSentence().getInformationClass());
        Assert.assertEquals("birthDay", messageFromChatbot.getEquivalentSentence().getInformationFieldNamePath());
        // the user give full birth day in the format: dd.mm.yyyy
        chatService.addMessageAndIdentifyInformationAndGetResponse("pe 24.10.1997", user.getId(), andy.getId());
        Assert.assertEquals(new SimpleDate(1997, 10, 24), user.getPersonalInformation().getBirthDay());
    }

    @Test
    public void testDetectPersonalInformation_firstName___valid() {
        Assert.assertNull(user.getPersonalInformation().getFirstName());
        // the chatbot request the name
        Message messageFromChatbot = chatService.requestMessageFromChatbot(user.getId(), GET_INFORMATION_FROM_USER);
        Assert.assertEquals(DIRECTIVE, messageFromChatbot.getEquivalentSentence().getSpeechType());
        Assert.assertEquals(PersonalInformation.class, messageFromChatbot.getEquivalentSentence().getInformationClass());
        Assert.assertEquals("firstName", messageFromChatbot.getEquivalentSentence().getInformationFieldNamePath());
        // the user give his name
        chatService.addMessageAndIdentifyInformationAndGetResponse("eu sunt Andrei", user.getId(), andy.getId());
        Assert.assertEquals("Andrei", user.getPersonalInformation().getFirstName());
    }

    @Test
    public void testDetectPersonalInformation_firstName___invalid() {
        Assert.assertNull(user.getPersonalInformation().getFirstName());
        // the chatbot request the name
        Message messageFromChatbot = chatService.requestMessageFromChatbot(user.getId(), GET_INFORMATION_FROM_USER);
        Assert.assertEquals(DIRECTIVE, messageFromChatbot.getEquivalentSentence().getSpeechType());
        Assert.assertEquals(PersonalInformation.class, messageFromChatbot.getEquivalentSentence().getInformationClass());
        Assert.assertEquals("firstName", messageFromChatbot.getEquivalentSentence().getInformationFieldNamePath());
        // the user noes NOT give his name
        chatService.addMessageAndIdentifyInformationAndGetResponse("NU ÎȚI SPUN", user.getId(), andy.getId());
        Assert.assertNull(user.getPersonalInformation().getFirstName());
    }

    @Test
    public void testDetectPersonalInformation_homeAddress_planet___valid() {
        user.getPersonalInformation().setFirstName("First Name");
        user.getPersonalInformation().setSurname("Surname");
        user.getPersonalInformation().setGender(Gender.MALE);
        user.getPersonalInformation().setBirthDay(new SimpleDate(1,1,1));
        userService.updateUser(user);
        Assert.assertNull(user.getPersonalInformation().getHomeAddress());
        // the chatbot request the planet name
        Message messageFromChatbot = chatService.requestMessageFromChatbot(user.getId(), GET_INFORMATION_FROM_USER);
        Assert.assertEquals(DIRECTIVE, messageFromChatbot.getEquivalentSentence().getSpeechType());
        Assert.assertEquals(PersonalInformation.class, messageFromChatbot.getEquivalentSentence().getInformationClass());
        Assert.assertEquals("homeAddress.planet", messageFromChatbot.getEquivalentSentence().getInformationFieldNamePath());
        // the user give his planet name
        chatService.addMessageAndIdentifyInformationAndGetResponse("stau pe Marte", user.getId(), andy.getId());
        Assert.assertEquals("Marte", user.getPersonalInformation().getHomeAddress().getPlanet());
    }

    @Test
    public void testDetectPersonalInformation_homeAddress_planet___valid_2() {
        user.getPersonalInformation().setFirstName("First Name");
        user.getPersonalInformation().setSurname("Surname");
        user.getPersonalInformation().setGender(Gender.MALE);
        user.getPersonalInformation().setBirthDay(new SimpleDate(1,1,1));
        userService.updateUser(user);
        Assert.assertNull(user.getPersonalInformation().getHomeAddress());
        // the chatbot request the planet name
        Message messageFromChatbot = chatService.requestMessageFromChatbot(user.getId(), GET_INFORMATION_FROM_USER);
        Assert.assertEquals(DIRECTIVE, messageFromChatbot.getEquivalentSentence().getSpeechType());
        Assert.assertEquals(PersonalInformation.class, messageFromChatbot.getEquivalentSentence().getInformationClass());
        Assert.assertEquals("homeAddress.planet", messageFromChatbot.getEquivalentSentence().getInformationFieldNamePath());
        // the user give his planet name
        chatService.addMessageAndIdentifyInformationAndGetResponse("Marte, dar tu ?", user.getId(), andy.getId());
        Assert.assertEquals("Marte", user.getPersonalInformation().getHomeAddress().getPlanet());
    }

    @Test
    public void testDetectPersonalInformation_homeAddress_planet___invalid() {
        user.getPersonalInformation().setFirstName("First Name");
        user.getPersonalInformation().setSurname("Surname");
        user.getPersonalInformation().setGender(Gender.MALE);
        user.getPersonalInformation().setBirthDay(new SimpleDate(1,1,1));
        userService.updateUser(user);
        Assert.assertNull(user.getPersonalInformation().getHomeAddress());
        // the chatbot request the planet name
        Message messageFromChatbot = chatService.requestMessageFromChatbot(user.getId(), GET_INFORMATION_FROM_USER);
        Assert.assertEquals(DIRECTIVE, messageFromChatbot.getEquivalentSentence().getSpeechType());
        Assert.assertEquals(PersonalInformation.class, messageFromChatbot.getEquivalentSentence().getInformationClass());
        Assert.assertEquals("homeAddress.planet", messageFromChatbot.getEquivalentSentence().getInformationFieldNamePath());
        // the user give his planet name
        chatService.addMessageAndIdentifyInformationAndGetResponse("ce de vrei să ști asta ?", user.getId(), andy.getId());
        Assert.assertNull(user.getPersonalInformation().getHomeAddress());
    }

    @Test
    public void testDetectPersonalInformation_homeAddress_streetNumber___invalid() {
        user.getPersonalInformation().setFirstName("First Name");
        user.getPersonalInformation().setSurname("Surname");
        user.getPersonalInformation().setGender(Gender.MALE);
        user.getPersonalInformation().setBirthDay(new SimpleDate(1,1,1));
        user.getPersonalInformation().setHomeAddress(new Address(0L, "Marte", "Europa", "România", "Cluj", "Jucu", RURAL, "Mănăștur", "Aia mare", null, null, null));
        userService.updateUser(user);
        Assert.assertNotNull(user.getPersonalInformation().getHomeAddress());
        // the chatbot request the planet name
        Message messageFromChatbot = chatService.requestMessageFromChatbot(user.getId(), GET_INFORMATION_FROM_USER);
        Assert.assertEquals(DIRECTIVE, messageFromChatbot.getEquivalentSentence().getSpeechType());
        Assert.assertEquals(PersonalInformation.class, messageFromChatbot.getEquivalentSentence().getInformationClass());
        Assert.assertEquals("homeAddress.streetNumber", messageFromChatbot.getEquivalentSentence().getInformationFieldNamePath());
        // the user give his planet name
        chatService.addMessageAndIdentifyInformationAndGetResponse("o stradă oarecare", user.getId(), andy.getId());
        Assert.assertNull(user.getPersonalInformation().getHomeAddress().getStreetNumber());
    }
}
