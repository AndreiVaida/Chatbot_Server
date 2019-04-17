package services.api;

import domain.entities.Message;
import domain.entities.User;

import java.util.List;

public interface MessageService {
    Message addMessage(final String text, final User fromUser, final User toUser);

    List<Message> getMessagesByUsers(final Long userId1, final Long userId2);
}
