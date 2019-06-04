package domain.entities;

import domain.enums.ItemClass;
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
@Table(name = "EXPRESSION_ITEM")
public class ExpressionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String text;

    @Column
    private ItemClass itemClass;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpressionItem)) return false;
        ExpressionItem that = (ExpressionItem) o;
        return Objects.equals(getText(), that.getText()) &&
                getItemClass() == that.getItemClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getText(), getItemClass());
    }
}
