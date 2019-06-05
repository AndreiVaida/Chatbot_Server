package services.api;

import domain.entities.Message;
import domain.enums.ChatbotRequestType;
import domain.enums.MessageSource;

import java.util.List;

import static domain.enums.ChatbotRequestType.GET_INFORMATION_FROM_USER;

public interface ChatService {
    ChatbotRequestType CHATBOT_REQUEST_TYPE = GET_INFORMATION_FROM_USER;

    /**
     * @param fromUserId - human user (non null and non 0)
     * @param toUserId   - human user or chatbot (for chatbot let this field null or 0)
     * @return added message
     */
    Message addMessage(final String text, final Long fromUserId, Long toUserId, final MessageSource messageSource);

    /**
     * @param fromUserId - human user (non null and non 0)
     * @param toUserId   - human user or chatbot (for chatbot let this field null or 0)
     * @return an appropriate response or a random one if the chatbot does't know to respond
     */
    Message addMessageAndGetResponse(final String text, final Long fromUserId, Long toUserId);

    /**
     * @param userId1 - human user (non null and non 0)
     * @param userId2 - human user or chatbot (for chatbot let this field null or 0)
     */
    List<Message> getMessages(final Long userId1, Long userId2);

    /**
     * @param chatbotRequestType may be null => by default is used the global variable CHATBOT_REQUEST_TYPE
     * @return a random message according to CHATBOT_REQUEST_TYPE
     * If CHATBOT_REQUEST_TYPE == LEARN_TO_SPEAK return a message with zero or few replies
     * If CHATBOT_REQUEST_TYPE == GET_INFORMATION_FROM_USER return a message requesting a information
     * If CHATBOT_REQUEST_TYPE == RANDOM return a random message
     */
    Message requestMessageFromChatbot(final Long userId, ChatbotRequestType chatbotRequestType);
}
