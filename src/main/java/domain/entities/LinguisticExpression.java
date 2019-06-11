package domain.entities;

import domain.enums.SpeechType;
import domain.information.Information;
import domain.information.PersonalInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "LINGUISTIC_EXPRESSION")
public class LinguisticExpression {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "LINGUISTIC_EXPRESSION_ITEMS",
            joinColumns = {@JoinColumn(name = "LINGUISTIC_EXPRESSION_ID")},
            inverseJoinColumns = {@JoinColumn(name = "ITEM_ID")}
    )
    @OrderColumn
    private List<ExpressionItem> items = new ArrayList<>(); // the non-NOT_AN_INFORMATION ExpressionItem.itemClass is the information to identify in the message (it should be only 1 item like this)

    @Column
    private SpeechType speechType;

    @Column
    private Class<Information> informationClass;

    @Column
    private String informationFieldNamePath;

    @Override
    public String toString() {
        return "LinguisticExpression{" +
                "id=" + id +
                ", items=" + items +
                ", speechType=" + speechType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinguisticExpression sentence = (LinguisticExpression) o;
        return Objects.equals(id, sentence.id) &&
                Objects.equals(items, sentence.items) &&
                speechType == sentence.speechType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, items, speechType);
    }

    public void setInformationClass(Class informationClass) {
        this.informationClass = informationClass;
    }
}
