package services.api;

import domain.entities.Message;
import domain.entities.Sentence;

public interface ChatbotService {
    /**
     * Create a new Sentence from given text.
     * Check if an equivalent Sentence is already stored in DB. More than half of the words should match (perfectly or by synonyms).
     * - If it matches perfectly (every word matched, at least with it's synonyms) then the existing sentence is considered equal with the new one.
     * The new sentence is not saved in DB. Return the existing sentence.
     * - If it matches imperfectly (some words haven't matched, not even their synonyms), then the new sentence is set as synonym for the existing sentence (and vice-versa).
     * The new sentence is saved in DB. Return the new sentence.
     * For every word matched by a synonym, we increase it's synonym frequency.
     * @param text - the text of the new sentence
     * @return the new created
     */
    Sentence addSentence(final String text);

    void addAnswer(final Message previousMessage, final Message message);

    String generateResponse(final String text);

    String pickRandomSentence();
}
