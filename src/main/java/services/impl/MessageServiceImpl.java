package services.impl;

import domain.entities.Message;
import domain.entities.User;
import dtos.MessageDto;
import dtos.RequestAddMessageDto;
import mappers.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import repositories.MessageRepository;
import repositories.UserRepository;
import services.api.MessageService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final Long CHATBOT_ID;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository, Environment environment) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        CHATBOT_ID = Long.valueOf(environment.getProperty("chatbot.id"));
    }

    @Override
    @Transactional
    public MessageDto addMessage(final RequestAddMessageDto requestAddMessageDto) {
        if (requestAddMessageDto.getToUserId() == null) {
            requestAddMessageDto.setToUserId(CHATBOT_ID);
        }
        if (requestAddMessageDto.getToUserId() == null || requestAddMessageDto.getToUserId() == 0) {
            requestAddMessageDto.setToUserId(CHATBOT_ID);
        }
        final User fromUser = userRepository.findById(requestAddMessageDto.getFromUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        final User toUser = userRepository.findById(requestAddMessageDto.getToUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        final Message message = MessageMapper.requestAddMessageDtoToMessage(requestAddMessageDto, fromUser, toUser, LocalDateTime.now());
        messageRepository.save(message);
        return MessageMapper.messageToMessageDto(message);
    }

    @Override
    @Transactional
    public List<MessageDto> getMessages(final Long userId1, Long userId2) {
        if (!userRepository.existsById(userId1)) {
            throw new EntityNotFoundException("User 1 not found.");
        }
        if (userId2 == null || userId2 == 0) {
            userId2 = CHATBOT_ID;
        }
        if (!userRepository.existsById(userId2)) {
            throw new EntityNotFoundException("User 2 not found.");
        }

        final List<Message> messages = messageRepository.findAllByUsers(userId1, userId2);
        return messages.stream()
                .map(MessageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }
}
