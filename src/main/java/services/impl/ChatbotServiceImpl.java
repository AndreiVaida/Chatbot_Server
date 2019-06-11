package services.impl;

import domain.entities.Address;
import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.SimpleDate;
import domain.entities.User;
import domain.entities.Word;
import domain.enums.LocalityType;
import domain.enums.SpeechType;
import domain.information.FacultyInformation;
import domain.information.FreeTimeInformation;
import domain.information.Information;
import domain.information.PersonalInformation;
import domain.information.RelationshipsInformation;
import domain.information.SchoolInformation;
import org.springframework.stereotype.Service;
import repositories.LinguisticExpressionRepository;
import repositories.SentenceRepository;
import repositories.WordRepository;
import services.api.ChatbotService;

import javax.transaction.Transactional;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static domain.enums.LocalityType.RURAL;
import static domain.enums.LocalityType.URBAN;
import static domain.enums.SpeechType.ACKNOWLEDGEMENT;
import static domain.enums.SpeechType.DIRECTIVE;
import static domain.enums.SpeechType.STATEMENT;

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private static final String wordsSplitRegex = "[\\s]+"; // TODO: change regex with a custom function which consider also the signs as items (, . ...)
    private final SentenceRepository sentenceRepository;
    private final WordRepository wordRepository;
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final Random random;

    public ChatbotServiceImpl(SentenceRepository sentenceRepository, WordRepository wordRepository, LinguisticExpressionRepository linguisticExpressionRepository) {
        this.sentenceRepository = sentenceRepository;
        this.wordRepository = wordRepository;
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        random = new Random();
    }

    static String[] splitInWords(final String string) {
        final String[] words = string.split(wordsSplitRegex);
        final List<String> completeWordList = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            // v1: removing punctuation
//            word = word.replaceAll("[.,;?!]+", "");
//            words[i] = word;

            // v2: keep punctuation as a word
            StringBuilder wordInConstruction = new StringBuilder();
            for (int j = 0; j < word.length(); j++) {
                char character = word.charAt(j);
                // check if the word in construction is a word/number or emoticon/... (3 dots)
                if (wordInConstruction.length() == 0
                        || (wordInConstruction.length() > 0 && (Character.isLetterOrDigit(wordInConstruction.charAt(0)) || wordInConstruction.charAt(0) == '-'))) {
                    // normal word
                    if (Character.isLetterOrDigit(character) || character == '-') {
                        // continue add a letter to normal word
                        wordInConstruction.append(character);
                    } else {
                        // the normal word ended, so save it and start building a word of punctuation marks
                        if (wordInConstruction.length() > 0) {
                            completeWordList.add(wordInConstruction.toString());
                        }
                        wordInConstruction = new StringBuilder();
                        wordInConstruction.append(character);
                    }
                } else {
                    // word of punctuation marks
                    if (!(Character.isLetterOrDigit(character) || character == '-')) {
                        // continue add a punctuation to punctuation word
                        wordInConstruction.append(character);
                    } else {
                        // the punctuation word ended, so save it and start building a normal word
                        if (wordInConstruction.length() > 0) {
                            completeWordList.add(wordInConstruction.toString());
                        }
                        wordInConstruction = new StringBuilder();
                        wordInConstruction.append(character);
                    }
                }
            }
            if (wordInConstruction.length() > 0) {
                completeWordList.add(wordInConstruction.toString());
            }
        }

        final String[] completeWordArray = new String[completeWordList.size()];
        for (int i = 0; i < completeWordArray.length; i++) {
            completeWordArray[i] = completeWordList.get(i);
        }
        return completeWordArray;
    }

    @Override
    public Sentence getSentence(final String text) {
        final String[] words = splitInWords(text);

        // create a new sentence from the given text
        final List<Word> sentenceWords = new ArrayList<>();
        for (String word : words) {
            // check if the word already exists in DB - if not: create a new one
            final List<Word> existingWords = wordRepository.getByTextWithoutDiacriticsIgnoreCase(Word.replaceDiacritics(word));
            if (!existingWords.isEmpty()) {
                sentenceWords.add(existingWords.get(0));
            } else {
                final Word newWord = new Word();
                newWord.setText(word);
                wordRepository.save(newWord);
                sentenceWords.add(newWord);
            }
        }
        final Sentence newSentence = new Sentence();
        newSentence.setWords(sentenceWords);
        newSentence.setSpeechType(identifySentenceType(newSentence));

        // compare the new sentence with the existing one
        final Sentence existingSentence = identifySentence(text);

        if (existingSentence == null) {
            // no equivalent sentence found in DB
            sentenceRepository.save(newSentence);
            return newSentence;
        }

        final int wordCountDifference = Math.abs(newSentence.getWords().size() - existingSentence.getWords().size());
        if (wordCountDifference > 0
                && wordCountDifference > Math.min(newSentence.getWords().size(), existingSentence.getWords().size()) / 2
                && Math.max(newSentence.getWords().size(), existingSentence.getWords().size()) >= 3) { // exceptional case: S1.length == 1 && S2.length == 2
            // no equivalent sentence found in DB
            sentenceRepository.save(newSentence);
            return newSentence;
        }

        // the found sentence is mainly the same (maybe)
        boolean allWordsAreInTheSamePositions = true;
        int nrOfMatchedWords = 0;
        for (int i = 0; i < newSentence.getWords().size(); i++) {
            final String newSentenceWord = Word.replaceDiacritics(newSentence.getWords().get(i).getText());
            // check if the new word is equal with the existing word (on same position in sentences) or equal with a synonym of the existing word
            if (i < existingSentence.getWords().size()) {
                final String existingSentenceWord = Word.replaceDiacritics(existingSentence.getWords().get(i).getText());
                if (newSentenceWord.toLowerCase().equals(existingSentenceWord.toLowerCase())) {
                    nrOfMatchedWords++;
                    continue; // ok - matched the word
                }
//                if (existingSentence.getWords().get(i).getSynonyms().keySet().stream()
//                        .anyMatch(synonym -> synonym.getText().toLowerCase().equals(newSentenceWord.toLowerCase()))) {
//                    nrOfMatchedWords++;
//                    continue; // ok - matched a synonym
//                }
            }
            if (existingSentence.getWords().stream().anyMatch(word -> word.equals(new Word(newSentenceWord)))) {
                allWordsAreInTheSamePositions = false;
                nrOfMatchedWords++;
                continue; // ok - the word is somewhere else in the sentence
            }
//            if (existingSentence.getWords().stream().anyMatch(existingWord -> existingWord.getSynonyms().keySet().stream()
//                    .anyMatch(synonym -> synonym.getText().toLowerCase().equals(newSentenceWord.toLowerCase())))) {
//                allWordsAreInTheSamePositions = false;
//                nrOfMatchedWords++;
//                continue; // ok - the word has a synonym somewhere else in the sentence
//            }
        }

        if (allWordsAreInTheSamePositions && wordCountDifference == 0 && nrOfMatchedWords == newSentence.getWords().size()) {
            return existingSentence; // exact same sentence (probably allWordsAreInTheSamePositions==true is sufficient)
        }

        /*
        length 1 => synonym can have 0 extra words
        length 2 => synonym can have 1 extra words
        length 3 => synonym can have 1 extra words
        length 4 => synonym can have 2 extra words
        length 5 => synonym can have 2 extra words
        length 6 => synonym can have 3 extra words
        ...
        */
        final int minSentencesSize = Math.min(existingSentence.getWords().size(), newSentence.getWords().size());
        boolean canBeSynonymSentence = (minSentencesSize / 2 >= wordCountDifference) && (minSentencesSize / 2 <= nrOfMatchedWords);
        if (!allWordsAreInTheSamePositions && canBeSynonymSentence) {
            // the sentences are different, but synonyms
            newSentence.addSynonym(existingSentence);
            sentenceRepository.save(newSentence);
            existingSentence.addSynonym(newSentence);
        }

        sentenceRepository.save(existingSentence);
        return newSentence;
    }

    /**
     * @return the identified SpeechType
     * The function does not change the sentence field "speechType".
     */
    private SpeechType identifySentenceType(final Sentence sentence) {
        if (sentence.getWords().get(sentence.getWords().size() - 1).getText().contains("?")) {
            return DIRECTIVE;
        }

        final List<String> words = sentence.getWords().stream().map(Word::getText).collect(Collectors.toList());

        final List<List<String>> directiveExpressions = getLinguisticExpressionsByTypeAsStrings(DIRECTIVE);
        for (List<String> expression : directiveExpressions) {
            if (containsExpression(words, expression)) {
                return DIRECTIVE;
            }
        }

        final List<List<String>> acknowledgementExpressions = getLinguisticExpressionsByTypeAsStrings(ACKNOWLEDGEMENT);
        for (List<String> expression : acknowledgementExpressions) {
            if (containsExpression(words, expression)) {
                return ACKNOWLEDGEMENT;
            }
        }

        final List<List<String>> statementExpressions = getLinguisticExpressionsByTypeAsStrings(STATEMENT);
        for (List<String> expression : statementExpressions) {
            if (containsExpression(words, expression)) {
                return STATEMENT;
            }
        }

        return STATEMENT; // TODO: check in the future if it's ok
    }

    private List<List<String>> getLinguisticExpressionsByTypeAsStrings(final SpeechType speechType) {
        final List<LinguisticExpression> linguisticExpressions = linguisticExpressionRepository.findAllBySpeechType(speechType);
        final List<List<String>> stringExpressions = new ArrayList<>();

        for (LinguisticExpression linguisticExpression : linguisticExpressions) {
            final List<String> stringExpression = new ArrayList<>();
            for (ExpressionItem item : linguisticExpression.getItems()) {
                stringExpression.add(item.getText());
            }
            stringExpressions.add(stringExpression);
        }
        return stringExpressions;
    }

    @Override
    public void addResponse(final Sentence previousSentence, final Sentence sentence) {
        previousSentence.addResponse(sentence);
        sentenceRepository.save(previousSentence);
    }

    @Override
    public void addResponseAndSynonym(final Sentence previousSentence, final Sentence sentence) {
        previousSentence.addResponse(sentence);
        sentenceRepository.save(previousSentence);

        if (previousSentence.getResponses().containsKey(sentence) && sentence.getResponses().containsKey(previousSentence)) {
            previousSentence.addSynonym(sentence);
            sentenceRepository.save(previousSentence);
            sentence.addSynonym(previousSentence);
            sentenceRepository.save(sentence);
        }
    }

    @Override
    public Sentence generateResponse(final Message message) {
        final Sentence sentence = message.getEquivalentSentence();
        if (sentence == null) {
            return null;
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Equivalent Sentence: " + sentence);

        Sentence response = pickGoodResponseForSentence(sentence);
        if (response != null) {
            System.out.println("Response Sentence: " + response);
            return response;
        }

        // check for a response in sentence synonyms
        final List<Sentence> orderedSynonyms = sentence.getSynonyms().keySet().stream()
                .sorted((synonym1, synonym2) -> sentence.getSynonyms().get(synonym2).compareTo(sentence.getSynonyms().get(synonym1)))
                .collect(Collectors.toList());
        for (Sentence synonym : orderedSynonyms) {
            response = pickGoodResponseForSentence(synonym);
            if (response != null) {
                System.out.println("Synonym of Sentence: " + synonym);
                System.out.println("Response Sentence from synonym: " + response);
                return response;
            }
        }

        return null;
    }

    @Override
    public String translateSentenceToText(final Sentence sentence) {
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < sentence.getWords().size(); i++) {
            final Word word = sentence.getWords().get(i);
            if (i > 0 && Character.isLetterOrDigit(word.getText().charAt(0))) {
                text.append(" ");
            }
            text.append(word.getText());
        }

        return text.toString();
    }

    /**
     * @return random good response or <null> if no responses are available for the provided sentence
     */
    private Sentence pickGoodResponseForSentence(final Sentence sentence) {
        if (sentence.getResponses().isEmpty()) {
            return null;
        }

        final List<Sentence> sortedResponses = sentence.getResponses().keySet().stream()
                .sorted((Sentence response1, Sentence response2) -> {
                    final Integer response1Frequency = sentence.getResponses().get(response1);
                    final Integer response2Frequency = sentence.getResponses().get(response2);
                    return response2Frequency.compareTo(response1Frequency);
                })
                .collect(Collectors.toList());

        int index = random.nextInt(sortedResponses.size());
        if (index != 0 && sortedResponses.size() > 10) {
            index = random.nextInt(index);
        }
        return sortedResponses.get(index);
    }

    /**
     * @return random good response or <null> if no responses are available for the provided sentence and for its synonyms
     * The returned sentence may be a synonym of a picked response.
     */
    private Sentence pickGoodResponseForSentenceIncludingSynonyms(final Sentence sentence) {
        final Map<Sentence, Integer> sentenceAndSynonymsResponses = new HashMap<>(sentence.getResponses());
        for (Sentence synonym : sentence.getSynonyms().keySet()) {
            for (Sentence response : synonym.getResponses().keySet()) {
                sentenceAndSynonymsResponses.put(response, synonym.getResponses().get(response));
            }
        }

        final List<Sentence> sortedResponses = sentenceAndSynonymsResponses.keySet().stream()
                .sorted((Sentence response1, Sentence response2) -> {
                    final Integer response1Frequency = sentenceAndSynonymsResponses.get(response1);
                    final Integer response2Frequency = sentenceAndSynonymsResponses.get(response2);
                    return response2Frequency.compareTo(response1Frequency);
                })
                .collect(Collectors.toList());

        if (sortedResponses.isEmpty()) {
            return null;
        }

        int index = random.nextInt(sortedResponses.size());
        if (index != 0 && sortedResponses.size() > 10) {
            index = random.nextInt(index);
        }
        final Sentence response = sortedResponses.get(index);
        Sentence chosenResponse = response;
        // pick a synonym of the chosen response
        if (random.nextBoolean() && !response.getSynonyms().isEmpty()) {
            final List<Sentence> synonyms = response.getSynonyms().keySet().stream()
                    .sorted((Sentence response1, Sentence response2) -> {
                        final Integer response1Frequency = response.getSynonyms().get(response1);
                        final Integer response2Frequency = response.getSynonyms().get(response2);
                        return response2Frequency.compareTo(response1Frequency);
                    })
                    .collect(Collectors.toList());
            int indexSynonym = random.nextInt(synonyms.size());
            if (indexSynonym != 0 && synonyms.size() > 10) {
                indexSynonym = random.nextInt(indexSynonym);
            }
            chosenResponse = synonyms.get(indexSynonym);
        }

        return chosenResponse;
    }

    /**
     * @return best sentence identified (it should have at last 75% of items matched with the provided text) or <null> if no proper sentence is found
     * We search in every sentence's items and in sentence's items synonyms.
     */
    private Sentence identifySentence(final String text) {
        // find those sentences that containsExpression the items from the given text
        final List<String> words = Arrays.asList(splitInWords(text));
        final int[] bestMatchedCount = {0};
        final List<Sentence> matchedSentences = sentenceRepository.findAll().parallelStream()
                .filter(sentence -> {
                    // 1. check in sentence's own items
                    final List<String> sentenceWords = sentence.getWords().stream().map(Word::getText).collect(Collectors.toList());
                    final int nrOfMatchesSentenceWords = calculateNrOfMatches(sentenceWords, words);
                    if (nrOfMatchesSentenceWords > Math.min(words.size(), sentence.getWords().size()) / 1.5
                            || (nrOfMatchesSentenceWords >= 1 && nrOfMatchesSentenceWords <= 2 && Math.max(words.size(), sentence.getWords().size()) <= 3)) {
                        return true;
                    }
                    // 2. check in items's synonyms
                    final Set<String> matchedWordsSynonyms = new HashSet<>();
                    final Set<String> lowerCaseWords = words.stream().map(String::toLowerCase).collect(Collectors.toSet());
                    for (Word sentenceWord : sentence.getWords()) {
                        final List<String> wordSynonyms = sentenceWord.getSynonyms().keySet().stream().map(Word::getText).map(String::toLowerCase).collect(Collectors.toList());
                        for (String synonym : wordSynonyms) {
                            if (lowerCaseWords.contains(synonym)) {
                                matchedWordsSynonyms.add(synonym);
                            }
                        }
                    }
                    final int minWordsToMatch = words.size() / 2;
                    final int maxWordsToMatch = words.size() + minWordsToMatch;
                    return matchedWordsSynonyms.size() > minWordsToMatch && matchedWordsSynonyms.size() < maxWordsToMatch;
                })
                .sorted((Sentence sentence1, Sentence sentence2) -> {
                    final List<String> sentence1Words = sentence1.getWords().stream().map(Word::getText).collect(Collectors.toList());
                    final List<String> sentence2Words = sentence2.getWords().stream().map(Word::getText).collect(Collectors.toList());
                    final Integer nrOfMatchedWordsS1 = calculateNrOfMatches(sentence1Words, words);
                    final Integer nrOfMatchedWordsS2 = calculateNrOfMatches(sentence2Words, words);

                    if (nrOfMatchedWordsS1 > bestMatchedCount[0]) {
                        bestMatchedCount[0] = nrOfMatchedWordsS1;
                    }
                    if (nrOfMatchedWordsS2 > bestMatchedCount[0]) {
                        bestMatchedCount[0] = nrOfMatchedWordsS2;
                    }

                    final int extraWordsS1 = sentence1Words.size() - nrOfMatchedWordsS1;
                    final int extraWordsS2 = sentence2Words.size() - nrOfMatchedWordsS2;
                    // less is better (0 = perfect match)
                    final Integer matchScoreS1 = Math.abs(nrOfMatchedWordsS1 - words.size()) - extraWordsS1 / 2;
                    final Integer matchScoreS2 = Math.abs(nrOfMatchedWordsS2 - words.size()) - extraWordsS2 / 2;
                    return matchScoreS2.compareTo(matchScoreS1);
                })
                .collect(Collectors.toList());

        // return the best sentence or <null> if we didn't find a good one
        if (matchedSentences.isEmpty() || bestMatchedCount[0] < words.size() / 2) {
            return null;
        }
        return matchedSentences.get(0);
    }

    /**
     * @return how many items from list1 are in list2
     */
    private int calculateNrOfMatches(final List<String> list1, final List<String> list2) {
        list1.replaceAll(String::toUpperCase);
        list2.replaceAll(String::toUpperCase);
        int nrOfMatches = 0;
        for (String word : list1) {
            if (partialContains(list2, word)) {
                nrOfMatches++;
            }
        }
        return nrOfMatches;
    }

    /**
     * @return true if the wordToFind is found in items (whole or partial) and false otherwise.
     * It is case sensitive.
     */
    private boolean partialContains(final List<String> words, final String wordToFind) {
        for (String word : words) {
            if (word.equals(wordToFind) || word.contains(wordToFind) || wordToFind.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the expression is found in sentence and false otherwise.
     * It is NOT case sensitive.
     */
    private boolean containsExpression(final List<String> sentence, final List<String> expression) {
        int i = 0;
        for (String wordE : expression) {
            if (i >= sentence.size()) {
                return false;
            }
            final String wordS = sentence.get(i);
            if (wordE == null || wordE.toLowerCase().equals(wordS.toLowerCase())) {
                i++;
            } else if (i > 0) {
                return false;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public Sentence pickRandomSentence() {
        final long nrOfSentences = sentenceRepository.count();
        if (nrOfSentences == 0) {
            return generateDefaultSentence();
        }
        return sentenceRepository.findAll().get(random.nextInt((int) nrOfSentences));
    }

    private Sentence generateDefaultSentence() {
        final List<Word> wordsWithoutDiacritics = wordRepository.getByTextWithoutDiacriticsIgnoreCase("salut");
        boolean wordExists = !wordsWithoutDiacritics.isEmpty();
        if (wordExists) {
            final List<Word> words = new ArrayList<>();
            words.add(wordsWithoutDiacritics.get(0));
            return sentenceRepository.findAllByWords(words).stream().min(Comparator.comparingInt(s -> s.getWords().size())).get();
        } else {
            final Word word = new Word();
            word.setText("Salut");
            //wordRepository.save(word);
            final Sentence sentence = new Sentence();
            sentence.getWords().add(word);
            sentence.setSpeechType(STATEMENT);
            sentenceRepository.save(sentence);
            return sentence;
        }
    }

    @Override
    @Transactional
    public Sentence pickSentenceWithFewReplies() {
        final long nrOfSentences = sentenceRepository.count();
        if (nrOfSentences == 0) {
            return generateDefaultSentence();
        }
        return sentenceRepository.findAll().parallelStream().min((sentence1, sentence2) -> {
            final Integer nrOfResponses_sentence1 = sentence1.getResponses().size();
            final Integer nrOfResponses_sentence2 = sentence2.getResponses().size();
            return nrOfResponses_sentence1.compareTo(nrOfResponses_sentence2);
        }).get();
    }

    @Override
    public Sentence pickSentenceRequestingInformation(final User user) {
        Class informationClass = null;
        String informationFieldNamePath = null;
        final List<Class> informationClasses = new ArrayList<>();
        informationClasses.add(PersonalInformation.class);
        informationClasses.add(SchoolInformation.class);
        informationClasses.add(FacultyInformation.class);
        informationClasses.add(FreeTimeInformation.class);
        informationClasses.add(RelationshipsInformation.class);

        for (Class infoClass : informationClasses) {
            try {
                final Method getterOfUser = user.getClass().getMethod("get" + infoClass.getSimpleName());
                final Information information = (Information) getterOfUser.invoke(user);

                informationFieldNamePath = getFirstNullItemNamePath(information, infoClass);
                if (informationFieldNamePath != null) {
                    informationClass = infoClass;
                    break;
                }

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (informationClass == null || informationFieldNamePath == null) {
            // pick random one
            try {
                informationClass = informationClasses.get(random.nextInt(informationClasses.size()));
                informationFieldNamePath = pickRandomField(informationClass);
            } catch (IntrospectionException e) {
                e.printStackTrace();
                return pickRandomSentence();
            }
        }

        final List<Sentence> sentences = sentenceRepository.findAllBySpeechTypeAndInformationClassAndInformationFieldNamePath(DIRECTIVE, informationClass, informationFieldNamePath);
        if (sentences.isEmpty()) {
            return pickRandomSentence();
        }
        if (informationFieldNamePath.endsWith("locality") && user.getPersonalInformation().getHomeAddress().getLocalityType() != null) {
            String localityType = "sat";
            if (user.getPersonalInformation().getHomeAddress().getLocalityType().equals(URBAN)) {
                localityType = "oraș";
            }
            final List<Sentence> sentencesByLocalityType = getSentencesThatContains(sentences, new Word(localityType));
            return sentencesByLocalityType.get(random.nextInt(sentencesByLocalityType.size()));
        }
        return sentences.get(random.nextInt(sentences.size()));
    }

    private List<Sentence> getSentencesThatContains(final List<Sentence> sentences, final Word wordToContain) {
        final List<Sentence> sentencesThatContainsTheWord = new ArrayList<>();
        for (Sentence sentence : sentences) {
            for (Word word : sentence.getWords()) {
                if (word.equals(wordToContain)) {
                    sentencesThatContainsTheWord.add(sentence);
                }
            }
        }
        return sentencesThatContainsTheWord;
    }

    /**
     * @return the path to a random field
     */
    private String pickRandomField(final Class objectClass) throws IntrospectionException {
        final BeanInfo beanInformation = Introspector.getBeanInfo(objectClass, Object.class);
        final PropertyDescriptor[] propertyDescriptors = beanInformation.getPropertyDescriptors();
        PropertyDescriptor selectedPropertyDescriptor = null;
        // take a random field, but not the id
        do {
            int index = random.nextInt(propertyDescriptors.length);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (index == 0) {
                    selectedPropertyDescriptor = propertyDescriptor;
                }
                index--;
            }
        } while (selectedPropertyDescriptor.getName().equals("id") || selectedPropertyDescriptor.getName().equals("email") || selectedPropertyDescriptor.getName().equals("password"));

        final Method getterOfObject = selectedPropertyDescriptor.getReadMethod();
        if (getterOfObject.getReturnType().equals(null)) return null;
        return null;
    }

    /**
     * information and informationClassDto should point the same Information class
     *
     * @param information      may be null
     * @param informationClass indicates the class to refer if @information is null
     * @return first not set value of the information class in a preset order or null if all the fields of the information are filled
     * Example for PersonalInformation: "firstName", "birthDay", "address.street", "address.number", "grades#math"
     */
    private String getFirstNullItemNamePath(final Information information, final Class informationClass) {
        // If information is null, then return the most significant field of the class
        if (information == null) {
            if (informationClass.equals(PersonalInformation.class)) return "firstName";
            if (informationClass.equals(SchoolInformation.class)) return "isAtSchool";
            if (informationClass.equals(FacultyInformation.class)) return "isAtFaculty";
            if (informationClass.equals(FreeTimeInformation.class)) return "likeReading";
            if (informationClass.equals(RelationshipsInformation.class)) return "numberOfBrothersAndSisters";
        }

        // The information is not null, but may be a field of it
        try {
            final List<String> fieldNamesInImportanceOrder = information.getFieldNamesInImportanceOrder();
            for (String fieldName : fieldNamesInImportanceOrder) {
                final String fieldName_firstLetterCapitalize = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                final Method getterOfInformation = information.getClass().getMethod("get" + fieldName_firstLetterCapitalize);
                final Object info = getterOfInformation.invoke(information);

                if (info == null) {
                    if (getterOfInformation.getReturnType().equals(HashMap.class) || getterOfInformation.getReturnType().equals(ArrayList.class)) {
                        return fieldName + "#?";
                    }
                    if (fieldName.startsWith("homeAddress")) {
                        final Address address = (Address) getterOfInformation.invoke(information);
                        if (address == null) {
                            return "homeAddress.planet";
                        }
                        final Method getterOfAddress = address.getClass().getMethod("getLocalityType");
                        final LocalityType localityType = (LocalityType) getterOfAddress.invoke(address);
                        if (localityType != null && localityType.equals(RURAL) && (fieldName.endsWith("neighborhood") || fieldName.endsWith("floor") || fieldName.endsWith("apartmentNumber"))) {
                            continue;
                        }
                    }
                    return fieldName;
                } else {
                    // the information is not null, but if it's a complex object (ex: SimpleDate, Address, PersonalInformation, Map, List etc.) check if has null attributes
                    if (info instanceof SimpleDate) {
                        final SimpleDate simpleDate = (SimpleDate) info;
                        if (simpleDate.getYear() == null) {
                            return fieldName + ".year";
                        }
                        if (simpleDate.getDay() == null) {
                            return fieldName + ".day";
                        }
                    }
                    if (info instanceof PersonalInformation) {
                        final PersonalInformation personalInformation = (PersonalInformation) info;
                        final String nullItemPathFromPersonalInformation = getFirstNullItemNamePath(personalInformation, informationClass);
                        if (nullItemPathFromPersonalInformation != null) {
                            return fieldName + "." + nullItemPathFromPersonalInformation;
                        }
                    }
                    if (info instanceof Address) {
                        final Address address = (Address) info;
                        final List<String> addressFieldNamesInImportanceOrder = address.getFieldNamesInImportanceOrder();
                        for (String addressFieldName : addressFieldNamesInImportanceOrder) {
                            final String addressFieldName_firstLetterCapitalize = addressFieldName.substring(0, 1).toUpperCase() + addressFieldName.substring(1);
                            final Method getterOfAddress = address.getClass().getMethod("get"+ addressFieldName_firstLetterCapitalize);
                            final Object addressField = getterOfAddress.invoke(address);
                            if (addressField == null) {
                                return fieldName + "." + addressFieldName;
                            }
                        }
                        // The fields from the list are not null. If localityType == URBAN, check the remaining (urban) fields.
                        final Method getterOfAddress = address.getClass().getMethod("getLocalityType");
                        final LocalityType localityType = (LocalityType) getterOfAddress.invoke(address);
                        if (localityType.equals(URBAN)) {
                            final List<String> addressUrbanFieldNamesInImportanceOrder = address.getUrbanFieldNamesInImportanceOrder();
                            for (String addressFieldName : addressUrbanFieldNamesInImportanceOrder) {
                                final String addressFieldName_firstLetterCapitalize = addressFieldName.substring(0, 1).toUpperCase() + addressFieldName.substring(1);
                                final Method getterOfAddress2 = address.getClass().getMethod("get"+ addressFieldName_firstLetterCapitalize);
                                final Object addressField = getterOfAddress2.invoke(address);
                                if (addressField == null) {
                                    return fieldName + "." + addressFieldName;
                                }
                            }
                        }
                        return null;
                    }
                    if (info instanceof Map) {
                        final Map map = (Map) info;
                        if (map.isEmpty()) {
                            return fieldName + "#?";
                        }

                        for (Object key : map.keySet()) {
                            final Object value = map.get(key);
                            if (value instanceof Integer && (Integer) value == 0) {
                                return fieldName + "#" + key.toString();
                            }
                            if (value instanceof PersonalInformation) {
                                final PersonalInformation personalInformation = (PersonalInformation) value;
                                final String nullItemPathFromPersonalInformation = getFirstNullItemNamePath(personalInformation, informationClass);
                                if (nullItemPathFromPersonalInformation != null) {
                                    return fieldName + "." + nullItemPathFromPersonalInformation;
                                }
                            }
                        }
                    }
                    if (info instanceof List) {
                        final List list = (List) info;
                        if (list.isEmpty()) {
                            return fieldName + "#?";
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
