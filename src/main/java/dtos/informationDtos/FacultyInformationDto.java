package dtos.informationDtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FacultyInformationDto implements InformationDto {
    private Boolean isAtFaculty;
    private String facultyName;
    private String facultySpecialization;
    private String facultyYear;
    private String facultyGroup;
    private String favouriteCourse;
    private String favouriteProfessor;
    private String bestFriend;
    private Map<String, Integer> coursesGrades = new HashMap<>();
}
