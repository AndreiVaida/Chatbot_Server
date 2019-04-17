package services.impl;

import domain.entities.Sentence;
import domain.entities.Word;
import org.springframework.stereotype.Service;
import repositories.SentenceRepository;
import services.api.ChatbotService;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {
    private final SentenceRepository sentenceRepository;
    private final Random random;

    public ChatbotServiceImpl(SentenceRepository sentenceRepository) {
        this.sentenceRepository = sentenceRepository;
        random = new Random();
    }

    /**
     * @return best response for the provided text message or <null> if no response can be given
     */
    @Override
    @Transactional
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
     */
    private Sentence identifySentence(final String text) {
        // find those sentences that contains the words from the given text
        final List<String> words = Arrays.asList(text.split("\\P{L}+"));
        final int[] bestMatchedCount = {0};
        final List<Sentence> matchedSentences = sentenceRepository.findAll().stream()
                .filter(sentence -> {
                    final List<String> sentenceWords = sentence.getWords().stream().map(Word::getText).collect(Collectors.toList());
                    return getNrOfPointsOfMatchedWords(sentenceWords, words) > sentenceWords.size() / 2;
                })
                .sorted((Sentence sentence1, Sentence sentence2) -> {
                    final List<String> sentence1Words = sentence1.getWords().stream().map(Word::getText).collect(Collectors.toList());
                    final List<String> sentence2Words = sentence2.getWords().stream().map(Word::getText).collect(Collectors.toList());
                    final Integer nrOfMatchedWordsS1 = getNrOfPointsOfMatchedWords(sentence1Words, words);
                    final Integer nrOfMatchedWordsS2 = getNrOfPointsOfMatchedWords(sentence2Words, words);

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
        if (bestMatchedCount[0] < words.size() / 2) {
            return null;
        }
        return matchedSentences.get(0);
    }

    private int getNrOfPointsOfMatchedWords(final List<String> list1, final List<String> list2) {
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
    @Transactional
    public String pickRandomSentence() {
        final long nrOfSentences = sentenceRepository.count();
        if (nrOfSentences == 0) {
            return "Salut ! Tocmai m-am născut !";
        }

        final Sentence sentence = sentenceRepository.findAll().get(random.nextInt((int) nrOfSentences));
        return translateSentenceToText(sentence);
    }
}
