package services.impl;

import domain.entities.Address;
import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.SentenceDetectionParameters;
import domain.entities.SimpleDate;
import domain.entities.User;
import domain.entities.Word;
import domain.enums.AddressingMode;
import domain.enums.LocalityType;
import domain.enums.SpeechType;
import domain.information.FacultyInformation;
import domain.information.FreeTimeInformation;
import domain.information.Information;
import domain.information.PersonalInformation;
import domain.information.RelationshipsInformation;
import domain.information.SchoolInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import repositories.DexRepository;
import repositories.LinguisticExpressionRepository;
import repositories.SentenceDetectionParametersRepository;
import repositories.SentenceRepository;
import repositories.WordRepository;
import services.api.ChatbotService;
import services.api.MessageService;

import javax.transaction.Transactional;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static app.Main.CHATBOT_ID;
import static domain.enums.AddressingMode.FORMAL;
import static domain.enums.AddressingMode.FORMAL_AND_INFORMAL;
import static domain.enums.AddressingMode.INFORMAL;
import static domain.enums.LocalityType.RURAL;
import static domain.enums.LocalityType.URBAN;
import static domain.enums.SpeechType.ACKNOWLEDGEMENT;
import static domain.enums.SpeechType.DIRECTIVE;
import static domain.enums.SpeechType.STATEMENT;

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private static final String wordsSplitRegex = "[\\s]+"; // TODO: change regex with a custom function which consider also the signs as items (, . ...)
    private final MessageService messageService;
    private final SentenceRepository sentenceRepository;
    private final WordRepository wordRepository;
    private final SentenceDetectionParametersRepository sentenceDetectionParametersRepository;
    private final DexRepository dexRepository;
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final Random random;

    public ChatbotServiceImpl(MessageService messageService, SentenceRepository sentenceRepository, WordRepository wordRepository, SentenceDetectionParametersRepository sentenceDetectionParametersRepository, DexRepository dexRepository, LinguisticExpressionRepository linguisticExpressionRepository) {
        this.messageService = messageService;
        this.sentenceRepository = sentenceRepository;
        this.wordRepository = wordRepository;
        this.sentenceDetectionParametersRepository = sentenceDetectionParametersRepository;
        this.dexRepository = dexRepository;
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        random = new Random();
    }

    static String[] splitInWords(final String string) {
        final String[] words = string.split(wordsSplitRegex);
        final List<String> completeWordList = new ArrayList<>();
        for (String word : words) {
            // keep punctuation as a word
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
    @Transactional
    public Sentence getExistingSentenceOrCreateANewOne(final String text) {
        final String[] words = splitInWords(text);

        // create a new sentence from the given text
        final List<Word> sentenceWords = new ArrayList<>();
        for (String textWord : words) {
            // check if the word already exists in DB - if not: create a new one
            final Word existingWord = wordRepository.getFirstByTextIgnoreCase(Word.replaceDiacritics(textWord));
            if (existingWord == null) {
                // the word is totally new
                final Word newWord = new Word(textWord);
                sentenceWords.add(newWord);
                wordRepository.save(newWord);
            } else {
                // the word may exists
                final Word word = new Word(textWord);
                final boolean wordAlreadyExists = existingWord.equals(word);
                if (wordAlreadyExists){
                    sentenceWords.add(existingWord);
                } else {
                    final Word newWord = new Word(textWord);
                    sentenceWords.add(newWord);
                    wordRepository.save(newWord);
                }
            }
        }

        final Sentence newSentence = new Sentence();
        newSentence.setWords(sentenceWords);
        newSentence.setSpeechType(identifySentenceType(newSentence));

        // get similar sentences from DB
        SentenceDetectionParameters sentenceDetectionParameters = sentenceDetectionParametersRepository.findBySentenceLength(sentenceWords.size());
        if (sentenceDetectionParameters == null) {
            sentenceDetectionParameters = sentenceDetectionParametersRepository.findTop1ByOrderBySentenceLengthDesc();
        }
        final List<Sentence> existingSentences = findSimilarSentencesByWords(sentenceWords,
                sentenceDetectionParameters.getMaxNrOfExtraWords(),
                sentenceDetectionParameters.getMaxNrOfUnmatchedWords(),
                sentenceDetectionParameters.getWeight());

        // choose the most appropriate sentence
        if (existingSentences.isEmpty()) {
            sentenceRepository.save(newSentence);
            return newSentence;
        }

        for (Sentence sentence : existingSentences) {
            if (newSentence.getWords().equals(sentence.getWords())) {
                // exact same sentence, it already exists in DB
                return sentence;
            }
        }

        // set the new sentence as synonym for existing ones
        sentenceRepository.save(newSentence);
        for (Sentence sentence : existingSentences) {
            sentence.addSynonym(newSentence);
            sentenceRepository.save(sentence);
            newSentence.addSynonym(sentence);
        }
        sentenceRepository.save(newSentence);
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
    @Transactional
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
    public String translateSentenceToText(final Sentence sentence, final AddressingMode addressingMode) {
        final StringBuilder text = new StringBuilder();
        for (int i = 0; i < sentence.getWords().size(); i++) {
            final Word word = sentence.getWords().get(i);
            if (i > 0 && (Character.isLetterOrDigit(word.getText().charAt(0)) || word.getText().charAt(0) == '(')) {
                text.append(" ");
            }
            // check addressing mode
            if (addressingMode == null || addressingMode == FORMAL_AND_INFORMAL) {
                text.append(word.getTextWithDiacritics().toLowerCase());
                continue;
            }
            // check for greetings
            final int greetingWordCount = wordIsPartOfGreeting(sentence, i); // 0, 1 or 2
            if (greetingWordCount > 0) {
                // the word is the first word of a greeting in sentence
                final List<String> newGreeting = getGreetingByAddressingMode(addressingMode);
                // skip the current greeting from the sentence
                i += greetingWordCount;
                // insert the new greeting in text
                for (int j = 0; j < newGreeting.size(); j++) {
                    text.append(newGreeting.get(j));
                    if (j < newGreeting.size() - 1) {
                        text.append(" ");
                    }
                }
                continue;
            }

            // tu / dumneavoastră
            if (word.getText().toLowerCase().equals("tu") && FORMAL.equals(addressingMode)) {
                text.append("dumneavoastră");
                continue;
            }
            if (word.getText().toLowerCase().equals("dumneavoastra") && INFORMAL.equals(addressingMode)) {
                text.append("tu");
                continue;
            }

            // identify an appropriate word for addressing mode
            String appropriateWord = getWordWithAddressingModeFromWordAndSynonyms(word, addressingMode);
            if (appropriateWord == null) {
                final boolean isImperative = sentence.getWords().get(sentence.getWords().size()-1).getText().contains("!");
                appropriateWord = dexRepository.getWordWithAddressingModeFromDex(word, addressingMode, isImperative);
            }
            if (appropriateWord == null) {
                appropriateWord = word.getTextWithDiacritics();
            }
            text.append(appropriateWord.toLowerCase());
        }

        return text.toString();
    }

    /**
     * @param addressingMode should be FORMAL or INFORMAL
     */
    private List<String> getGreetingByAddressingMode(final AddressingMode addressingMode) {
        switch (addressingMode) {
            case FORMAL: {
                final List<String> greeting = new ArrayList<>();
                final Word wordBună = getOrAddWordInDb("bună", FORMAL_AND_INFORMAL);
                greeting.add(wordBună.getTextWithDiacritics());

                final LocalTime currentTime = LocalTime.now();
                if (currentTime.isBefore(endOfMorning)) {
                    final Word wordDimineața = getOrAddWordInDb("dimineața", null);
                    greeting.add(wordDimineața.getTextWithDiacritics());
                }
                else if (currentTime.isAfter(startOfEvening)) {
                    final Word wordSeara = getOrAddWordInDb("seara", null);
                    greeting.add(wordSeara.getTextWithDiacritics());
                }
                else if (random.nextBoolean()) {
                    final Word wordZiua = getOrAddWordInDb("ziua", null);
                    greeting.add(wordZiua.getTextWithDiacritics());
                }
                return greeting;
            }
            default: {
                final List<String> greeting = new ArrayList<>();
                final LocalTime currentTime = LocalTime.now();
                if (currentTime.isBefore(LocalTime.of(11, 0))) {
                    if (random.nextBoolean()) {
                        final Word wordNeața = getOrAddWordInDb("neața", null);
                        greeting.add(wordNeața.getTextWithDiacritics());
                    } else {
                        final Word wordBună = getOrAddWordInDb("bună", null);
                        greeting.add(wordBună.getTextWithDiacritics());
                        final Word wordDimi = getOrAddWordInDb("dimi", null);
                        greeting.add(wordDimi.getTextWithDiacritics());
                    }
                    return greeting;
                }
                final List<Word> greetings = new ArrayList<>();
                greetings.add(getOrAddWordInDb("salut", null));
                greetings.add(getOrAddWordInDb("bună", null));
                greetings.add(getOrAddWordInDb("servus", null));
                greetings.add(getOrAddWordInDb("salutare", null));
                greetings.add(getOrAddWordInDb("hey", null));
                final int index = random.nextInt(greetings.size());
                greeting.add(greetings.get(index).getTextWithDiacritics());
                return greeting;
            }
        }
    }

    /**
     * WARNING: if the word already exists, we don't check if it has the same addressing mode
     * */
    private Word getOrAddWordInDb(final String text, final AddressingMode addressingMode) {
        Word word = wordRepository.getFirstByTextIgnoreCase(Word.replaceDiacritics(text));
        if (word == null) {
            word = new Word(text);
            word.setAddressingMode(addressingMode);
            wordRepository.save(word);
        }
        return word;
    }

    private int wordIsPartOfGreeting(final Sentence sentence, int wordIndex) {
        final String word = sentence.getWords().get(wordIndex).getText();
        if (word.equals("salut") || word.equals("servus") || word.equals("salutare") || word.equals("hey")) {
            return 1;
        }
        if (word.equals("buna") && wordIndex == sentence.getWords().size() - 1) {
            return 1;
        }
        if (!word.equals("buna")) {
            return 0;
        }
        final String nextWord = sentence.getWords().get(wordIndex+1).getText();
        if (nextWord.equals("ziua") || nextWord.equals("dimineata") || nextWord.equals("seara") || nextWord.equals("dimi")) {
            return 2;
        }
        return 0;
    }

    /**
     * @param word - may not have an addressing mode
     * @param addressingMode must not be null
     * @return the word if it has the same addressing mode, else a synonym of the word with the same addressing mode, else <null>
     * IMPORTANT: FORMAL == FORMAL_AND_INFORMAL && INFORMAL == FORMAL_AND_INFORMAL
     */
    private String getWordWithAddressingModeFromWordAndSynonyms(final Word word, final AddressingMode addressingMode) {
        if (addressingMode.equals(word.getAddressingMode()) || FORMAL_AND_INFORMAL.equals(word.getAddressingMode())) {
            return word.getTextWithDiacritics();
        }
        final List<Word> synonymsInAppearanceOrder = word.getSynonyms().keySet().stream()
                .sorted((synonym1, synonym2) -> word.getSynonyms().get(synonym2).compareTo(word.getSynonyms().get(synonym1)))
                .collect(Collectors.toList());
        for (Word synonym : synonymsInAppearanceOrder) {
            if (addressingMode.equals(synonym.getAddressingMode()) || FORMAL_AND_INFORMAL.equals(synonym.getAddressingMode())) {
                return synonym.getTextWithDiacritics();
            }
        }
        return null;
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
     * @param words                 - text to match
     * @param maxNrOfExtraWords     how many text may have in addition the identified sentences (inclusive). Used in filter and sorting.
     * @param maxNrOfUnmatchedWords how many text may not be matched int the sentences (inclusive). Used in filter and sorting.
     * @param weight                ∈ [0,1] and means the priority of maxNrOfExtraWords and maxNrOfUnmatchedWords. Used only in sorting.
     *                              0 = <nrOfExtraWords> should be as low as possible, don't care about <nrOfUnmatchedWords>
     *                              1 = <nrOfUnmatchedWords> should be as low as possible, don't care about <nrOfExtraWords>
     * @return similar sentences identified (it should have at last 75% of items matched with the provided text) or an empty list if no proper sentence is found
     * Matching a sentence means:
     * - the text from the given list must be in the sentence
     * - if the word is not in the sentence, check if a synonym of the word is in the sentence
     */
    public List<Sentence> findSimilarSentencesByWords(final List<Word> words, final int maxNrOfExtraWords, final int maxNrOfUnmatchedWords, final double weight) {
        final Map<Long, MatchingResult> matchingResultMap = new HashMap<>(); // <Sentence.id, MatchingResult>

        return sentenceRepository.findAll().stream()
                .filter(sentence -> {
                    if (Math.abs(words.size() - sentence.getWords().size()) > maxNrOfExtraWords) {
                        return false;
                    }
                    final MatchingResult matchingResult = compareWords(words, sentence.getWords());
                    final boolean isOK = matchingResult.getNrOfExtraWords() <= maxNrOfExtraWords && matchingResult.getNrOfUnmatchedWords() <= maxNrOfUnmatchedWords;
                    if (isOK) {
                        matchingResultMap.put(sentence.getId(), matchingResult);
                    }
                    return isOK;
                })
                .sorted((Sentence sentence1, Sentence sentence2) -> {
                    final MatchingResult mrS1 = matchingResultMap.get(sentence1.getId());
                    final MatchingResult mrS2 = matchingResultMap.get(sentence2.getId());

                    final double nrOfExtraWordsWeight = 1 - weight;
                    final double nrOfUnmatchedWords = 1 - nrOfExtraWordsWeight;
                    final Double differenceSentence1 = Math.abs(mrS1.getNrOfExtraWords() * nrOfExtraWordsWeight - mrS1.getNrOfUnmatchedWords() * nrOfUnmatchedWords);
                    final Double differenceSentence2 = Math.abs(mrS2.getNrOfExtraWords() * nrOfExtraWordsWeight - mrS2.getNrOfUnmatchedWords() * nrOfUnmatchedWords);
                    // less is better (0 difference = best match)
                    return differenceSentence1.compareTo(differenceSentence2);
                })
                .collect(Collectors.toList());
    }

    /**
     * The function does not take into account the positions of the text.
     * Consider that a word from <wordsToMatch> exists in <text> if the the list contains the word or a synonym of the word
     */
    private MatchingResult compareWords(final List<Word> wordsToMatch, final List<Word> words) {
        int nrOfExtraWords = 0;
        int nrOfUnmatchedWords = 0;

        for (Word wordToMatch : wordsToMatch) {
            if (!words.contains(wordToMatch) || wordToMatch.getSynonyms().keySet().stream().anyMatch(words::contains)) {
                nrOfUnmatchedWords++;
            }
        }
        for (Word word : words) {
            if (!wordsToMatch.contains(word) || word.getSynonyms().keySet().stream().anyMatch(wordsToMatch::contains)) {
                nrOfExtraWords++;
            }
        }
        return new MatchingResult(nrOfExtraWords, nrOfUnmatchedWords);
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
            return generateGreetingSentence();
        }
        return sentenceRepository.findAll().get(random.nextInt((int) nrOfSentences));
    }

    @Override
    public Sentence generateGreetingSentence() {
        final Word wordSalut = wordRepository.getFirstByTextIgnoreCase("salut");
        boolean wordExists = wordSalut != null;
        if (wordExists) {
            final List<Word> words = new ArrayList<>();
            words.add(wordSalut);
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
            return generateGreetingSentence();
        }
        return sentenceRepository.findAll().stream()
                .filter(sentence -> sentence.getInformationClass() == null)
                .min((sentence1, sentence2) -> {
            Integer nrOfResponses_sentence1 = sentence1.getResponses().size();
            Integer nrOfResponses_sentence2 = sentence2.getResponses().size();
            if (sentence1.getSynonyms().keySet().stream().anyMatch(synonym -> !synonym.getResponses().isEmpty())) {
                nrOfResponses_sentence1++;
            }
            if (sentence2.getSynonyms().keySet().stream().anyMatch(synonym -> !synonym.getResponses().isEmpty())) {
                nrOfResponses_sentence2++;
            }
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
                            final Method getterOfAddress = address.getClass().getMethod("get" + addressFieldName_firstLetterCapitalize);
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
                                final Method getterOfAddress2 = address.getClass().getMethod("get" + addressFieldName_firstLetterCapitalize);
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class MatchingResult {
        int nrOfExtraWords;
        int nrOfUnmatchedWords;
    }
}
