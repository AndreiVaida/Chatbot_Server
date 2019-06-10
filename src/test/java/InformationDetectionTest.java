import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.enums.Gender;
import domain.enums.ItemClass;
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

    @Before
    public void initialize() {
        informationService = new InformationServiceImpl(linguisticExpressionRepository, expressionItemRepository, personalInformationRepository);
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
        Information information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Eu sunt Andy"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        PersonalInformation personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

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
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Andy"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

        // Test 3.1: firstName ("NAME.")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Andy."));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Andy", personalInformation.getFirstName());

        // TEST 4: "Cred că eu sunt NAME."
        // Test 3.1: firstName ("Cred că eu sunt NAME.")
        information = informationService.identifyInformation(PersonalInformation.class, "firstName", new Message("Cred că eu sunt Andy."));
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
        Information information = informationService.identifyInformation(PersonalInformation.class, "birthDay", new Message("În octombrie"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        PersonalInformation personalInformation = (PersonalInformation) information;
        Assert.assertEquals((Integer)10, personalInformation.getBirthDay().getMonth());

        // Test 1.2: birthDay ("Pe DAY MONTH")
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay", new Message("Pe 24 octombrie"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals((Integer) 24, personalInformation.getBirthDay().getDay());
        Assert.assertEquals((Integer)10, personalInformation.getBirthDay().getMonth());

        // Test 1.3: birthDay ("Pe DAY MONTH YEAR")
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay", new Message("Pe 24 octombrie 1997"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals((Integer)24, personalInformation.getBirthDay().getDay());
        Assert.assertEquals((Integer)10, personalInformation.getBirthDay().getMonth());
        Assert.assertEquals((Integer)1997, personalInformation.getBirthDay().getYear());

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
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay", new Message("24 octombrie"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals((Integer)24, personalInformation.getBirthDay().getDay());
        Assert.assertEquals((Integer)10, personalInformation.getBirthDay().getMonth());

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
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay.year", new Message("în 1997"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals((Integer)1997, personalInformation.getBirthDay().getYear());

        // Test 3.2: birthDay ("În MONTH")
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay.month", new Message("în octombrie"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals((Integer)10, personalInformation.getBirthDay().getMonth());

        // Test 3.3: birthDay ("În DAY")
        information = informationService.identifyInformation(PersonalInformation.class, "birthDay.day", new Message("în 24"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals((Integer)24, personalInformation.getBirthDay().getDay());
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
        Information information = informationService.identifyInformation(PersonalInformation.class, "homeAddress.locality", new Message("În Cluj"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        PersonalInformation personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Cluj", personalInformation.getHomeAddress().getLocality());

        // Test 1.2: homeAddress.locality ("În NAME-NAME")
        information = informationService.identifyInformation(PersonalInformation.class, "homeAddress.locality", new Message("În Cluj-Napoca"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals("Cluj-Napoca", personalInformation.getHomeAddress().getLocality());

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
        information = informationService.identifyInformation(PersonalInformation.class, "homeAddress.localityType", new Message("Locuiesc la țară"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        personalInformation = (PersonalInformation) information;
        Assert.assertEquals(LocalityType.RURAL, personalInformation.getHomeAddress().getLocalityType());
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
        Information information = informationService.identifyInformation(RelationshipsInformation.class, "motherPersonalInformation.firstName", new Message("Maria"));
        Assert.assertEquals(RelationshipsInformation.class, information.getClass());
        RelationshipsInformation relationshipsInformation = (RelationshipsInformation) information;
        Assert.assertEquals("Maria", relationshipsInformation.getMotherPersonalInformation().getFirstName());

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
        information = informationService.identifyInformation(RelationshipsInformation.class, "motherPersonalInformation.firstName", new Message("Maria e numele ei."));
        Assert.assertEquals(RelationshipsInformation.class, information.getClass());
        relationshipsInformation = (RelationshipsInformation) information;
        Assert.assertEquals("Maria", relationshipsInformation.getMotherPersonalInformation().getFirstName());
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
        Information information = informationService.identifyInformation(PersonalInformation.class, "gender", new Message("sunt băiat"));
        Assert.assertEquals(PersonalInformation.class, information.getClass());
        PersonalInformation personalInformation = (PersonalInformation) information;
        Assert.assertEquals(Gender.MALE, personalInformation.getGender());
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
        Information information = informationService.identifyInformation(SchoolInformation.class, "isAtSchool", new Message("Da"));
        Assert.assertEquals(SchoolInformation.class, information.getClass());
        SchoolInformation schoolInformation = (SchoolInformation) information;
        Assert.assertEquals(true, schoolInformation.getIsAtSchool());
        // the course should appear in coursesGrades map
        Assert.assertEquals(true, schoolInformation.getIsAtSchool());

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
        information = informationService.identifyInformation(SchoolInformation.class, "coursesGrades#limba română", new Message("De obicei iau 8"));
        Assert.assertEquals(SchoolInformation.class, information.getClass());
        schoolInformation = (SchoolInformation) information;
        Assert.assertEquals((Integer) 8, schoolInformation.getCoursesGrades().get("limba română"));

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
        information = informationService.identifyInformation(SchoolInformation.class, "coursesGrades#?", new Message("matematică"));
        Assert.assertEquals(SchoolInformation.class, information.getClass());
        schoolInformation = (SchoolInformation) information;
        Assert.assertNotNull(schoolInformation.getCoursesGrades().get("matematică"));
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
        Information information = informationService.identifyInformation(SchoolInformation.class, "favouriteCourse", new Message("limba română"));
        Assert.assertEquals(SchoolInformation.class, information.getClass());
        SchoolInformation schoolInformation = (SchoolInformation) information;
        Assert.assertEquals("limba română", schoolInformation.getFavouriteCourse());
        // the course should appear in coursesGrades map
        Assert.assertNotNull(schoolInformation.getCoursesGrades().get("limba română"));

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
        information = informationService.identifyInformation(SchoolInformation.class, "coursesGrades#limba română", new Message("De obicei iau 8"));
        Assert.assertEquals(SchoolInformation.class, information.getClass());
        schoolInformation = (SchoolInformation) information;
        Assert.assertEquals((Integer) 8, schoolInformation.getCoursesGrades().get("limba română"));

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
        information = informationService.identifyInformation(SchoolInformation.class, "coursesGrades#?", new Message("matematică"));
        Assert.assertEquals(SchoolInformation.class, information.getClass());
        schoolInformation = (SchoolInformation) information;
        Assert.assertNotNull(schoolInformation.getCoursesGrades().get("matematică"));
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
        Information information = informationService.identifyInformation(RelationshipsInformation.class, "brothersAndSistersPersonalInformation#?.firstName", new Message("Andrei"));
        Assert.assertEquals(RelationshipsInformation.class, information.getClass());
        RelationshipsInformation relationshipsInformation = (RelationshipsInformation) information;
        Assert.assertEquals("Andrei", relationshipsInformation.getBrothersAndSistersPersonalInformation().get("Andrei").getFirstName());
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
        Information information = informationService.identifyInformation(FreeTimeInformation.class, "hobbies#?", new Message("Îmi place să joc fotbal"));
        Assert.assertEquals(FreeTimeInformation.class, information.getClass());
        FreeTimeInformation freeTimeInformation = (FreeTimeInformation) information;
        Assert.assertEquals(1, freeTimeInformation.getHobbies().size());
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("fotbal"));

        // Test 1.2: hobbies ("Îmi place să joc NAME, NAME și NAME")
        information = informationService.identifyInformation(FreeTimeInformation.class, "hobbies#?", new Message("Îmi place să joc fotbal, baschet și volei."));
        Assert.assertEquals(FreeTimeInformation.class, information.getClass());
        freeTimeInformation = (FreeTimeInformation) information;
        Assert.assertEquals(3, freeTimeInformation.getHobbies().size());
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("fotbal"));
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("baschet"));
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("volei"));

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
        information = informationService.identifyInformation(FreeTimeInformation.class, "hobbies#?", new Message("Îmi place să joc fotbal, să pescuiesc, îmi place să citesc și ador să fac poze."));
        Assert.assertEquals(FreeTimeInformation.class, information.getClass());
        freeTimeInformation = (FreeTimeInformation) information;
        Assert.assertEquals(4, freeTimeInformation.getHobbies().size());
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("fotbal"));
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("pescuiesc"));
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("citesc"));
        Assert.assertTrue(freeTimeInformation.getHobbies().contains("fac poze"));
    }
}
