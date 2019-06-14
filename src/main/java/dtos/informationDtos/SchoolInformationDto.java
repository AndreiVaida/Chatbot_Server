package dtos.informationDtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SchoolInformationDto implements InformationDto {
    private Boolean isAtSchool;
    private String schoolName;
    private String schoolProfile;
    private String schoolClass;
    private String favouriteCourse;
    private String favouriteProfessor;
    private String bestFriend;
    private Map<String,Integer> coursesGrades = new HashMap<>();
}
