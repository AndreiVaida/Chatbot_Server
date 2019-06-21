package mappers;

import domain.entities.Message;
import domain.entities.User;
import dtos.MessageDto;
import dtos.RequestSendMessageDto;

import java.time.LocalDateTime;

public class MessageMapper {
    public static Message requestAddMessageDtoToMessage(final RequestSendMessageDto requestSendMessageDto, final User fromUser,
                                                        final User toUser, final LocalDateTime dateTime) {
        final Message message = new Message();
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        message.setText(requestSendMessageDto.getMessage());
        message.setDateTime(dateTime);
        return message;
    }

    public static MessageDto messageToMessageDto(final Message message) {
        final MessageDto messageDto = new MessageDto();
        messageDto.setFromUserId(message.getFromUser().getId());
        if (message.getToUser() != null) {
            messageDto.setToUserId(message.getToUser().getId());
        } else {
            messageDto.setToUserId(null);
        }
        messageDto.setMessage(message.getText());
        messageDto.setDateTime(message.getDateTime());
        messageDto.setIsUnknownMessage(message.getIsUnknownMessage());
        return messageDto;
    }
}
