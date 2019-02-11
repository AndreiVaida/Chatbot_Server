package services.api;

import dtos.MessageDto;
import dtos.RequestAddMessageDto;

import java.util.List;

public interface MessageService {
    void addMessage(final RequestAddMessageDto requestAddMessageDto);

    List<MessageDto> getMessagesForUsers(final Long userId1, final Long userId2);
}
