package mappers;

import domain.entities.Message;
import domain.entities.User;
import dtos.MessageDto;
import dtos.RequestAddMessageDto;

import java.time.LocalDateTime;

public class MessageMapper {
    public static Message requestAddMessageDtoToMessage(final RequestAddMessageDto requestAddMessageDto, final User fromUser,
                                                        final User toUser, final LocalDateTime dateTime) {
        final Message message = new Message();
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        message.setMessage(requestAddMessageDto.getMessage());
        message.setDateTime(dateTime);
        return message;
    }

    public static MessageDto messageToMessageDto(final Message message) {
        final MessageDto messageDto = new MessageDto();
        messageDto.setFromUserId(message.getFromUser().getId());
        messageDto.setToUserId(message.getToUser().getId());
        messageDto.setMessage(message.getMessage());
        messageDto.setDateTime(message.getDateTime());
        return messageDto;
    }
}
