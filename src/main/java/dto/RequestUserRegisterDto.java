package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestUserRegisterDto {
    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    @Size(min = 3, max = 255)
    private String password;

    @NotEmpty
    @Size(min = 2, max = 255)
    private String firstName;

    @Size(min = 2, max = 255)
    private String surname;

    private LocalDate birthDay;
}
