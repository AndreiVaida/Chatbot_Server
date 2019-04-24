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

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private final SentenceRepository sentenceRepository;
    private final WordRepository wordRepository;
    private final Random random;
    private final String wordsSplitRegex;

    public ChatbotServiceImpl(SentenceRepository sentenceRepository, WordRepository wordRepository) {
        this.sentenceRepository = sentenceRepository;
        this.wordRepository = wordRepository;
        random = new Random();
        wordsSplitRegex = "\\P{L}+";
    }

    @Override
    public Sentence addSentence(final String text) {
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

        // compare the new sentence with the existing one
        final Sentence existingSentence = identifySentence(text);
        if (existingSentence == null || newSentence.getWords().size() != existingSentence.getWords().size()) {
            // no equivalent sentence found in DB
            sentenceRepository.save(newSentence);
            return newSentence;
        }
        for (int i = 0; i < newSentence.getWords().size(); i++) {
            final String newSentenceWord = newSentence.getWords().get(i).getText();
            final String existingSentenceWord = existingSentence.getWords().get(i).getText();
            // check if the new word is equal with the existing word (on same position in sentences) or equal with a synonym of the existing word
            if (newSentenceWord.toLowerCase().equals(existingSentenceWord.toLowerCase())) {

                continue; // ok
            }
            if (existingSentence.getWords().get(i).getSynonyms().keySet().stream()
                    .noneMatch(synonym -> synonym.getText().toLowerCase().equals(newSentenceWord.toLowerCase()))) {
                // the sentence found in DB has different words
                sentenceRepository.save(newSentence);
                return newSentence;
            }
        }

        // the existing sentence perfectly matches the new one
        for (Word existingWord : existingSentence.getWords()) {
            if (existingWord.get)
        }
    }

    @Override
    public void addAnswer(final Message previousMessage, final Message message) {
        final Sentence previousSentence = identifySentence(previousMessage.getText());
        previousSentence.
    }

    /**
     * @return best response for the provided text message or <null> if no response can be given
     */
    @Override
    public String generateResponse(final String text) {
        final Sentence sentence = identifySentence(text);
        if (sentence == null) {
            return null;
        }

        final Sentence responseSentence = pickBestResponseSentence(sentence);
        if (responseSentence == null) {
            return null;
        }

        return translateSentenceToText(responseSentence);
    }

    private String translateSentenceToText(final Sentence sentence) {
        final StringBuilder text = new StringBuilder();
        for (Word word : sentence.getWords()) {
            text.append(word.getText());
        }

        return text.toString();
    }

    /**
     * @return best response or <null> if no responses are available for the provided sentence
     */
    private Sentence pickBestResponseSentence(final Sentence sentence) {
        if (sentence.getResponses().isEmpty()) {
            return null;
        }

        final List<Sentence> sortedSentences = sentence.getResponses().keySet().stream()
                .sorted((Sentence response1, Sentence response2) -> {
                    return sentence.getResponses().get(response2).compareTo(sentence.getResponses().get(response1));
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
                    final Set<String> loweCaseWords = words.stream().map(String::toLowerCase).collect(Collectors.toSet());
                    for (Word sentenceWord : sentence.getWords()) {
                        final List<String> wordSynonyms = sentenceWord.getSynonyms().keySet().stream().map(Word::getText).map(String::toLowerCase).collect(Collectors.toList());
                        for (String synonym : wordSynonyms) {
                            if (loweCaseWords.contains(synonym)) {
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
    public String pickRandomSentence() {
        final long nrOfSentences = sentenceRepository.count();
        if (nrOfSentences == 0) {
            return "Salut ! Tocmai m-am născut !";
        }

        final Sentence sentence = sentenceRepository.findAll().get(random.nextInt((int) nrOfSentences));
        return translateSentenceToText(sentence);
    }
}
