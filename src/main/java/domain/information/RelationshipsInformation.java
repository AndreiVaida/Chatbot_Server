package domain.information;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "RELATIONSHIP_INFORMATION")
public class RelationshipsInformation implements Information {
    @Id
    @Column
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOTHER_INFORMATION")
    private PersonalInformation motherPersonalInformation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FATHER_INFORMATION")
    private PersonalInformation fatherPersonalInformation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "BROTHERS_SISTERS_INFORMATION")
    private List<PersonalInformation> brothersAndSistersPersonalInformation = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "GRANDPARENTS_INFORMATION")
    private List<PersonalInformation> grandparentsPersonalInformation = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WIFE_HUSBAND_INFORMATION")
    private PersonalInformation wifeOrHusbandInformation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "KIDS_INFORMATION")
    private List<PersonalInformation> kidsPersonalInformation = new ArrayList<>();
}
