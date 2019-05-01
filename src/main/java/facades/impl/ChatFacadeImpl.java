package facades.impl;

import dtos.MessageDto;
import dtos.RequestSendMessageDto;
import facades.api.ChatFacade;
import mappers.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import services.api.ChatService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatFacadeImpl implements ChatFacade {
    private final ChatService chatService;

    @Autowired
    public ChatFacadeImpl(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public MessageDto addMessage(final RequestSendMessageDto requestSendMessageDto) {
        return MessageMapper.messageToMessageDto(
                chatService.addMessageAndGetResponse(requestSendMessageDto.getMessage(), requestSendMessageDto.getFromUserId(), requestSendMessageDto.getToUserId()));
    }

    @Override
    public List<MessageDto> getMessages(final Long userId1, final Long userId2) {
        return chatService.getMessages(userId1, userId2).stream()
                .map(MessageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public MessageDto requestMessageFromChatbot(final Long userId) {
        return MessageMapper.messageToMessageDto(chatService.requestMessageFromChatbot(userId));
    }
}
