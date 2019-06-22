package services.impl;

import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.RejectingExpression;
import domain.entities.SimpleDate;
import domain.entities.User;
import domain.entities.Word;
import domain.enums.Gender;
import domain.enums.ItemClass;
import domain.enums.LocalityType;
import domain.enums.SpeechType;
import domain.information.Information;
import domain.information.PersonalInformation;
import org.springframework.stereotype.Service;
import repositories.ExpressionItemRepository;
import repositories.LinguisticExpressionRepository;
import repositories.PersonalInformationRepository;
import repositories.RejectingExpressionRepository;
import services.api.InformationDetectionService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static domain.enums.ItemClass.NOT_AN_INFORMATION;
import static domain.enums.SpeechType.STATEMENT;
import static services.impl.ChatbotServiceImpl.splitInWords;

@Service
public class InformationDetectionServiceImpl implements InformationDetectionService {
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final ExpressionItemRepository expressionItemRepository;
    private final RejectingExpressionRepository rejectingExpressionRepository;

    public InformationDetectionServiceImpl(LinguisticExpressionRepository linguisticExpressionRepository, ExpressionItemRepository expressionItemRepository, PersonalInformationRepository personalInformationRepository, RejectingExpressionRepository rejectingExpressionRepository) {
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.expressionItemRepository = expressionItemRepository;
        this.rejectingExpressionRepository = rejectingExpressionRepository;
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
    @Transactional
    public List<Object> identifyAndSetInformation(Class informationClass, String informationFieldNamePath, final Message answer, final User user) {
        if (informationClass == null) {
            informationClass = identifyInformationClass(answer);
            informationFieldNamePath = answer.getEquivalentSentence().getInformationFieldNamePath();

            if (informationClass == null || informationFieldNamePath == null) {
                return null;
            }
        }
        final List<LinguisticExpression> expressions = getLinguisticExpressionsByClassAndFieldAndSpeechType(informationClass, removeMapKeysFromPath(informationFieldNamePath), STATEMENT);
        ItemClass itemClass = null;

        final List<Object> identifiedInformation = new ArrayList<>();

        // split the message by comma to delimit ideas
        final String[] subsentences = splitInSubentences(answer.getText());
        for (String subsentence : subsentences) {
            final String[] answerWords = splitInWords(subsentence);
            LinguisticExpression matchedLinguisticExpression;
            for (LinguisticExpression expression : expressions) {
                matchedLinguisticExpression = expression;
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

                    // Exceptional case for expression.size==1: user did not really responded
                    if (isNotInformation(informationWords)) {
                        continue; // skip to next subsentence
                    }

                    try {
                        final Object informationAsItsType = convertTextToInformation(informationWords, itemClass, matchedLinguisticExpression);
                        if (informationAsItsType == null) {
                            continue;
                        }

                        final Method getterOfUser = user.getClass().getMethod("get" + informationClass.getSimpleName());
                        Information userInformation = (Information) getterOfUser.invoke(user);
                        if (userInformation == null) {
                            userInformation = (Information) informationClass.newInstance();
                            final Method setterOfUser = user.getClass().getMethod("set" + informationClass.getSimpleName(), informationClass);
                            setterOfUser.invoke(user, userInformation);
                        }
                        // check if the effective information is a field of a field of the information
                        final String[] fieldNameHierarchy = informationFieldNamePath.split("\\.");
                        setTheInformation(userInformation, fieldNameHierarchy, informationAsItsType);
                        identifiedInformation.add(informationAsItsType);
                        break; // we found the information, skip the next linguistic expressions and go to next subsentence

                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (identifiedInformation.isEmpty()) {
            return null;
        }
        return identifiedInformation;
    }

    /**
     * @return true if the given subsentence contains expressions like "nu-ți spun", "nu știu" etc.
     */
    private boolean isNotInformation(final String[] words) {
        final StringBuilder wordsAsExpression_SB = new StringBuilder();
        for (String word : words) {
            wordsAsExpression_SB.append(word).append(" ");
        }
        final String wordsAsExpression = Word.replaceDiacritics(wordsAsExpression_SB.toString()).toLowerCase();

        final List<RejectingExpression> rejectingExpressions = rejectingExpressionRepository.findAll();
        for (RejectingExpression rejectingExpression : rejectingExpressions) {
            final String[] expressionWords = rejectingExpression.getText().split(" ");
            boolean textContainsAllExpressionWords = true;
            for (String expressionWord : expressionWords) {
                if (!wordsAsExpression.contains(Word.replaceDiacritics(expressionWord))) {
                    textContainsAllExpressionWords = false;
                    break;
                }
            }
            if (textContainsAllExpressionWords) {
                return true;
            }
        }
        return false;
    }

    private String[] splitInSubentences(final String text) {
        final String[] split = text.split("([.,]+)| și ");
        final List<String> subsentences = new ArrayList<>();
        // don't split dates by dot
        for (int i = 0; i < split.length; i++) {
            String subsentence = split[i];
            final String[] subsentenceWords = subsentence.split(" ");
            final String lastSubsentenceWord = subsentenceWords[subsentenceWords.length - 1];

            boolean isNumber_S1 = false;
            boolean isNumber_S2 = false;
            boolean isNumber_S3 = false;
            try { Integer.parseInt(lastSubsentenceWord); isNumber_S1 = true; }
            catch (NumberFormatException ignored) {}
            try { Integer.parseInt(split[i + 1]); isNumber_S2 = true; }
            catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {}
            try {
                final String nextNextFirstSubsentenceWord = split[i+2].split(" ")[0];
                Integer.parseInt(nextNextFirstSubsentenceWord); isNumber_S3 = true;
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {}

            if (isNumber_S1 && isNumber_S2) {
                subsentence += "." + split[i + 1];
                i++;
            }
            if (isNumber_S1 && isNumber_S2 && isNumber_S3) {
                subsentence += "." + split[i + 1];
                i++;
            }
            subsentences.add(subsentence);
        }
        return subsentences.toArray(new String[0]);
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
     * If the value already exists, replace it. If the value needs to be saved in a collection, add it keeping the collection free of duplicates.
     *
     * @param parent               - original parent, it will be a child if the hierarchy impose (length > 1)
     * @param fieldNameHierarchy   - the parent is at index 0, the child is index 1 etc.
     *                             The hierarchy may contain maps. In this case, the field name element should contain the key of the element you want to update separate with #.
     *                             If after # follows ? it means that the field is a map and you want to add the information in map.
     *                             Example for PersonalInformation: "[{grades#math}]"
     *                             Example for RelationshipInformation: "[{kidsPersonalInformation#Matei},{firstName}]", "[{brothersAndSistersPersonalInformation#?}]"
     * @param informationAsItsType - the effective information which should be set for last child in hierarchy
     */
    static private void setTheInformation(final Object parent, final String[] fieldNameHierarchy, final Object informationAsItsType) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
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

                // auxiliary code: update relevant fields to the changed one // TODO task "PathAndKeys"
//                if (fieldName.equals("favouriteCourse")) {
//                    final Method getterOfCoursesGradesMap = parent.getClass().getMethod("getCoursesGrades");
//                    final Map<String, Integer> coursesGrades = (Map<String, Integer>) getterOfCoursesGradesMap.invoke(parent);
//                    coursesGrades.put(String.valueOf(informationAsItsType), 0);
//                }
            } else {
                // the field is a collection (map or list)
                if (getterOfChild.getReturnType().equals(Map.class)) {
                    final Map map = (Map) getterOfChild.invoke(parent);
                    if (fieldKey.equals("?")) {
                        if (fieldName.equals("coursesGrades")) {
                            // map.put(informationAsItsType, 0); // TODO task "PathAndKeys"
                        } else { // number of members from a map from RelationshipInformation
                            final PersonalInformation personalInformation = new PersonalInformation();
                            // personalInformationRepository.save(personalInformation);
                            map.put(informationAsItsType, personalInformation);
                        }
                    } else {
                        map.put(fieldKey, informationAsItsType);
                    }
                }
                if (getterOfChild.getReturnType().equals(List.class)) {
                    final List list = (List) getterOfChild.invoke(parent);
                    if (!list.contains(informationAsItsType)) {
                        list.add(informationAsItsType);
                    }
                }
            }
        } else {
            // CHILD INSIDE HIERARCHY, NOT THE LAST ONE
            if (fieldKey == null) {
                // normal field or SimpleDate, no map
                Object child = getterOfChild.invoke(parent);
                if (child == null) {
                    final Class childClass = getterOfChild.getReturnType();
                    child = childClass.newInstance();
                    final Method setterOfChild = parent.getClass().getMethod("set" + fieldName_firstLetterCapitalize, childClass);
                    setterOfChild.invoke(parent, child);
                }
                if (child instanceof SimpleDate) {
                    // consider fieldNameHierarchy.length == 2
                    final String dateFieldName_firstLetterCapitalize = fieldNameHierarchy[1].substring(0, 1).toUpperCase() + fieldNameHierarchy[1].substring(1);
                    final Method setterOfDate = child.getClass().getMethod("set" + dateFieldName_firstLetterCapitalize, Integer.class);
                    final Integer value = ((SimpleDate)informationAsItsType).getMonth();
                    setterOfDate.invoke(child, value);
                    return;
                }

                final String[] fieldNameHierarchyChild = new String[fieldNameHierarchy.length - 1];
                System.arraycopy(fieldNameHierarchy, 1, fieldNameHierarchyChild, 0, fieldNameHierarchy.length - 1);
                setTheInformation(child, fieldNameHierarchyChild, informationAsItsType);
            } else {
                // the field is a map
                final Map map = (Map) getterOfChild.invoke(parent);
                Object value = map.get(fieldKey);
                if (value == null) {
                    value = new PersonalInformation(); // TODO: use more than PersonalInformation
                    // personalInformationRepository.save((PersonalInformation) value);
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
     *                         if DATE: return {day: informationWords[0], month: informationWords[1], year: informationWords[2]}, where day and year are optional
     *                         if STRING: return informationWords[0] + informationWords[1] + ... + informationWords[informationWords.length]
     * @param itemClass        is the class which the effective information should be
     *                         if NAME: return String
     *                         if NUMBER: return integer
     *                         if BOOLEAN: return boolean
     *                         if DATE: return SimpleDate
     *                         if GENDER: return Gender
     *                         if LOCALITY_TYPE: return AddressType
     *                         else: return String
     * @param linguisticExpression the one that identified the information
     * @return the information converted the corresponding Java class or <null> if it cannot be converted
     */
    private Object convertTextToInformation(final String[] informationWords, final ItemClass itemClass, final LinguisticExpression linguisticExpression) {
        switch (itemClass) {
            case NUMBER: {
                // floor
                if (informationWords[0].toLowerCase().equals("parter")) return 0;
                if (informationWords[0].toLowerCase().equals("subsol")) return -1;
                if (informationWords[0].toLowerCase().equals("ultimul")) return 10;
                // school class
                if (informationWords[0].toLowerCase().startsWith("preg")) return 0;
                if (!Character.isDigit(informationWords[0].charAt(informationWords[0].length()-1))) informationWords[0] = informationWords[0].substring(0, informationWords[0].length()-1);
                try {
                    return Integer.valueOf(informationWords[0]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }

            case BOOLEAN: {
                final StringBuilder informationString_SB = new StringBuilder();
                for (String word : informationWords) {
                    informationString_SB.append(word).append(" ");
                }
                final String informationString = Word.replaceDiacritics(informationString_SB.toString()).toLowerCase();
                if ((informationString.contains("da") && informationString.contains("de unde")) || informationString.contains("mai putin")) {
                    return false;
                }
                if (informationString.contains("nu") || informationString.contains("neg") || informationString.contains("nici vorba") ||
                        informationString.contains("niciodata") || informationString.contains("never") || informationString.contains("nope")) {
                    return false;
                }
                if (informationString.contains("da") || informationString.contains("sigur") || informationString.contains("afirm") || informationString.contains("categoric") ||
                        informationString.contains("absolut") || informationString.contains("normal") || (informationString.contains("evident") && !informationString.contains("nu")) ||
                        informationString.contains("desigur") || informationString.contains("putin") || informationString.contains("cateodata") ||
                        (informationString.contains("ca") && informationString.contains("de") && informationString.contains("cat")) || informationString.contains("probabil")) {
                    return true;
                }
                return null;
            }

            case DATE: {
                try {
                    // ieri / azi / mâine / poimâine
                    final String informationWord = Word.replaceDiacritics(informationWords[0].toLowerCase());
                    if (informationWord.equals("azi") || informationWord.startsWith("ast")) {
                        final LocalDate today = LocalDate.now();
                        return new SimpleDate(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
                    }
                    if (informationWord.equals("ieri")) {
                        final LocalDate yesterday = LocalDate.now().minusDays(1);
                        return new SimpleDate(yesterday.getYear(), yesterday.getMonthValue(), yesterday.getDayOfMonth());
                    }
                    if (Word.replaceDiacritics(informationWords[0]).toLowerCase().equals("maine")) {
                        final LocalDate tomorrow = LocalDate.now().plusDays(1);
                        return new SimpleDate(tomorrow.getYear(), tomorrow.getMonthValue(), tomorrow.getDayOfMonth());
                    }
                    if (Word.replaceDiacritics(informationWords[0]).toLowerCase().equals("poimâine")) {
                        final LocalDate tomorrow = LocalDate.now().plusDays(2);
                        return new SimpleDate(tomorrow.getYear(), tomorrow.getMonthValue(), tomorrow.getDayOfMonth());
                    }
                    // acum X ani/luni/zile
                    if (linguisticExpression.getItems().stream().anyMatch(item -> {if (item.getText() == null) return false; return item.getText().equals("ani"); })
                            || (informationWords.length >= 2 && informationWords[1].toLowerCase().equals("ani")) || (informationWords.length >= 3 && informationWords[2].toLowerCase().equals("ani"))) {
                        final int yearsToSubtract = Integer.valueOf(informationWords[0]);
                        final LocalDate pastTime = LocalDate.now().minusYears(yearsToSubtract);
                        return new SimpleDate(pastTime.getYear(), null, null);
                    }
                    if (linguisticExpression.getItems().stream().anyMatch(item -> {if (item.getText() == null) return false; return item.getText().equals("luni"); })
                            || (informationWords.length >= 2 && informationWords[1].toLowerCase().equals("luni")) || (informationWords.length >= 3 && informationWords[2].toLowerCase().equals("luni"))) {
                        final int monthsToSubtract = Integer.valueOf(informationWords[0]);
                        final LocalDate pastTime = LocalDate.now().minusMonths(monthsToSubtract);
                        return new SimpleDate(pastTime.getYear(), pastTime.getMonthValue(), null);
                    }
                    if (linguisticExpression.getItems().stream().anyMatch(item -> {if (item.getText() == null) return false; return item.getText().equals("zile"); })
                            || (informationWords.length >= 2 && informationWords[1].toLowerCase().equals("zile")) || (informationWords.length >= 3 && informationWords[2].toLowerCase().equals("luni"))) {
                        final int daysToSubtract = Integer.valueOf(informationWords[0]);
                        final LocalDate pastTime = LocalDate.now().minusDays(daysToSubtract);
                        return new SimpleDate(pastTime.getYear(), pastTime.getMonthValue(), pastTime.getDayOfMonth());
                    }

                    // remove dots from date
                    final List<String> informationWordsNoDots = new ArrayList<>();
                    for (String word : informationWords) {
                        if (!word.equals(".")) {
                            informationWordsNoDots.add(word);
                        }
                    }
                    Integer day = null;
                    Integer month = null;
                    Integer year = null;
                    if (informationWordsNoDots.size() == 1) { // just month
                        month = stringToMonth(informationWordsNoDots.get(0));
                    }
                    if (informationWordsNoDots.size() == 2) { // day + month
                        day = Integer.valueOf(informationWordsNoDots.get(0));
                        month = stringToMonth(informationWordsNoDots.get(1));
                    }
                    if (informationWordsNoDots.size() >= 3) { // day + month + year
                        day = Integer.valueOf(informationWordsNoDots.get(0));
                        month = stringToMonth(informationWordsNoDots.get(1));
                        year = Integer.valueOf(informationWordsNoDots.get(2));
                    }
                    if (day == null && month == null && year == null) {
                        return null;
                    }
                    return new SimpleDate(year, month, day);
                }
                catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            }

            case GENDER: {
                for (String informationWord : informationWords) {
                    informationWord = Word.replaceDiacritics(informationWord.toLowerCase());
                    if (informationWord.startsWith("b") || informationWord.startsWith("mas") || informationWord.contains("domn") || informationWord.contains("domnisor")) {
                        return Gender.MALE;
                    }
                    if (informationWord.startsWith("f") || informationWord.contains("doamna") || informationWord.contains("domnisoara")) {
                        return Gender.FEMALE;
                    }
                }
                return null;
            }

            case LOCALITY_TYPE: {
                final String informationWord = Word.replaceDiacritics(informationWords[0].toLowerCase());
                if (informationWord.startsWith("ța") || informationWord.startsWith("ta") || informationWord.startsWith("sat") || informationWord.startsWith("ru")) { // țară || sat || rural
                    return LocalityType.RURAL;
                }
                if (informationWord.startsWith("or") || informationWord.startsWith("ur")) { // oraș || urban
                    return LocalityType.URBAN;
                }
                return null;
            }

            default: { // NAME or anything else
                final StringBuilder name = new StringBuilder();
                for (int i = 0; i < informationWords.length; i++) {
                    final String word = informationWords[i];

                    name.append(word);
                    if (i < informationWords.length - 1 && Character.isLetterOrDigit(informationWords[i + 1].charAt(0))) {
                        name.append(" ");
                    }
                }
                return name.toString();
            }
        }
    }

    private Integer stringToMonth(final String word) {
        try {
            return Integer.parseInt(word);
        } catch (NumberFormatException e) {
            if (word.toLowerCase().startsWith("ian")) return 1; // TODO: TAKE MONTHS FROM DB
            if (word.toLowerCase().startsWith("feb")) return 2;
            if (word.toLowerCase().startsWith("mar")) return 3;
            if (word.toLowerCase().startsWith("apr")) return 4;
            if (word.toLowerCase().startsWith("mai")) return 5;
            if (word.toLowerCase().startsWith("iun")) return 6;
            if (word.toLowerCase().startsWith("iul")) return 7;
            if (word.toLowerCase().startsWith("aug")) return 8;
            if (word.toLowerCase().startsWith("sep")) return 9;
            if (word.toLowerCase().startsWith("oct")) return 10;
            if (word.toLowerCase().startsWith("no")) return 11;
            if (word.toLowerCase().startsWith("dec")) return 12;
        }
        return null;
    }

    /**
     * @return all linguistic expressions with the given properties, SORTED DESCENDING by the number of items (expression items)
     */
    private List<LinguisticExpression> getLinguisticExpressionsByClassAndFieldAndSpeechType(final Class<Information> informationClass,
                                                                                            final String informationField,
                                                                                            final SpeechType speechType) {
        return linguisticExpressionRepository.findAllByInformationClassAndInformationFieldNamePathAndSpeechType(informationClass, informationField, speechType)
                .stream()
                .sorted((expression1, expression2) -> Integer.compare(expression2.getItems().size(), expression1.getItems().size()))
                .collect(Collectors.toList());
    }

//    /**
//     * @param previousMessage is a directive, statement or acknowledgement (ex: „Care e numele tău ?” or „Spune-mi numele tău !”, „Eu sunt Andy.”, „Salut !”).
//     *                        It must have set the fields: informationClassDto and informationFieldNamePath. (ex: PersonalInformation and FirstName)
//     *                        It may be null. If it's null, we try to detect automatically what type of information is in answer.
//     * @param answer          is a statement
//     * @return a PersonalInformation object if we find at least 1 personal information; otherwise return <null>
//     */
//    private PersonalInformation identifyPersonalInformation(final Message previousMessage, final Message answer) {
//        if (previousMessage != null) {
//            final Class<Information> informationClassDto = previousMessage.getEquivalentSentence().getInformationClassDto();
//            final Field informationFieldNamePath = answer.getEquivalentSentence().getInformationFieldNamePath();
//            final List<LinguisticExpression> expressions = getLinguisticExpressionsByClassAndFieldAndSpeechType(informationClassDto, informationFieldNamePath, STATEMENT);
//
//        }
//    }
}
