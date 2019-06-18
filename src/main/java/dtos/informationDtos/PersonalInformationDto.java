package dtos.informationDtos;

import domain.enums.Gender;
import dtos.AddressDto;
import dtos.SimpleDateDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PersonalInformationDto implements InformationDto {
    private String firstName;
    private String surname;
    private SimpleDateDto birthDay;
    private Gender gender;
    private AddressDto homeAddress;
}
