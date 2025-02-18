package dtos;

import domain.enums.MessageSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageDto {
    private Long fromUserId;
    private Long toUserId;
    private String message;
    private LocalDateTime dateTime;
    private Boolean isUnknownMessage;
    private MessageSource messageSource;
}
