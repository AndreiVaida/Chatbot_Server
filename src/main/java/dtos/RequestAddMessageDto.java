package dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestAddMessageDto {
    @NonNull
    private String message;

    @NonNull
    private Long fromUserId;

    private Long toUserId;

}
