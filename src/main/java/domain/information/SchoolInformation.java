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
@Table(name = "SCHOOL_INFORMATION")
public class SchoolInformation implements Information {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "IS_AT_SCHOOL")
    private Boolean isAtSchool;

    @Column(name = "SCHOOL_NAME")
    private String schoolName;

    @Column(name = "SCHOOL_PROFILE")
    private String schoolProfile;

    @Column(name = "SCHOOL_CLASS")
    private String schoolClass;

    @Column(name = "FAVOURITE_COURSE")
    private String favouriteCourse;

    @Column(name = "FAVOURITE_PROFESSOR")
    private String favouriteProfessor;

    @Column(name = "BEST_FRIEND")
    private String bestFriend;

    @ElementCollection
    @CollectionTable(name="SCHOOL_COURSES_GRADES")
    @MapKeyJoinColumn(name="COURSE")
    @Column(name="GRADE")
    private Map<String,Integer> coursesGrades = new HashMap<>();

    @Override
    public List<String> getFieldNamesInImportanceOrder() {
        final List<String> fieldNamesInImportanceOrder = new ArrayList<>();
        fieldNamesInImportanceOrder.add("isAtSchool");
        fieldNamesInImportanceOrder.add("schoolName");
        fieldNamesInImportanceOrder.add("schoolProfile");
        fieldNamesInImportanceOrder.add("schoolClass");
        fieldNamesInImportanceOrder.add("favouriteCourse");
        fieldNamesInImportanceOrder.add("favouriteProfessor");
        fieldNamesInImportanceOrder.add("bestFriend");
        fieldNamesInImportanceOrder.add("coursesGrades");
        return fieldNamesInImportanceOrder;
    }
}
