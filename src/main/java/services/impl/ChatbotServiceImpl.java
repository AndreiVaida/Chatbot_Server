package services.impl;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.Word;
import org.springframework.stereotype.Service;
import repositories.SentenceRepository;
import repositories.WordRepository;
import services.api.ChatbotService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static domain.enums.SentenceType.QUESTION;
import static domain.enums.SentenceType.STATEMENT;

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private final SentenceRepository sentenceRepository;
    private final WordRepository wordRepository;
    private final Random random;
    private final String wordsSplitRegex; // TODO: change regex with a custom function which consider also the signs as words (, . ...)

    public ChatbotServiceImpl(SentenceRepository sentenceRepository, WordRepository wordRepository) {
        this.sentenceRepository = sentenceRepository;
        this.wordRepository = wordRepository;
        random = new Random();
        wordsSplitRegex = "[\\s,;.]+";
    }

    @Override
    public Sentence getSentence(final String text) {
        final String[] words = text.split(wordsSplitRegex);

        // create a new sentence from the given text
        final List<Word> sentenceWords = new ArrayList<>();
        for (String word : words) {
            // check if the word already exists in DB - if not: create a new one
            final Word existingWord = wordRepository.getByTextIgnoreCase(word);
            if (existingWord != null) {
                sentenceWords.add(existingWord);
            } else {
                final Word newWord = new Word();
                newWord.setText(word);
                wordRepository.save(newWord);
                sentenceWords.add(newWord);
            }
        }
        final Sentence newSentence = new Sentence();
        newSentence.setWords(sentenceWords);
        if (sentenceWords.stream().anyMatch(word -> word.getText().contains("?"))) {
            newSentence.setSentenceType(QUESTION);
        } else {
            newSentence.setSentenceType(STATEMENT);
        }

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
            final String newSentenceWord = newSentence.getWords().get(i).getText();
            // check if the new word is equal with the existing word (on same position in sentences) or equal with a synonym of the existing word
            if (i < existingSentence.getWords().size()) {
                final String existingSentenceWord = existingSentence.getWords().get(i).getText();
                if (newSentenceWord.toLowerCase().equals(existingSentenceWord.toLowerCase())) {
                    nrOfMatchedWords++;
                    continue; // ok - matched the word
                }
                if (existingSentence.getWords().get(i).getSynonyms().keySet().stream()
                        .anyMatch(synonym -> synonym.getText().toLowerCase().equals(newSentenceWord.toLowerCase()))) {
                    nrOfMatchedWords++;
                    continue; // ok - matched a synonym
                }
            }
            if (existingSentence.getWords().stream().anyMatch(word -> word.getText().toLowerCase().equals(newSentenceWord.toLowerCase()))) {
                allWordsAreInTheSamePositions = false;
                nrOfMatchedWords++;
                continue; // ok - the word is somewhere else in the sentence
            }
            if (existingSentence.getWords().stream().anyMatch(existingWord -> existingWord.getSynonyms().keySet().stream()
                    .anyMatch(synonym -> synonym.getText().toLowerCase().equals(newSentenceWord.toLowerCase())))) {
                allWordsAreInTheSamePositions = false;
                nrOfMatchedWords++;
                continue; // ok - the word has a synonym somewhere else in the sentence
            }
        }

        final int minimumWordsToMatch = Math.min(newSentence.getWords().size(), existingSentence.getWords().size()) - wordCountDifference / 2;
        if (!allWordsAreInTheSamePositions || nrOfMatchedWords < minimumWordsToMatch) {
            // the sentences are different, but synonyms
            newSentence.addSynonym(existingSentence);
            sentenceRepository.save(newSentence);
            existingSentence.addSynonym(newSentence);
            sentenceRepository.save(existingSentence);
            return newSentence;
        }

        // the sentences are identically
        return existingSentence;
    }

    @Override
    public void addResponse(final Sentence previousSentence, final Sentence sentence) {
        previousSentence.addResponse(sentence);
        sentenceRepository.save(previousSentence);
    }

    @Override
    public Sentence generateResponse(final Message message) {
        final Sentence sentence = message.getEquivalentSentence();
        if (sentence == null) {
            return null;
        }

        Sentence response = pickBestResponseForSentence(sentence);
        if (response != null) {
            return response;
        }

        // check for a response in sentence synonyms
        final List<Sentence> orderedSynonyms = sentence.getSynonyms().keySet().stream()
                .sorted((synonym1, synonym2) -> sentence.getSynonyms().get(synonym2).compareTo(sentence.getSynonyms().get(synonym1)))
                .collect(Collectors.toList());
        for (Sentence synonym : orderedSynonyms) {
            response = pickBestResponseForSentence(synonym);
            if (response != null) {
                return response;
            }
        }

        return null;
    }

    @Override
    public String translateSentenceToText(final Sentence sentence) {
        final StringBuilder text = new StringBuilder();
        for (Word word : sentence.getWords()) {
            text.append(word.getText()).append(" ");
        }
        text.deleteCharAt(text.length() - 1);

        return text.toString();
    }

    /**
     * @return best response or <null> if no responses are available for the provided sentence
     */
    private Sentence pickBestResponseForSentence(final Sentence sentence) {
        if (sentence.getResponses().isEmpty()) {
            return null;
        }

        final List<Sentence> sortedSentences = sentence.getResponses().keySet().stream()
                .sorted((Sentence response1, Sentence response2) -> {
                    final Integer response1Frequency = sentence.getResponses().get(response1);
                    final Integer response2Frequency = sentence.getResponses().get(response2);
                    return response2Frequency.compareTo(response1Frequency);
                })
                .collect(Collectors.toList());
        return sortedSentences.get(0);
    }

    /**
     * @return best sentence identified (it should have at last half of words matched with the provided text) or <null> if no proper sentence is found
     * We search in every sentence's words and in sentence's words synonyms.
     */
    private Sentence identifySentence(final String text) {
        // find those sentences that contains the words from the given text
        final List<String> words = Arrays.asList(text.split(wordsSplitRegex));
        final int[] bestMatchedCount = {0};
        final List<Sentence> matchedSentences = sentenceRepository.findAll().stream()
                .filter(sentence -> {
                    // 1. check in sentence's own words
                    final List<String> sentenceWords = sentence.getWords().stream().map(Word::getText).collect(Collectors.toList());
                    final int nrOfMatchesSentenceWords = calculateNrOfMatches(sentenceWords, words);
                    if (nrOfMatchesSentenceWords > words.size() / 2) {
                        return true;
                    }
                    // 2. check in words's synonyms
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
                    return matchedWordsSynonyms.size() > words.size() / 2;
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

                    return nrOfMatchedWordsS2.compareTo(nrOfMatchedWordsS1);
                })
                .collect(Collectors.toList());

        // return the best sentence or <null> if we didn't find a good one
        if (matchedSentences.isEmpty() || bestMatchedCount[0] < words.size() / 2) {
            return null;
        }
        return matchedSentences.get(0);
    }

    /**
     * @return how many words from list1 are in list2
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

    private boolean partialContains(final List<String> words, final String wordToFind) {
        for (String word : words) {
            if (word.equals(wordToFind) || word.contains(wordToFind) || wordToFind.contains(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Sentence pickRandomSentence() {
        final long nrOfSentences = sentenceRepository.count();
        if (nrOfSentences == 0) {
            return generateDefaultSentence();
        }
        return sentenceRepository.findAll().get(random.nextInt((int) nrOfSentences));
    }

    private Sentence generateDefaultSentence() {
        final Word word = new Word();
        word.setText("Salut");
        wordRepository.save(word);
        final Sentence sentence = new Sentence();
        sentence.getWords().add(word);
        sentence.setSentenceType(STATEMENT);
        sentenceRepository.save(sentence);
        return sentence;
    }

    @Override
    public Sentence pickSentenceWithFewReplies() {
        final long nrOfSentences = sentenceRepository.count();
        if (nrOfSentences == 0) {
            return generateDefaultSentence();
        }
        return sentenceRepository.findAll().stream().min((sentence1, sentence2) -> {
            final Integer nrOfResponses_sentence1 = sentence1.getResponses().size();
            final Integer nrOfResponses_sentence2 = sentence2.getResponses().size();
            return nrOfResponses_sentence1.compareTo(nrOfResponses_sentence2);
        }).get();
    }
}
