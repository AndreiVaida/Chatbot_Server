package dtos.informationDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RelationshipInformationDto implements InformationDto {
    //private PersonalInformationDto motherPersonalInformation;
    //private PersonalInformationDto fatherPersonalInformation;

    private Integer numberOfBrothersAndSisters;
    //private Map<String, PersonalInformationDto> brothersAndSistersPersonalInformation = new HashMap<>();

    private Integer numberOfGrandparents;
    //private Map<String, PersonalInformationDto> grandparentsPersonalInformation = new HashMap<>();

    //private PersonalInformationDto wifeOrHusbandInformation;

    private Integer numberOfKids;
    //private Map<String, PersonalInformationDto> kidsPersonalInformation = new HashMap<>();
}
