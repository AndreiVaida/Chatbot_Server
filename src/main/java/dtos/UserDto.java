package dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String surname;
    private SimpleDateDto birthDay;
    private byte[] profilePicture;
    private boolean isAdmin;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", birthDay=" + birthDay +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
