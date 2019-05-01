package services.api;

import domain.entities.Message;
import domain.enums.MessageSource;

import java.util.List;

public interface ChatService {
    /**
     * @param fromUserId - human user (non null and non 0)
     * @param toUserId - human user or chatbot (for chatbot let this field null or 0)
     * @return added message
     */
    Message addMessage(final String text, final Long fromUserId, Long toUserId, final MessageSource messageSource);

    /**
     * @param fromUserId - human user (non null and non 0)
     * @param toUserId - human user or chatbot (for chatbot let this field null or 0)
     * @return an appropriate response or a random one if the chatbot does't know to respond
     */
    Message addMessageAndGetResponse(final String text, final Long fromUserId, Long toUserId);

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
