package services.impl;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import domain.enums.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import repositories.MessageRepository;
import services.api.MessageService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
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
}
