package dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RequestUserRegisterDto {
    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    @Size(min = 2, max = 255)
    private String password;

    @Size(min = 2, max = 255)
    @NotEmpty
    private String firstName;
}
