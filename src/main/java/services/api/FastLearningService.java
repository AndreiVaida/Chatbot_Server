package services.api;

import dtos.MessageDto;

import java.util.List;

public interface FastLearningService {
    void addKnowledgeToChatbotIfRequested();

    void addMessages(final List<MessageDto> messageDtos);
}
