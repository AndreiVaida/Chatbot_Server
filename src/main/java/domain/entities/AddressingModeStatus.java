package domain.entities;

import domain.enums.AddressingMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "ADDRESSING_MODE_STATUS")
public class AddressingModeStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private int nrOfFormalWords;

    @Column
    private int nrOfInformalWords;

    public AddressingMode getPreferredAddressingMode() {
        if (nrOfFormalWords == 0 && nrOfInformalWords == 0) {
            return null;
        }
        if (nrOfFormalWords > nrOfInformalWords) {
            return AddressingMode.FORMAL;
        }
        if (nrOfFormalWords < nrOfInformalWords) {
            return AddressingMode.INFORMAL;
        }
        return AddressingMode.FORMAL_AND_INFORMAL;
    }

    public void add(final int nrOfFormalWords, final int nrOfInformalWords) {
        this.nrOfFormalWords += nrOfFormalWords;
        this.nrOfInformalWords += nrOfInformalWords;
    }
}
