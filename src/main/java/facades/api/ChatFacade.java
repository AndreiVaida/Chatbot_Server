package facades.api;

import domain.enums.ChatbotRequestType;
import dtos.MessageDto;
import dtos.RequestSendMessageDto;
import dtos.ResponseMessageAndInformationDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatFacade {
    ResponseMessageAndInformationDto addMessage(final RequestSendMessageDto requestSendMessageDto);

    MessageDto addMessageFromGuest(final String message);

    List<MessageDto> getMessages(final Long userId1, final Long userId2);

    List<MessageDto> getMessages(final Long userId1, final Long userId2, final LocalDateTime from, final boolean includeMaxMessage, final Integer nrOfMessages);

    MessageDto requestMessageFromChatbot(final Long userId, final ChatbotRequestType chatbotRequestType);
}
