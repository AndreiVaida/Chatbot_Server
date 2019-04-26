package services.api;

import domain.entities.Message;

import java.util.List;

public interface ChatService {
    /**
     * @param fromUserId - human user (non null and non 0)
     * @param toUserId - human user or chatbot (for chatbot let this field null or 0)
     */
    Message addMessage(final String text, final Long fromUserId, Long toUserId);

    /**
     * @param userId1 - human user (non null and non 0)
     * @param userId2 - human user or chatbot (for chatbot let this field null or 0)
     */
    List<Message> getMessages(final Long userId1, Long userId2);

    /**
     * @return a random message
     */
    Message requestMessageFromChatbot(final Long userId);
}
