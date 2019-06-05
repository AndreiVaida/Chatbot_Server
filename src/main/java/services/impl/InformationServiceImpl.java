package services.impl;

import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.enums.Gender;
import domain.enums.ItemClass;
import domain.enums.SpeechType;
import domain.information.Information;
import domain.information.PersonalInformation;
import org.springframework.stereotype.Service;
import repositories.ExpressionItemRepository;
import repositories.LinguisticExpressionRepository;
import repositories.PersonalInformationRepository;
import services.api.InformationService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static domain.enums.ItemClass.NOT_AN_INFORMATION;
import static domain.enums.SpeechType.STATEMENT;
import static services.impl.ChatbotServiceImpl.splitInWords;

@Service
public class InformationServiceImpl implements InformationService {
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final ExpressionItemRepository expressionItemRepository;
    private final PersonalInformationRepository personalInformationRepository;

    public InformationServiceImpl(LinguisticExpressionRepository linguisticExpressionRepository, ExpressionItemRepository expressionItemRepository, PersonalInformationRepository personalInformationRepository) {
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.expressionItemRepository = expressionItemRepository;
        this.personalInformationRepository = personalInformationRepository;
    }

    @Override
    @Transactional
    public LinguisticExpression addLinguisticExpression(final LinguisticExpression linguisticExpression) {
        for (ExpressionItem expressionItem : linguisticExpression.getItems()) {
            expressionItemRepository.save(expressionItem);
        }

        return linguisticExpressionRepository.save(linguisticExpression);
    }

    @Override
    @Transactional
    public List<LinguisticExpression> getAllLinguisticExpressions() {
        return linguisticExpressionRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteLinguisticExpression(final Long expressionId) {
        if (!linguisticExpressionRepository.existsById(expressionId)) {
            throw new EntityNotFoundException("Linguistic Expression not found.");
        }
        linguisticExpressionRepository.deleteById(expressionId);
    }

    @Override
    public Information identifyInformation(Class informationClass, String informationFieldNamePath, final Message answer) {
        if (informationClass == null) {
            informationClass = identifyInformationClass(answer);
            informationFieldNamePath = answer.getEquivalentSentence().getInformationFieldName();
        }
        final List<LinguisticExpression> expressions = getLinguisticExpressionsByClassAndFieldAndSpeechType(informationClass, removeMapKeysFromPath(informationFieldNamePath), STATEMENT);
        ItemClass itemClass = null;

        final String[] answerWords = splitInWords(answer.getText());
        for (LinguisticExpression expression : expressions) {
            // search the beginning of the expression in the answer
            int iAnswer = 0;
            int iInformationBegin = 0; // the index from the answer where the information starts
            int iInformationEnd = -1; // the index from the answer where the information ends
            int iExpression = 0;
            String expressionWord = expression.getItems().get(0).getText();
            if (expressionWord != null) {
                expressionWord = expressionWord.toLowerCase();
            }

            boolean matchingWords = false;
            boolean matchingTheInformation = false; // the information consist of >=1 word
            for (; iAnswer < answerWords.length; iAnswer++) {
                final String answerWord = answerWords[iAnswer].toLowerCase();

                // if we are matching the information, check for end of information (for first item after information in expression)
                if (matchingTheInformation && answerWord.equals(expressionWord)) {
                    matchingWords = true;
                    matchingTheInformation = false;
                    iExpression++;
                    iInformationEnd = iAnswer;
                    if (iExpression == expression.getItems().size()) {
                        break;
                    }
                    expressionWord = expression.getItems().get(iExpression).getText();
                    if (expressionWord != null) { // it will not be null
                        expressionWord = expressionWord.toLowerCase();
                    }
                    continue;
                }

                if (matchingTheInformation) {
                    iInformationEnd = iAnswer + 1;
                    continue;
                }

                // if the current item of the expression is the information to find, skip (at least) 1 word of the answer AND the expression item
                if (!expression.getItems().get(iExpression).getItemClass().equals(NOT_AN_INFORMATION)) {
                    itemClass = expression.getItems().get(iExpression).getItemClass(); // this should be set just once (itemClass should be final)
                    matchingTheInformation = true;
                    matchingWords = false;
                    iInformationBegin = iAnswer;
                    iExpression++;
                    if (iExpression == expression.getItems().size()) {
                        iInformationEnd = iAnswer + 1;
                        continue;
                    }
                    expressionWord = expression.getItems().get(iExpression).getText();
                    if (expressionWord != null) {
                        expressionWord = expressionWord.toLowerCase();
                    }
                    continue;
                }

                // check if the answerWord == expressionWord
                if (matchingWords && !answerWord.equals(expressionWord)) {
                    // reset the search
                    iExpression = 0;
                    expressionWord = expression.getItems().get(iExpression).getText();
                    if (expressionWord != null) {
                        expressionWord = expressionWord.toLowerCase();
                    }
                    matchingWords = false;
                    continue;
                }
                if (answerWord.equals(expressionWord)) { // && !matchingTheInformation
                    matchingWords = true;
                    iExpression++;
                    if (iExpression == expression.getItems().size()) {
                        break;
                    }
                    expressionWord = expression.getItems().get(iExpression).getText();
                    if (expressionWord != null) {
                        expressionWord = expressionWord.toLowerCase();
                    }
                    continue;
                }
            }

            // if we matched all the items of the expression => we found the information
            if (iExpression == expression.getItems().size()) {
                String[] informationWords = new String[iInformationEnd - iInformationBegin];
                for (int i = iInformationBegin; i < iInformationEnd; i++) {
                    informationWords[i - iInformationBegin] = answerWords[i];
                }
                try {
                    final Information information = (Information) informationClass.newInstance();
                    final Object informationAsItsType = convertTextToInformation(informationWords, itemClass);
                    if (informationAsItsType == null) {
                        return null;
                    }

                    // check if the effective information is a field of a field of the information
                    final String[] fieldNameHierarchy = informationFieldNamePath.split("\\.");
                    setTheInformation(information, fieldNameHierarchy, informationAsItsType);
                    return information;

                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private String removeMapKeysFromPath(final String informationFieldNamePath) {
        final String[] pathList = informationFieldNamePath.split("\\.");
        final StringBuilder newInformationFieldNamePath = new StringBuilder();
        for (int i = 0; i < pathList.length; i++) {
            String[] path = pathList[i].split("#");
            if (i > 0) {
                newInformationFieldNamePath.append(".");
            }
            newInformationFieldNamePath.append(path[0]);
        }
        return newInformationFieldNamePath.toString();
    }

    /**
     * Sets the informationAsItsType in the last child of the hierarchy. Recursive function.
     * If any of the fields (children) are null, create a new object for it.
     * @param parent - original parent, it will be a child if the hierarchy impose (length > 1)
     * @param fieldNameHierarchy - the parent is at index 0, the child is index 1 etc.
     *                           The hierarchy may contain maps. In this case, the field name element should contain the key of the element you want to update separate with #.
     *                           If after # follows ? it means that the field is a map and you want to add the information in map.
     *                           Example for PersonalInformation: "[{grades#math}]"
     *                           Example for RelationshipInformation: "[{kidsPersonalInformation#Matei},{firstName}]", "[{brothersAndSistersPersonalInformation#?}]"
     * @param informationAsItsType - the effective information which should be set for last child in hierarchy
     */
    private void setTheInformation(final Object parent, final String[] fieldNameHierarchy, final Object informationAsItsType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        final String[] field = fieldNameHierarchy[0].split("#"); // get an array of 1 or 2 elements: fieldName and map key (if the field is a map)
        final String fieldName = field[0];
        String fieldKey = null;
        if (field.length >= 2) {
            fieldKey = field[1];
        }
        final String fieldName_firstLetterCapitalize = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        final Method getterOfChild = parent.getClass().getMethod("get" + fieldName_firstLetterCapitalize);

        if (fieldNameHierarchy.length == 1) {
            // LAST CHILD IN HIERARCHY
            if (fieldKey == null) {
                // normal field, no map
                final Class fieldClass = getterOfChild.getReturnType();
                final Method setterOfInformation = parent.getClass().getMethod("set" + fieldName_firstLetterCapitalize, fieldClass);
                setterOfInformation.invoke(parent, informationAsItsType);

                // auxiliary code: update relevant fields to the changed one
                if (fieldName.equals("favouriteCourse")) {
                    final Method getterOfCoursesGradesMap = parent.getClass().getMethod("getCoursesGrades");
                    final Map<String,Integer> coursesGrades = (Map<String, Integer>) getterOfCoursesGradesMap.invoke(parent);
                    coursesGrades.put(String.valueOf(informationAsItsType), 10);
                }
            }
            else {
                // the field is a map
                final Map map = (Map) getterOfChild.invoke(parent);
                if (fieldKey.equals("?")) {
                    if (fieldName.equals("coursesGrades")) {
                        map.put(informationAsItsType, 10);
                    }
                    else { // number of members from a map from RelationshipInformation
                        final PersonalInformation personalInformation = new PersonalInformation();
                        personalInformationRepository.save(personalInformation);
                        map.put(informationAsItsType, personalInformation);
                    }
                }
                else {
                    map.put(fieldKey, informationAsItsType);
                }
            }
        }
        else {
            // CHILD INSIDE HIERARCHY, NOT THE LAST ONE
            if (fieldKey == null) {
                // normal field, no map
                Object child = getterOfChild.invoke(parent);
                if (child == null) {
                    final Class childClass = getterOfChild.getReturnType();
                    child = childClass.newInstance();
                    final Method setterOfChild = parent.getClass().getMethod("set" + fieldName_firstLetterCapitalize, childClass);
                    setterOfChild.invoke(parent, child);
                }

                final String[] fieldNameHierarchyChild = new String[fieldNameHierarchy.length - 1];
                System.arraycopy(fieldNameHierarchy, 1, fieldNameHierarchyChild, 0, fieldNameHierarchy.length - 1);
                setTheInformation(child, fieldNameHierarchyChild, informationAsItsType);
            }
            else {
                // the field is a map
                final Map map = (Map) getterOfChild.invoke(parent);
                Object value = map.get(fieldKey);
                if (value == null) {
                    value = new PersonalInformation(); // TODO: use more than PersonalInformation
                    personalInformationRepository.save((PersonalInformation) value);
                    if (fieldKey.equals("?")) {
                        fieldKey = (String) informationAsItsType;
                    }
                    map.put(fieldKey, value);
                }
                final String[] fieldNameHierarchyChild = new String[fieldNameHierarchy.length - 1];
                System.arraycopy(fieldNameHierarchy, 1, fieldNameHierarchyChild, 0, fieldNameHierarchy.length - 1);
                setTheInformation(value, fieldNameHierarchyChild, informationAsItsType);
            }
        }
    }

    private Class<Information> identifyInformationClass(final Message message) {
        return null;
    }

    /**
     * @param informationWords the information as string
     *                         if NUMBER: return informationWords[0]
     *                         if DATE: return {day: informationWords[0], month: informationWords[1], year: informationWords[2]}, where year is optional
     *                         if STRING: return informationWords[0] + informationWords[1] + ... + informationWords[informationWords.length]
     * @param itemClass        is the class which the effective information should be
     *                         if NAME: return String
     *                         if NUMBER: return Integer
     *                         if DATE: return LocalDate
     *                         if GENDER: return Gender
     *                         else: return String
     * @return the information converted the corresponding Java class or <null> if it cannot be converted
     */
    private Object convertTextToInformation(final String[] informationWords, final ItemClass itemClass) {
        switch (itemClass) {
            case NUMBER:
                return Integer.valueOf(informationWords[0]);

            case DATE: {
                if (informationWords.length < 2) {
                    return null;
                }
                int day = Integer.valueOf(informationWords[0]);
                int month = 1; // TODO: they should not be 1 as default
                int year = 1;
                try {
                    month = Integer.parseInt(informationWords[1]);
                } catch (NumberFormatException e) {
                    if (informationWords[1].toLowerCase().startsWith("ian")) month = 1; // TODO: TAKE MONTHS FROM DB
                    if (informationWords[1].toLowerCase().startsWith("feb")) month = 2;
                    if (informationWords[1].toLowerCase().startsWith("mar")) month = 3;
                    if (informationWords[1].toLowerCase().startsWith("apr")) month = 4;
                    if (informationWords[1].toLowerCase().startsWith("mai")) month = 5;
                    if (informationWords[1].toLowerCase().startsWith("iun")) month = 6;
                    if (informationWords[1].toLowerCase().startsWith("iul")) month = 7;
                    if (informationWords[1].toLowerCase().startsWith("aug")) month = 8;
                    if (informationWords[1].toLowerCase().startsWith("sep")) month = 9;
                    if (informationWords[1].toLowerCase().startsWith("oct")) month = 10;
                    if (informationWords[1].toLowerCase().startsWith("no")) month = 11;
                    if (informationWords[1].toLowerCase().startsWith("dec")) month = 12;
                }
                if (informationWords.length >= 3) {
                    year = Integer.valueOf(informationWords[2]);
                }
                return LocalDate.of(year, month, day);
            }

            case GENDER: {
                if (informationWords[0].toLowerCase().startsWith("b")) {
                    return Gender.MALE;
                }
                return Gender.FEMALE;
            }

            default: { // NAME or anything else
                final StringBuilder name = new StringBuilder();
                for (int i = 0; i < informationWords.length; i++) {
                    final String word = informationWords[i];
                    name.append(word);
                    if (i < informationWords.length - 1) {
                        name.append(" ");
                    }
                }
                return name.toString();
            }
        }
    }

    /**
     * @return all linguistic expressions with the given properties, SORTED DESCENDING by the number of items (expression items)
     */
    private List<LinguisticExpression> getLinguisticExpressionsByClassAndFieldAndSpeechType(final Class<Information> informationClass,
                                                                                            final String informationField,
                                                                                            final SpeechType speechType) {
        return linguisticExpressionRepository.findAllByInformationClassAndInformationFieldNameAndSpeechType(informationClass, informationField, speechType)
                .stream()
                .sorted((expression1, expression2) -> Integer.compare(expression2.getItems().size(), expression1.getItems().size()))
                .collect(Collectors.toList());
    }

//    /**
//     * @param previousMessage is a directive, statement or acknowledgement (ex: „Care e numele tău ?” or „Spune-mi numele tău !”, „Eu sunt Andy.”, „Salut !”).
//     *                        It must have set the fields: informationClass and informationFieldName. (ex: PersonalInformation and FirstName)
//     *                        It may be null. If it's null, we try to detect automatically what type of information is in answer.
//     * @param answer          is a statement
//     * @return a PersonalInformation object if we find at least 1 personal information; otherwise return <null>
//     */
//    private PersonalInformation identifyPersonalInformation(final Message previousMessage, final Message answer) {
//        if (previousMessage != null) {
//            final Class<Information> informationClass = previousMessage.getEquivalentSentence().getInformationClass();
//            final Field informationFieldName = answer.getEquivalentSentence().getInformationFieldName();
//            final List<LinguisticExpression> expressions = getLinguisticExpressionsByClassAndFieldAndSpeechType(informationClass, informationFieldName, STATEMENT);
//
//        }
//    }
}
