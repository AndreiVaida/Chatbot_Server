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
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@Table(name = "RELATIONSHIP_INFORMATION")
public class RelationshipsInformation implements Information {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOTHER_INFORMATION")
    private PersonalInformation motherPersonalInformation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FATHER_INFORMATION")
    private PersonalInformation fatherPersonalInformation;

    @Column
    private Integer numberOfBrothersAndSisters;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "BROTHERS_SISTERS_INFORMATION")
    @MapKey(name = "firstName")
    private Map<String, PersonalInformation> brothersAndSistersPersonalInformation = new HashMap<>();

    @Column
    private Integer numberOfGrandparents;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "GRANDPARENTS_INFORMATION")
    @MapKey(name = "firstName")
    private Map<String, PersonalInformation> grandparentsPersonalInformation = new HashMap<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WIFE_HUSBAND_INFORMATION")
    private PersonalInformation wifeOrHusbandInformation;

    @Column
    private Integer numberOfKids;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "KIDS_INFORMATION")
    @MapKey(name = "firstName")
    private Map<String, PersonalInformation> kidsPersonalInformation = new HashMap<>(); // <kidFirstName, PersonalInformation>
}
