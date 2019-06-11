import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.User;
import domain.enums.Gender;
import domain.enums.LocalityType;
import domain.enums.SpeechType;
import domain.information.FreeTimeInformation;
import domain.information.Information;
import domain.information.PersonalInformation;
import domain.information.RelationshipsInformation;
import domain.information.SchoolInformation;
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
import services.api.InformationService;
import services.impl.InformationServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static domain.enums.ItemClass.BOOLEAN;
import static domain.enums.ItemClass.DATE;
import static domain.enums.ItemClass.GENDER;
import static domain.enums.ItemClass.LOCALITY_TYPE;
import static domain.enums.ItemClass.NAME;
import static domain.enums.ItemClass.NOT_AN_INFORMATION;
import static domain.enums.ItemClass.NUMBER;

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
    private User user;

    @Before
    public void initialize() {
        informationService = new InformationServiceImpl(linguisticExpressionRepository, expressionItemRepository, personalInformationRepository);
        user = new User();
    }

    @Test
    public void testDetectPersonalInformation_Name() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Care e numele tău ?" */
        // TEST 1: "eu sunt NAME"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("eu", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("sunt", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: firstName ("Eu sunt NAME")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());

        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("eu", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("sunt", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        expressionItems.add(new ExpressionItem(".", NOT_AN_INFORMATION));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 1.2: firstName - normal answer 2 ("Eu sunt NAME.")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy."), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());

        // Test 1.3: firstName - normal answer 3 ("Eu sunt NAME NAME.")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy cel mare."), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy cel mare", user.getPersonalInformation().getFirstName());

        // TEST 2: "NAME e numele meu"
        // add linguistic expression
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
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(3, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: firstName ("NAME e numele meu")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Andy e numele meu"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());

        // Test 2.2: firstName ("NAME e numele meu.")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Andy e numele meu."), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());

        // TEST 3: "NAME"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(4, informationService.getAllLinguisticExpressions().size());

        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        expressionItems.add(new ExpressionItem(".",NOT_AN_INFORMATION));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(5, informationService.getAllLinguisticExpressions().size());

        // Test 3.1: firstName ("NAME")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Andy"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());

        // Test 3.1: firstName ("NAME.")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Andy."), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());

        // TEST 4: "Cred că eu sunt NAME."
        // Test 3.1: firstName ("Cred că eu sunt NAME.")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Cred că eu sunt Andy."), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());

        // Test 3.1: firstName ("NAME.")
        informationService.identifyAndSetInformation(PersonalInformation.class, "firstName", new Message("Andy."), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Andy", user.getPersonalInformation().getFirstName());
    }

    @Test
    public void testDetectPersonalInformation_BirthDay() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Când e ziua ta ?" */
        // TEST 1: "Pe DATE"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("pe", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("în", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: birthDay ("În MONTH")
        informationService.identifyAndSetInformation(PersonalInformation.class, "birthDay", new Message("În octombrie"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals((Integer)10, user.getPersonalInformation().getBirthDay().getMonth());

        // Test 1.2: birthDay ("Pe DAY MONTH")
        informationService.identifyAndSetInformation(PersonalInformation.class, "birthDay", new Message("Pe 24 octombrie"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals((Integer) 24, user.getPersonalInformation().getBirthDay().getDay());
        Assert.assertEquals((Integer)10, user.getPersonalInformation().getBirthDay().getMonth());

        // Test 1.3: birthDay ("Pe DAY MONTH YEAR")
        informationService.identifyAndSetInformation(PersonalInformation.class, "birthDay", new Message("Pe 24 octombrie 1997"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals((Integer)24, user.getPersonalInformation().getBirthDay().getDay());
        Assert.assertEquals((Integer)10, user.getPersonalInformation().getBirthDay().getMonth());
        Assert.assertEquals((Integer)1997, user.getPersonalInformation().getBirthDay().getYear());

        // TEST 2: "DATE"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(3, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: birthDay ("DATE")
        informationService.identifyAndSetInformation(PersonalInformation.class, "birthDay", new Message("24 octombrie"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals((Integer)24, user.getPersonalInformation().getBirthDay().getDay());
        Assert.assertEquals((Integer)10, user.getPersonalInformation().getBirthDay().getMonth());

        /* DIRECTIVE: "În ce an te-ai născut ?" */
        // TEST 3: "În YEAR"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("în", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay.year");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("în", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay.month");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("în", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, DATE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("birthDay.day");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);

        // Test 3.1: birthDay ("În YEAR")
        informationService.identifyAndSetInformation(PersonalInformation.class, "birthDay.year", new Message("în 1997"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals((Integer)1997, user.getPersonalInformation().getBirthDay().getYear());

        // Test 3.2: birthDay ("În MONTH")
        informationService.identifyAndSetInformation(PersonalInformation.class, "birthDay.month", new Message("în octombrie"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals((Integer)10, user.getPersonalInformation().getBirthDay().getMonth());

        // Test 3.3: birthDay ("În DAY")
        informationService.identifyAndSetInformation(PersonalInformation.class, "birthDay.day", new Message("în 24"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals((Integer)24, user.getPersonalInformation().getBirthDay().getDay());
    }

    @Test
    public void testPersonalInformation_Address() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "În ce oraș locuiești ?" */
        // TEST 1: "În NAME"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("în", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("homeAddress.locality");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: homeAddress.locality ("În NAME")
        informationService.identifyAndSetInformation(PersonalInformation.class, "homeAddress.locality", new Message("În Cluj"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Cluj", user.getPersonalInformation().getHomeAddress().getLocality());

        // Test 1.2: homeAddress.locality ("În NAME-NAME")
        informationService.identifyAndSetInformation(PersonalInformation.class, "homeAddress.locality", new Message("În Cluj-Napoca"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals("Cluj-Napoca", user.getPersonalInformation().getHomeAddress().getLocality());

        /* DIRECTIVE: "Locuiești la țară sau la oraș ?" */
        // TEST 1: "Locuiesc la țară"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("la", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, LOCALITY_TYPE));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("homeAddress.localityType");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);

        // Test 1.1: homeAddress.localityType ("la NAME")
        informationService.identifyAndSetInformation(PersonalInformation.class, "homeAddress.localityType", new Message("Locuiesc la țară"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals(LocalityType.RURAL, user.getPersonalInformation().getHomeAddress().getLocalityType());
    }

    @Test
    public void testRelationshipInformation_MotherPersonalInformation_PersonalInformation_Name() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Cum o cheamă pe mama ta ?" */
        // TEST 1: "NAME"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(RelationshipsInformation.class);
        linguisticExpression.setInformationFieldNamePath("motherPersonalInformation.firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: motherPersonalInformation.firstName ("NAME")
        informationService.identifyAndSetInformation(RelationshipsInformation.class, "motherPersonalInformation.firstName", new Message("Maria"), user);
        Assert.assertNotNull(user.getRelationshipsInformation());
        Assert.assertEquals("Maria", user.getRelationshipsInformation().getMotherPersonalInformation().getFirstName());

        // TEST 2: "NAME e numele ei."
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        expressionItems.add(new ExpressionItem("e", NOT_AN_INFORMATION));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(RelationshipsInformation.class);
        linguisticExpression.setInformationFieldNamePath("motherPersonalInformation.firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: motherPersonalInformation.firstName ("NAME e numele ei.")
        informationService.identifyAndSetInformation(RelationshipsInformation.class, "motherPersonalInformation.firstName", new Message("Maria e numele ei."), user);
        Assert.assertNotNull(user.getRelationshipsInformation());
        Assert.assertEquals("Maria", user.getRelationshipsInformation().getMotherPersonalInformation().getFirstName());
    }

    @Test
    public void testDetectPersonalInformation_Gender() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Ești băiat sau fată ?" */
        // TEST 1: "sunt NAME"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("sunt", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, GENDER));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(PersonalInformation.class);
        linguisticExpression.setInformationFieldNamePath("gender");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: firstName ("sunt GENDER")
        informationService.identifyAndSetInformation(PersonalInformation.class, "gender", new Message("sunt băiat"), user);
        Assert.assertNotNull(user.getPersonalInformation());
        Assert.assertEquals(Gender.MALE, user.getPersonalInformation().getGender());
    }

    @Test
    public void testDetectSchoolInformation_IsAtSchool() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Ești la școală ?" */
        // TEST 1: "BOOLEAN"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, BOOLEAN));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(SchoolInformation.class);
        linguisticExpression.setInformationFieldNamePath("isAtSchool");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: favouriteCourse ("NAME")
        informationService.identifyAndSetInformation(SchoolInformation.class, "isAtSchool", new Message("Da"), user);
        Assert.assertNotNull(user.getSchoolInformation());
        Assert.assertEquals(true, user.getSchoolInformation().getIsAtSchool());
        // the course should appear in coursesGrades map
        Assert.assertEquals(true, user.getSchoolInformation().getIsAtSchool());

        /* DIRECTIVE: "Ce notă iei de obicei la limba română ?" */
        // TEST 2: "De obicei am NUMBER."
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("de", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("obicei", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("iau", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NUMBER));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(SchoolInformation.class);
        linguisticExpression.setInformationFieldNamePath("coursesGrades");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: coursesGrades ("De obicei iau 8")
        informationService.identifyAndSetInformation(SchoolInformation.class, "coursesGrades#limba română", new Message("De obicei iau 8"), user);
        Assert.assertNotNull(user.getSchoolInformation());
        Assert.assertEquals((Integer) 8, user.getSchoolInformation().getCoursesGrades().get("limba română"));

        /* DIRECTIVE: "Ce materie ai la școală ?" */
        // TEST 3: "NAME"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(SchoolInformation.class);
        linguisticExpression.setInformationFieldNamePath("coursesGrades");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(3, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: coursesGrades ("De obicei iau 8")
        informationService.identifyAndSetInformation(SchoolInformation.class, "coursesGrades#?", new Message("matematică"), user);
        Assert.assertNotNull(user.getSchoolInformation());
        Assert.assertNotNull(user.getSchoolInformation().getCoursesGrades().get("matematică"));
    }

    @Test
    public void testDetectSchoolInformation_FavouriteCoursesAndGrades() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Care e materia ta preferată ?" */
        // TEST 1: "NAME"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(SchoolInformation.class);
        linguisticExpression.setInformationFieldNamePath("favouriteCourse");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: favouriteCourse ("NAME")
        informationService.identifyAndSetInformation(SchoolInformation.class, "favouriteCourse", new Message("limba română"), user);
        Assert.assertNotNull(user.getSchoolInformation());
        Assert.assertEquals("limba română", user.getSchoolInformation().getFavouriteCourse());
        // the course should appear in coursesGrades map
        Assert.assertNotNull(user.getSchoolInformation().getCoursesGrades().get("limba română"));

        /* DIRECTIVE: "Ce notă iei de obicei la limba română ?" */
        // TEST 2: "De obicei am NUMBER."
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("de", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("obicei", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("iau", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NUMBER));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(SchoolInformation.class);
        linguisticExpression.setInformationFieldNamePath("coursesGrades");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: coursesGrades ("De obicei iau 8")
        informationService.identifyAndSetInformation(SchoolInformation.class, "coursesGrades#limba română", new Message("De obicei iau 8"), user);
        Assert.assertNotNull(user.getSchoolInformation());
        Assert.assertEquals((Integer) 8, user.getSchoolInformation().getCoursesGrades().get("limba română"));

        /* DIRECTIVE: "Ce materie ai la școală ?" */
        // TEST 3: "NAME"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(SchoolInformation.class);
        linguisticExpression.setInformationFieldNamePath("coursesGrades");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(3, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: coursesGrades ("De obicei iau 8")
        informationService.identifyAndSetInformation(SchoolInformation.class, "coursesGrades#?", new Message("matematică"), user);
        Assert.assertNotNull(user.getSchoolInformation());
        Assert.assertNotNull(user.getSchoolInformation().getCoursesGrades().get("matematică"));
    }

    @Test
    public void testRelationshipInformation_BrothersAndSistersPersonalInformation_Name() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Cum îl cheamă pe fratele tău ?" */
        // TEST 1: "Andrei"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(RelationshipsInformation.class);
        linguisticExpression.setInformationFieldNamePath("brothersAndSistersPersonalInformation.firstName");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: brothersAndSistersPersonalInformation.firstName ("Andrei")
        informationService.identifyAndSetInformation(RelationshipsInformation.class, "brothersAndSistersPersonalInformation#?.firstName", new Message("Andrei"), user);
        Assert.assertNotNull(user.getRelationshipsInformation());
        Assert.assertEquals("Andrei", user.getRelationshipsInformation().getBrothersAndSistersPersonalInformation().get("Andrei").getFirstName());
    }

    @Test
    public void testFreeTimeInformation_Hobbies() throws InstantiationException, IllegalAccessException {
        /* DIRECTIVE: "Ce îți place să faci în timpul liber ?" */
        // TEST 1: "Îmi place să joc NAME"
        // add linguistic expression
        LinguisticExpression linguisticExpression = new LinguisticExpression();
        List<ExpressionItem> expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("place", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("să", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("joc", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(FreeTimeInformation.class);
        linguisticExpression.setInformationFieldNamePath("hobbies");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(1, informationService.getAllLinguisticExpressions().size());
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(FreeTimeInformation.class);
        linguisticExpression.setInformationFieldNamePath("hobbies");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(2, informationService.getAllLinguisticExpressions().size());

        // Test 1.1: hobbies ("Îmi place să joc NAME")
        informationService.identifyAndSetInformation(FreeTimeInformation.class, "hobbies#?", new Message("Îmi place să joc fotbal"), user);
        Assert.assertNotNull(user.getFreeTimeInformation());
        Assert.assertEquals(1, user.getFreeTimeInformation().getHobbies().size());
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("fotbal"));

        // Test 1.2: hobbies ("Îmi place să joc NAME, NAME și NAME")
        informationService.identifyAndSetInformation(FreeTimeInformation.class, "hobbies#?", new Message("Îmi place să joc fotbal, tenis, baschet și volei."), user);
        Assert.assertNotNull(user.getFreeTimeInformation());
        Assert.assertEquals(4, user.getFreeTimeInformation().getHobbies().size());
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("fotbal"));
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("tenis"));
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("baschet"));
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("volei"));

        // TEST 2: "Îmi place să joc NAME, să NAME, îmi place să NAME și îmi ador să NAME"
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("place", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("să", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(FreeTimeInformation.class);
        linguisticExpression.setInformationFieldNamePath("hobbies");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(3, informationService.getAllLinguisticExpressions().size());
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("ador", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem("să", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(FreeTimeInformation.class);
        linguisticExpression.setInformationFieldNamePath("hobbies");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(4, informationService.getAllLinguisticExpressions().size());
        // add linguistic expression
        linguisticExpression = new LinguisticExpression();
        expressionItems = new ArrayList<>();
        expressionItems.add(new ExpressionItem("să", NOT_AN_INFORMATION));
        expressionItems.add(new ExpressionItem(null, NAME));
        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setInformationClass(FreeTimeInformation.class);
        linguisticExpression.setInformationFieldNamePath("hobbies");
        linguisticExpression.setSpeechType(SpeechType.STATEMENT);
        informationService.addLinguisticExpression(linguisticExpression);
        Assert.assertEquals(5, informationService.getAllLinguisticExpressions().size());

        // Test 2.1: hobbies ("Îmi place să joc NAME, să NAME, îmi place să NAME și îmi ador să NAME")
        user.setFreeTimeInformation(null);
        informationService.identifyAndSetInformation(FreeTimeInformation.class, "hobbies#?", new Message("Îmi place să joc fotbal, să pescuiesc, îmi place să citesc și ador să fac poze."), user);
        Assert.assertNotNull(user.getFreeTimeInformation());
        Assert.assertEquals(4, user.getFreeTimeInformation().getHobbies().size());
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("fotbal"));
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("pescuiesc"));
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("citesc"));
        Assert.assertTrue(user.getFreeTimeInformation().getHobbies().contains("fac poze"));
    }
}
