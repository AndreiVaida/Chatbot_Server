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

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "SENTENCE_DETECTION_PARAMETERS")
public class SentenceDetectionParameters {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(unique = true)
    private Integer sentenceLength;

    @Column
    private Integer maxNrOfExtraWords;

    @Column
    private Integer maxNrOfUnmatchedWords;

    @Column
    private Double weight;

    public SentenceDetectionParameters(Integer sentenceLength, Integer maxNrOfExtraWords, Integer maxNrOfUnmatchedWords, Double weight) {
        this.sentenceLength = sentenceLength;
        this.maxNrOfExtraWords = maxNrOfExtraWords;
        this.maxNrOfUnmatchedWords = maxNrOfUnmatchedWords;
        this.weight = weight;
    }
}
