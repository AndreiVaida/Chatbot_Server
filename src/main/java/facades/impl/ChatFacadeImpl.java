package facades.impl;

import domain.entities.ResponseMessageAndInformation;
import domain.enums.ChatbotRequestType;
import dtos.MessageDto;
import dtos.RequestSendMessageDto;
import dtos.ResponseMessageAndInformationDto;
import facades.api.ChatFacade;
import mappers.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import services.api.ChatService;

import java.time.LocalDateTime;
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
    public ResponseMessageAndInformationDto addMessage(final RequestSendMessageDto requestSendMessageDto) {
        final ResponseMessageAndInformation responseMessageAndInformation = chatService.addMessageAndIdentifyInformationAndGetResponse(requestSendMessageDto.getMessage(), requestSendMessageDto.getFromUserId(), requestSendMessageDto.getToUserId());
        final MessageDto messageDto = MessageMapper.messageToMessageDto(responseMessageAndInformation.getMessage());
        return new ResponseMessageAndInformationDto(messageDto, responseMessageAndInformation.getInformation());
    }

    @Override
    public List<MessageDto> getMessages(final Long userId1, final Long userId2) {
        return chatService.getMessages(userId1, userId2).stream()
                .map(MessageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDto> getMessages(Long userId1, Long userId2, LocalDateTime maxDateTime, Integer nrOfMessages) {
        return chatService.getMessages(userId1, userId2, maxDateTime, nrOfMessages).stream()
                .map(MessageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public MessageDto requestMessageFromChatbot(final Long userId, final ChatbotRequestType chatbotRequestType) {
        return MessageMapper.messageToMessageDto(chatService.requestMessageFromChatbot(userId, chatbotRequestType));
    }
}
