package facades.api;

import dtos.MessageDto;
import dtos.RequestSendMessageDto;

import java.util.List;

public interface MessageFacade {
    MessageDto addMessage(final RequestSendMessageDto requestSendMessageDto);

    List<MessageDto> getMessages(final Long userId1, Long userId2);

    MessageDto requestMessageFromChatbot(final Long userId);
}
