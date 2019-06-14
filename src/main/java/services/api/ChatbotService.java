package services.api;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import domain.entities.Word;
import domain.enums.AddressingMode;
import domain.information.Information;

import java.time.LocalTime;
import java.util.List;

public interface ChatbotService {
    LocalTime endOfMorning = LocalTime.of(11, 0, 0);
    LocalTime startOfEvening = LocalTime.of(18, 0, 0);

    /**
     * @param text - the text for which is intended to get a sentence
     * @return the corresponding sentence for text: an existing sentence if already exists in DB or a new one (will be also saved in DB)
     */
    Sentence getExistingSentenceOrCreateANewOne(final String text);

    List<Sentence> findSimilarSentencesByWords(final List<Word> words, final int maxNrOfExtraWords, final int maxNrOfUnmatchedWords, final double weight);
    /**
     * Adds sentence as a response for previousSentence. If it is already a response, it's incremented its frequency.
     */
    void addResponse(final Sentence previousSentence, final Sentence sentence);

    /**
     * Adds sentence as a response for previousSentence. If it is already a response, it's incremented its frequency.
     * If both sentences are responses for each other, they are set as synonyms for each other.
     */
    void addResponseAndSynonym(final Sentence previousSentence, final Sentence sentence);

    /**
     * @return best response for the provided message or <null> if no response can be given
     */
    Sentence generateResponse(final Message message);

    String translateSentenceToText(final Sentence sentence, final AddressingMode addressingMode);

    /**
     * @return random sentence
     * If no sentences are stored in DB, we create a new one with the text „Salut”.
     */
    Sentence pickRandomSentence();

    /**
     * @return a sentence which has the least replies
     * If no sentences are stored in DB, we create a new one with the text „Salut”
     */
    Sentence pickSentenceWithFewReplies();

    /**
     * @param user - the user for whom new information is desired
     * @return a DIRECTIVE requesting a new information for the given user
     * If all the user information is known, then return a random directive.
     */
    Sentence pickSentenceRequestingInformation(final User user);
}
