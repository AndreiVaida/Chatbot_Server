package services.api;

import domain.entities.Message;

import java.util.List;

public interface MessageService {
    Message addMessage(final String message, final Long fromUserId, final Long toUserId);

    List<Message> getMessages(final Long userId1, Long userId2);

    Message requestMessageFromChatbot(final Long userId);

    Message getMessageById(final Long id);
}
