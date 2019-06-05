import com.sun.xml.internal.ws.api.model.MEP;
import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.enums.ItemClass;
import domain.enums.SpeechType;
import domain.information.Information;
import domain.information.PersonalInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import repositories.ExpressionItemRepository;
import repositories.LinguisticExpressionRepository;
import repositories.PersonalInformationRepository;
import services.api.ChatbotService;
import services.api.InformationService;
import services.impl.ChatbotServiceImpl;
import services.impl.InformationServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static domain.enums.ItemClass.*;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class InformationDetectionTest {
    @Autowired
    private PersonalInformationRepository personalInformationRepository;
    @Autowired
    private LinguisticExpressionRepository linguisticExpressionRepository;
    @Autowired
    private ExpressionItemRepository expressionItemRepository;
    private InformationService informationService;

    @Before
    public void initialize() {
        informationService = new InformationServiceImpl(linguisticExpressionRepository, expressionItemRepository);
    }

    @Test
    public void testDetectPersonalInformation_Name() {
        /* DIRECTIVE: "Care e numele tău ?" */
        // TEST 1: "eu sunt NAME"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("eu", ItemClass.NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("sunt", ItemClass.NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldName("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: firstName ("Eu sunt NAME")
        Information information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        PersonalInformation personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

        // Test 1.2: firstName - normal answer 2 ("Eu sunt NAME.")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy."));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

        // Test 1.3: firstName - normal answer 3 ("Eu sunt NAME NAME.")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy cel mare."));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy cel mare", personalInformation.getFirstName());

        // TEST 2: "NAME e numele meu"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        expressionItems.add(new ExpressionItem("e", ItemClass.NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("numele", ItemClass.NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("meu", ItemClass.NOT_AN_INFORMATION));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldName("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: firstName ("NAME e numele meu")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Andy e numele meu"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

        // Test 2.2: firstName ("NAME e numele meu.")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Andy e numele meu."));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

        // TEST 3: "NAME"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldName("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(3, informationService.getAllLinguisticExpressions().size());

        // Test 3.1: firstName ("NAME")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Andy"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

        // Test 3.1: firstName ("NAME.")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Andy."));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());
    }

    @Test
    public void testDetectPersonalInformation_BirthDay() {
        /* DIRECTIVE: "Când e ziua ta ?" */
        // TEST 1: "Pe DATE"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("pe", ItemClass.NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, ItemClass.DATE));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldName("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: birthDay ("Pe DATE")
        Information information = informationService.identifyInformation(PersonalInformation.class, "birthDay", new Message("Pe 24 octombrie"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        PersonalInformation personalInformation = (PersonalInformation) information;
        Assert.assertEquals(24, personalInformation.getBirthDay().getDayOfMonth());
        Assert.assertEquals(10, personalInformation.getBirthDay().getMonthValue());

        // Test 1.2: birthDay ("Pe DATE")
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay", new Message("Pe 24 octombrie 1997"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals(24, personalInformation.getBirthDay().getDayOfMonth());
        Assert.assertEquals(10, personalInformation.getBirthDay().getMonthValue());
        Assert.assertEquals(1997, personalInformation.getBirthDay().getYear());

        // TEST 2: "DATE"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, ItemClass.DATE));
        linguisticExpression.setItems(expressionItems);
        for (ExpressionItem item : expressionItems) {
            expressionItemRepository.save(item);
        }
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldName("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: birthDay ("DATE")
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay", new Message("24 octombrie"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals(24, personalInformation.getBirthDay().getDayOfMonth());
        Assert.assertEquals(10, personalInformation.getBirthDay().getMonthValue());
    }
}
