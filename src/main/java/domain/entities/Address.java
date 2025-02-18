package domain.entities;

import domain.enums.LocalityType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "ADDRESS")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "PLANET")
    private String planet;

    @Column(name = "CONTINENT")
    private String continent;

    @Column(name = "COUNTRY")
    private String country;

    @Column(name = "COUNTY")
    private String county;

    @Column(name = "LOCALITY")
    private String locality;

    @Column(name = "LOCALITY_TYPE")
    private LocalityType localityType;

    @Column(name = "NEIGHBORHOOD")
    private String neighborhood;

    @Column(name = "STREET")
    private String street;

    @Column(name = "STREET_NUMBER")
    private Integer streetNumber;

    @Column(name = "FLOOR")
    private Integer floor;

    @Column(name = "APARTMENT_NUMBER")
    private Integer apartmentNumber;

    public List<String> getFieldNamesInImportanceOrder() {
        final List<String> fieldNamesInImportanceOrder = new ArrayList<>();
        fieldNamesInImportanceOrder.add("planet");
        fieldNamesInImportanceOrder.add("continent");
        fieldNamesInImportanceOrder.add("country");
        fieldNamesInImportanceOrder.add("county");
        fieldNamesInImportanceOrder.add("localityType");
        fieldNamesInImportanceOrder.add("locality");
        fieldNamesInImportanceOrder.add("street");
        fieldNamesInImportanceOrder.add("streetNumber");
        return fieldNamesInImportanceOrder;
    }

    public List<String> getUrbanFieldNamesInImportanceOrder() {
        final List<String> fieldNamesInImportanceOrder = new ArrayList<>();
        fieldNamesInImportanceOrder.add("neighborhood");
        fieldNamesInImportanceOrder.add("floor");
        fieldNamesInImportanceOrder.add("apartmentNumber");
        return fieldNamesInImportanceOrder;
    }
}
