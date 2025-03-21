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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
        List<Sentence> synonymsOrdered = sentence.getSynonyms().keySet().stream()
                .sorted((synonym1, synonym2) -> sentence.getSynonyms().get(synonym2).compareTo(sentence.getSynonyms().get(synonym1)))
                .collect(Collectors.toList());

        // if no real synonym is present, take a response for the most similar sentence
        if (synonymsOrdered.isEmpty() || sentence.getSynonyms().get(synonymsOrdered.get(0)) <= 1) {
            final Map<Long, MatchingResult> matchingResultMap = new HashMap<>(); // <Sentence.id, MatchingResult>
            final Set<Sentence> synonyms = sentence.getSynonyms().keySet();
            for (Sentence synonym : synonyms) {
                final MatchingResult matchingResult = compareWords(synonym.getWords(), sentence.getWords(), sentence.getWords().size() >= 3);
                matchingResultMap.put(synonym.getId(), matchingResult);
            }
            final double weight = 0.9;
            synonymsOrdered = synonyms.stream()
                    .sorted((Sentence synonym1, Sentence synonym) -> {
                        final MatchingResult mrS1 = matchingResultMap.get(synonym1.getId());
                        final MatchingResult mrS2 = matchingResultMap.get(synonym.getId());

                        final double nrOfExtraWordsWeight = 1 - weight;
                        final double nrOfUnmatchedWords = 1 - nrOfExtraWordsWeight;
                        final Double differenceSentence1 = Math.abs(mrS1.getNrOfExtraWords() * nrOfExtraWordsWeight - mrS1.getNrOfUnmatchedWords() * nrOfUnmatchedWords);
                        final Double differenceSentence2 = Math.abs(mrS2.getNrOfExtraWords() * nrOfExtraWordsWeight - mrS2.getNrOfUnmatchedWords() * nrOfUnmatchedWords);
                        // less is better (0 difference = best match)
                        return differenceSentence1.compareTo(differenceSentence2);
                    })
                    .collect(Collectors.toList());
        }

        for (Sentence synonym : synonymsOrdered) {
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
            if (i > 0 && (Character.isLetterOrDigit(word.getText().charAt(0)) || word.getText().charAt(0) == '(')
                    && text.charAt(text.length() - 1) != '(') {
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

            // tu, ție, tău, ta / dumneavoastră
            if ((word.getText().toLowerCase().equals("tu") || word.getText().toLowerCase().equals("tie") || word.getText().toLowerCase().equals("tau") || word.getText().toLowerCase().equals("ta"))
                    && FORMAL.equals(addressingMode)) {
                text.append("dumneavoastră");
                continue;
            }
            if (word.getText().toLowerCase().startsWith("dumnea") && INFORMAL.equals(addressingMode)) {
                text.append("tu");
                continue;
            }

            // îți, te / vă
            if (word.getText().toLowerCase().equals("iti") || word.getText().toLowerCase().equals("te")) {
                if (FORMAL.equals(addressingMode)){
                    text.append("vă");
                    continue;
                }
                if (INFORMAL.equals(addressingMode) && word.getText().toLowerCase().equals("te")){
                    text.append("te");
                    continue;
                }
                if (INFORMAL.equals(addressingMode)){
                    text.append("îți");
                    continue;
                }
            }
            if (word.getText().toLowerCase().equals("va")) {
                Word previousWord = null;
                if (i > 0) previousWord = sentence.getWords().get(i - 1);
                if (INFORMAL.equals(addressingMode) && !(previousWord != null && (previousWord.getText().toLowerCase().equals("ma") || previousWord.getText().toLowerCase().equals("te")))) {
                    text.append("îți");
                    continue;
                }
                if (FORMAL.equals(addressingMode)){
                    text.append("vă");
                    continue;
                }
            }

            // ți-ar, ți-aș, te-ai / v-ar, v-ați
            if (word.getText().toLowerCase().startsWith("ti-") && FORMAL.equals(addressingMode)) {
                text.append(word.getTextWithDiacritics().replace("ți-", "v-").replace("ti-", "v-"));
                continue;
            }
            if (word.getText().toLowerCase().startsWith("te-ai") && FORMAL.equals(addressingMode)) {
                text.append(word.getTextWithDiacritics().replace("te-ai", "v-ați"));
                continue;
            }
            if (word.getText().toLowerCase().startsWith("te-") && FORMAL.equals(addressingMode)) {
                text.append(word.getTextWithDiacritics().replace("te-", "v-"));
                continue;
            }
            if (word.getText().toLowerCase().startsWith("v-") && INFORMAL.equals(addressingMode)) {
                text.append(word.getText().replace("v-", "ți-"));
                continue;
            }
//            if (word.getText().toLowerCase().startsWith("iti") && FORMAL.equals(addressingMode)) {
//                text.append(word.getTextWithDiacritics().replace("ți", "vă").replace("ti", "v"));
//                continue;
//            }
            if (word.getText().toLowerCase().equals("va") && INFORMAL.equals(addressingMode)) {
                text.append(word.getText().replace("va", "ți"));
                continue;
            }

            // -mi / -ți-mi
            if (word.getText().toLowerCase().endsWith("-mi") && FORMAL.equals(addressingMode)) {
                text.append(word.getTextWithDiacritics().replace("-mi", "-ți-mi"));
                continue;
            }

            // other words that are not well changed by DexRepository
            if (word.getText().toLowerCase().equals("ei")) {
                text.append("ei");
                continue;
            }
            if (word.getText().toLowerCase().equals("vrei") && addressingMode == INFORMAL) {
                text.append("vrei");
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

    public AddressingMode getAddressingModeOfWord(final List<Word> words, final int index) {
        final String word = words.get(index).getText().toLowerCase();
        String nextWord = null;
        if (words.size() > index + 1) {
            nextWord = words.get(index + 1).getText().toLowerCase();
        }

        // greeting
        if (nextWord != null) {
            if (word.equals("buna")) {
                if (nextWord.equals("dimineata") || nextWord.equals("ziua") || nextWord.equals("seara")) {
                    return FORMAL;
                }
                if (nextWord.equals("dimi")) {
                    return INFORMAL;
                }
                return null;
            }
        }
        if (word.equals("neata") || word.equals("salut") || word.equals("servus") || word.equals("salutare") || word.equals("hey")) {
            return INFORMAL;
        }
        // pronoun
        if (word.startsWith("dumnea") || word.equals("dvs") || word.equals("va")) {
            return FORMAL;
        }
        if (word.equals("tu") || word.equals("iti")) {
            return INFORMAL;
        }
        return null;
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

    /**
     * @return number of greeting words
     */
    private int wordIsPartOfGreeting(final Sentence sentence, int wordIndex) {
        if (wordIndex > 1) {
            return 0;
        }
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

    public Sentence pickGoodResponseForSentence(final Sentence sentence) {
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

        // create a virtual array of responses, every response is "frequency" times in array (ex: r1,r1,r1,r1, r2,r2, r3)
        int totalFrequency = 0;
        for (Sentence response : sortedResponses) {
            final int frequency = sentence.getResponses().get(response);
            if (frequency > 0) {
                totalFrequency += frequency;
            }
        }
        // pick a random index in the virtual array
        final int frequencyIndex = random.nextInt(totalFrequency);
        // pick the response from the virtual array
        totalFrequency = 0;
        for (Sentence response : sortedResponses) {
            final int frequency = sentence.getResponses().get(response);
            if (frequency > 0) {
                totalFrequency += frequency;
            }
            if (totalFrequency > frequencyIndex) {
                return response;
            }
        }

        // should never get here
        final Exception exception = new RuntimeException("BUG IN pickGoodResponseForSentence() METHOD. IT DID NOT PICKED A REPLY.");
        exception.printStackTrace();
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
     * For sentences with >=3 words ignore punctuation in sorting
     */
    public List<Sentence> findSimilarSentencesByWords(final List<Word> words, final int maxNrOfExtraWords, final int maxNrOfUnmatchedWords, final double weight) {
        final Map<Long, MatchingResult> matchingResultMap = new HashMap<>(); // <Sentence.id, MatchingResult>

        return sentenceRepository.findAll().stream()
                .filter(sentence -> {
                    if (Math.abs(words.size() - sentence.getWords().size()) > maxNrOfExtraWords) {
                        return false;
                    }
                    // ignore punctuation for sentences with 3 or more words (used only in sorting)
                    final MatchingResult matchingResult = compareWords(words, sentence.getWords(), sentence.getWords().size() >= 3);
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
    private MatchingResult compareWords(final List<Word> wordsToMatch, final List<Word> words, boolean ignorePunctuation) {
        int nrOfExtraWords = 0;
        int nrOfUnmatchedWords = 0;

        for (Word wordToMatch : wordsToMatch) {
            if (!words.contains(wordToMatch) && wordToMatch.getSynonyms().keySet().stream().noneMatch(words::contains)) {
                nrOfUnmatchedWords++;
            }
        }
        for (Word word : words) {
            if (!wordsToMatch.contains(word) && word.getSynonyms().keySet().stream().noneMatch(wordsToMatch::contains)) {
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
        final Word wordSalut = getOrAddWordInDb("salut", INFORMAL);
        final List<Word> words = new ArrayList<>();
        words.add(wordSalut);

        if (random.nextBoolean()) {
            // add „Eu sunt Andy”
            final Word dotOrExclamation;
            if (random.nextBoolean()) {
                dotOrExclamation = getOrAddWordInDb(".", null);
            } else {
                dotOrExclamation = getOrAddWordInDb("!", null);
            }
            final Word wordEu = getOrAddWordInDb("Eu", INFORMAL);
            final Word wordSunt = getOrAddWordInDb("sunt", INFORMAL);
            final Word wordAndy = getOrAddWordInDb("Andy", INFORMAL);
            words.add(dotOrExclamation);
            words.add(wordEu);
            words.add(wordSunt);
            words.add(wordAndy);
        }

        final Sentence sentence = new Sentence();
        sentence.setWords(words);
        sentence.setSpeechType(STATEMENT);
        sentenceRepository.save(sentence);
        return sentence;
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
        informationClasses.add(FreeTimeInformation.class);
        informationClasses.add(SchoolInformation.class);
        informationClasses.add(FacultyInformation.class);
        informationClasses.add(RelationshipsInformation.class);

        for (Class infoClass : informationClasses) {
            try {
                final Method getterOfUser = user.getClass().getMethod("get" + infoClass.getSimpleName());
                final Information information = (Information) getterOfUser.invoke(user);
                final Boolean probablyAtFaculty = probablyAtFaculty(user);
                if (information instanceof SchoolInformation && probablyAtFaculty != null && probablyAtFaculty && isAtFacultyUnknown(user)) {
                    final Boolean isAtSchool = user.getSchoolInformation().getIsAtSchool();
                    if (isAtSchool == null || !isAtSchool) {
                        continue; // go to FacultyInformation
                    }
                }
                // if the user is not at school/faculty, don't ask about that
                if (information instanceof SchoolInformation && ((SchoolInformation) information).getIsAtSchool() != null && !((SchoolInformation) information).getIsAtSchool()) {
                    continue;
                }
                if (information instanceof FacultyInformation && ((FacultyInformation) information).getIsAtFaculty() != null && !((FacultyInformation) information).getIsAtFaculty()) {
                    continue;
                }

                // default: get path to first unknown information field
                informationFieldNamePath = getFirstNullItemNamePath(information, infoClass);

                // don't request same information 2 times în X minutes
                final Message lastRequest = messageService.getLastMessageByInformationClassAndInformationFieldNamePath(CHATBOT_ID, user.getId(), infoClass, informationFieldNamePath);
                if (lastRequest != null && LocalDateTime.now().minusMinutes(MINUTES_TO_WAIT_TO_REQUEST_AGAIN_SAME_INFORMATION)
                        .isBefore(lastRequest.getDateTime())) {
                    continue;
                }

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

//        final PathAndKeys pathAndKeys = replaceMapKeysWithQuestionMark(informationFieldNamePath);// TODO task "PathAndKeys"
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

        final Sentence sentence = sentences.get(random.nextInt(sentences.size()));

        if (informationFieldNamePath.contains("coursesGrades")) {
            if (informationClass.equals(SchoolInformation.class) && user.getSchoolInformation() != null) {
                replaceQuestionMarkWithCourse(sentence, user.getSchoolInformation().getFavouriteCourse());
            }
            if (informationClass.equals(FacultyInformation.class) && user.getFacultyInformation() != null) {
                replaceQuestionMarkWithCourse(sentence, user.getFacultyInformation().getFavouriteCourse());
            }
            replaceQuestionMarkWithCourse(sentence, null);
        }
        return sentence;
    }

    /**
     * @param course if null, replace ? with a random course
     */
    private void replaceQuestionMarkWithCourse(final Sentence sentence, final String course) {
        final Word lastWord = sentence.getWords().get(sentence.getWords().size() - 1);
        if (!lastWord.getText().equals("?")) {
            return;
        }

        if (course != null) {
            sentence.getWords().set(sentence.getWords().size() - 1, getOrAddWordInDb(course, null));
            return;
        }

        final List<String> courses = new ArrayList<>();
        courses.add("limba română");
        courses.add("matematică");
        courses.add("istorie");
        courses.add("geografie");
        courses.add("biologie");
        courses.add("fizică");
        courses.add("informatică");
        final Word word = getOrAddWordInDb(courses.get(random.nextInt(courses.size())), null);
        sentence.getWords().set(sentence.getWords().size() - 1, word);
        sentenceRepository.save(sentence); // TODO: MAKE A NEW SENTENCE
    }

    // TODO task "PathAndKeys"
    private PathAndKeys replaceMapKeysWithQuestionMark(final String informationFieldNamePath) {
        final StringBuffer newPath = new StringBuffer();
        final List<String> keys = new ArrayList<>();
        for (String subPath : informationFieldNamePath.split("#")) {
            newPath.append(subPath).append("#?");
            // TODO task "PathAndKeys": save keys
        }
        newPath.deleteCharAt(newPath.length() - 1);
        newPath.deleteCharAt(newPath.length() - 1);
        return new PathAndKeys();
    }

    private Boolean probablyAtFaculty(final User user) {
        final SimpleDate birthDay = user.getPersonalInformation().getBirthDay();
        if (birthDay == null || birthDay.getYear() == null) {
            return null;
        }
        return LocalDate.now().getYear() - birthDay.getYear() >= 19;
    }

    private boolean isAtFacultyUnknown(final User user) {
        return user.getFacultyInformation() == null || user.getFacultyInformation().getIsAtFaculty() == null;
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
                    if (information instanceof FreeTimeInformation) {
                        final Boolean likeReading = ((FreeTimeInformation)information).getLikeReading();
                        if (likeReading != null && !likeReading && (fieldName.equals("favouriteBook") || fieldName.equals("currentReadingBook"))) {
                            continue;
                        }
                        final Boolean likeVideoGames = ((FreeTimeInformation)information).getLikeVideoGames();
                        if (likeVideoGames != null && !likeVideoGames && (fieldName.equals("favouriteVideoGame") || fieldName.equals("currentPlayedGame"))) {
                            continue;
                        }
                        final Boolean likeBoardGames = ((FreeTimeInformation)information).getLikeBoardGames();
                        if (likeBoardGames != null && !likeBoardGames && (fieldName.equals("favouriteBoardGame") || fieldName.equals("currentBoardGame"))) {
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

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class PathAndKeys {
        String informationFieldNamePath;
        List<String> keys;
    }
}
