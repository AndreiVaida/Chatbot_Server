package domain.information;

import domain.entities.Address;
import domain.entities.SimpleDate;
import domain.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "PERSONAL_INFORMATION")
public class PersonalInformation implements Information {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "SURNAME")
    private String surname;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "BIRTH_DAY")
    private SimpleDate birthDay;

    @Column(name = "GENDER")
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "HOME_ADDRESS")
    private Address homeAddress;

    @Override
    public List<String> getFieldNamesInImportanceOrder() {
        final List<String> fieldNamesInImportanceOrder = new ArrayList<>();
        fieldNamesInImportanceOrder.add("firstName");
        fieldNamesInImportanceOrder.add("surname");
        fieldNamesInImportanceOrder.add("birthDay");
        fieldNamesInImportanceOrder.add("gender");
        fieldNamesInImportanceOrder.add("homeAddress");
        return fieldNamesInImportanceOrder;
    }
}
