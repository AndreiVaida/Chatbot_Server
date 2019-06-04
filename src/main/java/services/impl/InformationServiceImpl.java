package services.impl;

import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.enums.ItemClass;
import domain.enums.SpeechType;
import domain.information.Information;
import domain.information.PersonalInformation;
import org.springframework.stereotype.Service;
import repositories.ExpressionItemRepository;
import repositories.LinguisticExpressionRepository;
import services.api.InformationService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static domain.enums.ItemClass.NOT_AN_INFORMATION;
import static domain.enums.SpeechType.STATEMENT;
import static services.impl.ChatbotServiceImpl.wordsSplitRegex;

@Service
public class InformationServiceImpl implements InformationService {
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final ExpressionItemRepository expressionItemRepository;

    public InformationServiceImpl(LinguisticExpressionRepository linguisticExpressionRepository, ExpressionItemRepository expressionItemRepository) {
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.expressionItemRepository = expressionItemRepository;
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
    public Information identifyInformation(Class informationClass, String informationFieldName, final Message answer) {
        if (informationClass == null) {
            informationClass = identifyInformationClass(answer);
            informationFieldName = answer.getEquivalentSentence().getInformationFieldName();
        }
        final List<LinguisticExpression> expressions = getLinguisticExpressionsByClassAndFieldAndSpeechType(informationClass, informationFieldName, STATEMENT);
        ItemClass itemClass = null;

        final String[] answerWords = answer.getText().split(wordsSplitRegex);
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
                    iInformationEnd = iExpression;
                    if (iExpression == expression.getItems().size()) {
                        break;
                    }
                    expressionWord = expression.getItems().get(iExpression).getText();
                    if (expressionWord != null) {
                        expressionWord = expressionWord.toLowerCase();
                    }
                    continue;
                }

                // if the current item of the expression is the information to find, skip (at least) 1 word of the answer AND the expression item
                if (!expression.getItems().get(iExpression).getItemClass().equals(NOT_AN_INFORMATION)) {
                    itemClass = expression.getItems().get(iExpression).getItemClass(); // this should be set just once
                    matchingTheInformation = true;
                    matchingWords = false;
                    iInformationBegin = iAnswer;
                    iExpression++;
                    if (iExpression == expression.getItems().size()) {
                        iInformationEnd = iExpression;
                        break;
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
                if (answerWord.equals(expressionWord)) {
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

            // check if we matched all items of the expression
            if (iExpression == expression.getItems().size()) {
                String[] informationWords = new String[iInformationEnd - iInformationBegin];
                for (int i = iInformationBegin; i < iInformationEnd; i++) {
                    informationWords[i - iInformationBegin] = answerWords[i];
                }
                try {
                    final Information information = (Information) informationClass.newInstance();
                    final Method setterOfInformation = information.getClass().getMethod("set" + informationFieldName.substring(0,1).toUpperCase() + informationFieldName.substring(1), String.class);
                    setterOfInformation.invoke(information, convertTextToInformation(informationWords, itemClass));
                    return information;

                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private Class<Information> identifyInformationClass(final Message message) {
        return null;
    }

    /**
     * @param informationWords the information as string
     *                         if NUMBER: return informationWords[0]
     *                         if DATE: return {day: informationWords[0], month: informationWords[1], year: informationWords[2]}, where year is optional
     *                         if STRING: return informationWords[0] + informationWords[1] + ... + informationWords[informationWords.length]
     * @param itemClass - the class which the information should be
     * @return the information converted the corresponding Java class
     */
    private Object convertTextToInformation(final String[] informationWords, final ItemClass itemClass) {
        switch (itemClass) {
            case NUMBER: return Integer.valueOf(informationWords[0]);
            case DATE: {
                int day = Integer.valueOf(informationWords[0]);
                int month = 1; // TODO: they should not be 1 as default
                int year = 1;
                try {
                    month = Integer.parseInt(informationWords[1]);
                } catch(NumberFormatException e){
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
            default: {
                final StringBuilder name = new StringBuilder();
                for (String word : informationWords) {
                    name.append(word);
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
