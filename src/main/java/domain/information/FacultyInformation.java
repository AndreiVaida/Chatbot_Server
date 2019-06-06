package domain.information;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "FACULTY_INFORMATION")
public class FacultyInformation implements Information {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "IS_AT_FACULTY")
    private Boolean isAtFaculty;

    @Column(name = "FACULTY_NAME")
    private String facultyName;

    @Column(name = "FACULTY_SPECIALIZATION")
    private String facultySpecialization;

    @Column(name = "FACULTY_YEAR")
    private String facultyYear;

    @Column(name = "FACULTY_GROUP")
    private String facultyGroup;

    @Column(name = "FAVOURITE_COURSE")
    private String favouriteCourse;

    @Column(name = "FAVOURITE_PROFESSOR")
    private String favouriteProfessor;

    @Column(name = "BEST_FRIEND")
    private String bestFriend;

    @ElementCollection
    @CollectionTable(name="FACULTY_COURSES_GRADES")
    @MapKeyJoinColumn(name="COURSE")
    @Column(name="GRADE")
    private Map<String, Integer> coursesGrades = new HashMap<>();

    @Override
    public List<String> getFieldNamesInImportanceOrder() {
        final List<String> fieldNamesInImportanceOrder = new ArrayList<>();
        fieldNamesInImportanceOrder.add("isAtFaculty");
        fieldNamesInImportanceOrder.add("facultyName");
        fieldNamesInImportanceOrder.add("facultySpecialization");
        fieldNamesInImportanceOrder.add("facultyYear");
        fieldNamesInImportanceOrder.add("facultyGroup");
        fieldNamesInImportanceOrder.add("favouriteCourse");
        fieldNamesInImportanceOrder.add("favouriteProfessor");
        fieldNamesInImportanceOrder.add("bestFriend");
        fieldNamesInImportanceOrder.add("coursesGrades");
        return fieldNamesInImportanceOrder;
    }
}
