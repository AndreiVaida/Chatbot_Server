package domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "WORDS")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String text;

    @ElementCollection
    @CollectionTable(name="WORD_SYNONYMS",
            joinColumns=@JoinColumn(name="SYNONYM_ID"))
    private Map<Word, Integer> synonyms = new HashMap<>(); // <synonym, frequency>

    /**
     * Adds the given word as a synonym to this one.
     * If this word already has the given synonym, then is increased its frequency.
     */
    public void addSynonym(final Word synonym) {
        Integer frequency = synonyms.get(synonym);
        if (frequency == null) {
            frequency = 0;
        }
        frequency++;
        synonyms.put(synonym, frequency);
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(id, word.id) &&
                Objects.equals(text, word.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }
}
