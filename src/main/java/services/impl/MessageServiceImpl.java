package services.impl;

import domain.entities.Message;
import domain.entities.User;
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
    public Message addMessage(final String text, final User fromUser, final User toUser) {
        final Message message = new Message();
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        message.setText(text);
        message.setDateTime(LocalDateTime.now());

        messageRepository.save(message);
        return message;
    }

    @Override
    @Transactional
    public List<Message> getMessagesByUsers(final Long userId1, final Long userId2) {
        return messageRepository.findAllByUsers(userId1, userId2);
    }
}
