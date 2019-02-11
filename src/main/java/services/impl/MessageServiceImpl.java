package services.impl;

import domain.entities.Message;
import domain.entities.User;
import dtos.MessageDto;
import dtos.RequestAddMessageDto;
import mappers.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private MessageRepository messageRepository;
    private UserRepository userRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void addMessage(final RequestAddMessageDto requestAddMessageDto) {
        final User fromUser = userRepository.findById(requestAddMessageDto.getFromUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        final User toUser = userRepository.findById(requestAddMessageDto.getFromUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        final Message message = MessageMapper.requestAddMessageDtoToMessage(requestAddMessageDto, fromUser, toUser, LocalDateTime.now());
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public List<MessageDto> getMessages(final Long userId1, final Long userId2) {
        final User fromUser = userRepository.findById(userId1)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        final User toUser = userRepository.findById(userId2)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        final List<Message> messages = messageRepository.findAllByFromUser_IdAndToUser_IdOrToUser_IdAndFromUser_IdOrderByDateTime(userId1, userId2, userId2, userId1);
        return messages.stream()
                .map(MessageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }
}
