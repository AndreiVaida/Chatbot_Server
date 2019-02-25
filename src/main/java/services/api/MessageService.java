package services.api;

import dtos.MessageDto;
import dtos.RequestAddMessageDto;

import java.util.List;

public interface MessageService {
    MessageDto addMessage(final RequestAddMessageDto requestAddMessageDto);

    List<MessageDto> getMessages(final Long userId1, Long userId2);
}
