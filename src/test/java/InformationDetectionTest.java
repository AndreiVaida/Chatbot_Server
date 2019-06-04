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
import static domain.enums.ItemClass.NOT_AN_INFORMATION;

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
    public void testDetectPersonalInformation() {
        // add linguistic expression
        final LinguisticExpression linguisticExpression = new LinguisticExpression();
        final List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("eu", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("sunt", NOT_AN_INFORMATION));
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

        // test if the name is detected
        Information information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        PersonalInformation personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());
    }
}
