package services.api;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.information.Information;

import java.util.List;

public interface ChatbotService {
    /**
     * Create a new Sentence from given text.
     * Check if an equivalent Sentence is already stored in DB. More than half of the items should match (perfectly or by synonyms).
     * - If it matches perfectly (every word matched, at least with it's synonyms) then the existing sentence is considered equal with the new one.
     * The new sentence is not saved in DB. Return the existing sentence.
     * - If it matches imperfectly (some items haven't matched, not even their synonyms), then the new sentence is set as synonym for the existing sentence (and vice-versa).
     * The new sentence is saved in DB. Return the new sentence.
     * For every word matched by a synonym, we increase it's synonym frequency.
     *
     * @param text - the non-empty text of the new sentence
     * @return the sentence assigned to given text
     */
    Sentence getSentence(final String text);

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

    String translateSentenceToText(final Sentence sentence);

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
}
