package services.impl;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import domain.enums.MessageSource;
import domain.information.Information;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import repositories.MessageRepository;
import services.api.MessageService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public Message addMessage(final String text, final User fromUser, final User toUser, final Sentence equivalentSentence, MessageSource messageSource) {
        final Message message = new Message();
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        message.setText(text);
        message.setDateTime(LocalDateTime.now());
        message.setEquivalentSentence(equivalentSentence);
        message.setMessageSource(messageSource);

        messageRepository.save(message);
        return message;
    }

    @Override
    public List<Message> getMessagesByUsers(final Long userId1, final Long userId2) {
        return messageRepository.findAllByUsers(userId1, userId2);
    }

    @Override
    public List<Message> getMessagesByUsers(final Long userId1, final Long userId2, final LocalDateTime maxDateTime, final Integer nrOfMessages) {
        return messageRepository.findAllByUsers(userId1, userId2, maxDateTime, PageRequest.of(0, nrOfMessages));
    }

    @Override
    public Message getPreviousMessage(final Long userId_1, final Long userId_2, final Long lastMessageId) {
        final List<Message> previousMessages = messageRepository.getPreviousMessages(userId_1, userId_2, lastMessageId, PageRequest.of(0, 1));
        if (!previousMessages.isEmpty()) {
            return previousMessages.get(0);
        }
        return null;
    }

    @Override
    public Message getLastMessage(final Long fromUserId, final Long toUserId) {
        final List<Message> previousMessages = messageRepository.getAllByFromUser_IdAndToUser_IdOrderByDateTimeDesc(fromUserId, toUserId, PageRequest.of(0, 1));
        if (!previousMessages.isEmpty()) {
            return previousMessages.get(0);
        }
        return null;
    }

    @Override
    public Message getLastMessageByInformationClassAndInformationFieldNamePath(final Long fromUserId, final Long toUserId, final Class<Information> informationClass, final String informationFieldNamePath) {
        final List<Message> previousMessages = messageRepository.getAllByFromUser_IdAndToUser_IdAndEquivalentSentence_InformationClassAndEquivalentSentence_InformationFieldNamePathOrderByDateTimeDesc(
                fromUserId, toUserId, informationClass, informationFieldNamePath, PageRequest.of(0, 1));
        if (!previousMessages.isEmpty()) {
            return previousMessages.get(0);
        }
        return null;
    }

    @Override
    public Message getLastMessageOfUsers(final Long user1Id, final Long user2Id) {
        final Message messageFromUser1 = getLastMessage(user1Id, user2Id);
        final Message messageFromUser2 = getLastMessage(user2Id, user1Id);

        if (messageFromUser1 == null && messageFromUser2 == null) {
            return null;
        }
        if (messageFromUser1 != null && messageFromUser2 == null) {
            return messageFromUser1;
        }
        if (messageFromUser1 == null && messageFromUser2 != null) {
            return messageFromUser2;
        }
        if (messageFromUser1.getDateTime().isAfter(messageFromUser2.getDateTime())) {
            return messageFromUser1;
        }
        return messageFromUser2;
    }
}
