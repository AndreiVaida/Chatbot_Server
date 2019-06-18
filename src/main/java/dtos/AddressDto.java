package dtos;

import domain.enums.LocalityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddressDto {
    private String planet;
    private String continent;
    private String country;
    private String county;
    private String locality;
    private LocalityType localityType;
    private String neighborhood;
    private String street;
    private Integer streetNumber;
    private Integer floor;
    private Integer apartmentNumber;
}
