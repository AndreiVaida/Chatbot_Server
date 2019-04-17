package services.api;

public interface ChatbotService {
    String generateResponse(final String text);

    String pickRandomSentence();
}
