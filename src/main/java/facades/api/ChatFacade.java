package facades.api;

import domain.enums.ChatbotRequestType;
import dtos.MessageDto;
import dtos.RequestSendMessageDto;

import java.util.List;

public interface ChatFacade {
    MessageDto addMessage(final RequestSendMessageDto requestSendMessageDto);

    List<MessageDto> getMessages(final Long userId1, final Long userId2);

    MessageDto requestMessageFromChatbot(final Long userId, final ChatbotRequestType chatbotRequestType);
}
