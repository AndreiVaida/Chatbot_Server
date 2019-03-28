package services.api;

import dtos.MessageDto;
import dtos.RequestSendMessageDto;

import java.util.List;

public interface MessageService {
    MessageDto addMessage(final RequestSendMessageDto requestSendMessageDto);

    List<MessageDto> getMessages(final Long userId1, Long userId2);
}
