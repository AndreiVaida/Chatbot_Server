package facades.impl;

import dtos.MessageDto;
import dtos.RequestSendMessageDto;
import facades.api.MessageFacade;
import mappers.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import services.api.MessageService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageFacadeImpl implements MessageFacade {
    private final MessageService messageService;

    @Autowired
    public MessageFacadeImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public MessageDto addMessage(final RequestSendMessageDto requestSendMessageDto) {
        return MessageMapper.messageToMessageDto(
                messageService.addMessage(requestSendMessageDto.getMessage(), requestSendMessageDto.getFromUserId(), requestSendMessageDto.getToUserId()));
    }

    @Override
    public List<MessageDto> getMessages(final Long userId1, final Long userId2) {
        return messageService.getMessages(userId1, userId2).stream()
                .map(MessageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public MessageDto requestMessageFromChatbot(final Long userId) {
        return MessageMapper.messageToMessageDto(messageService.requestMessageFromChatbot(userId));
    }
}
