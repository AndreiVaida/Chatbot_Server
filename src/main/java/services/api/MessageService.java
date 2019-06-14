package services.api;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import domain.enums.MessageSource;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {
    Message addMessage(final String text, final User fromUser, final User toUser, final Sentence equivalentSentence, MessageSource messageSource);

    List<Message> getMessagesByUsers(final Long userId1, final Long userId2);

    /**
     * @param maxDateTime max date of a message
     * @param nrOfMessages number of messages to return before given date
     * @return lat <nrOfMessages> messages before <maxDateTime> (including maxDateTime)
     */
    List<Message> getMessagesByUsers(final Long userId1, final Long userId2, final LocalDateTime maxDateTime, final Integer nrOfMessages);

    /**
     * @return the first message before given message or <null> if there is no previous message
     */
    Message getPreviousMessage(final Long userId_1, final Long userId_2, final Long lastMessageId);

    /**
     * @return the last message sent by fromUser to toUser or <null> if there is no message
     */
    Message getLastMessage(final Long fromUserId, final Long toUserId);
}
