package domain.entities;

import domain.information.FacultyInformation;
import domain.information.FreeTimeInformation;
import domain.information.PersonalInformation;
import domain.information.RelationshipsInformation;
import domain.information.SchoolInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import static javax.persistence.FetchType.LAZY;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "USERS")
public class User {
    // ACCOUNT DATA
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "EMAIL", unique = true)
    private String email;

    @NotNull
    @Column(name = "PASSWORD")
    private String password;

    // PERSONAL INFORMATION
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PERSONAL_INFORMATION")
    private PersonalInformation personalInformation;

    // RELATIONSHIPS
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "RELATIONSHIPS_INFORMATION")
    private RelationshipsInformation relationshipsInformation;

    // EDUCATION
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "SCHOOL_INFORMATION")
    private SchoolInformation schoolInformation;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "FACULTY_INFORMATION")
    private FacultyInformation facultyInformation;

    // FREE TIME
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "FREE_TIME_INFORMATION")
    private FreeTimeInformation freeTimeInformation;

    // FOOD
    @Column(name = "FAVOURITE_FOOD")
    private String favouriteFood; // TODO: GASTRONOMY INFORMATION

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "ADDRESSING_MODE_STATUS")
    private AddressingModeStatus addressingModeStatus = new AddressingModeStatus();

    @Column(name = "CONTENT")
    @Basic(fetch = LAZY)
    @Lob
    private byte[] profilePicture;

    @Column(name = "IS_ADMINISTRATOR")
    private Boolean isAdministrator = false;

    public User(Long id, String email, String password, String firstName, String surname, SimpleDate birthDay) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.personalInformation = new PersonalInformation();
        this.personalInformation.setFirstName(firstName);
        this.personalInformation.setSurname(surname);
        this.personalInformation.setBirthDay(birthDay);
    }
}
