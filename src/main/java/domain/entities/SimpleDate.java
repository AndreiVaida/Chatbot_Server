package domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "SIMPLE_DATE")
public class SimpleDate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Integer year = null;

    @Column
    private Integer month = null;

    @Column
    private Integer day = null;

    public SimpleDate(Integer year, Integer month, Integer day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public String toString() {
        String string = "";
        if (day != null) {
            string += day;
        }
        if (month != null) {
            if (!string.isEmpty()) string += ".";
            string += month;
        }
        if (year != null) {
            if (!string.isEmpty()) string += ".";
            string += year;
        }
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleDate that = (SimpleDate) o;
        return Objects.equals(year, that.year) &&
                Objects.equals(month, that.month) &&
                Objects.equals(day, that.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day);
    }
}
