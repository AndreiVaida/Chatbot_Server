package domain.information;

import domain.entities.Address;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "PERSONAL_INFORMATION")
public class PersonalInformation implements Information {
    @Id
    @Column
    private Long id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "BIRTH_DAY")
    private LocalDate birthDay;

    @Column(name = "GENDER")
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HOME_ADDRESS")
    private Address homeAddress;
}
