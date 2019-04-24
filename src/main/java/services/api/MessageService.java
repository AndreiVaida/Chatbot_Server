package services.api;

import domain.entities.Message;
import domain.entities.User;

import java.util.List;

public interface MessageService {
    Message addMessage(final String text, final User fromUser, final User toUser);

    List<Message> getMessagesByUsers(final Long userId1, final Long userId2);

    /**
     * @return the first message before given message or <null> if there is no previous message
     */
    Message getPreviousMessage(final Long userId_1, final Long userId_2, final Long lastMessageId);
}
