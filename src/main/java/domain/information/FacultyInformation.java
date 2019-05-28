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
    @Column
    private Long id;

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
}
