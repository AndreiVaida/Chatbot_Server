package dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SentenceDetectionParametersDto {
    private Integer sentenceLength;
    private Integer maxNrOfExtraWords;
    private Integer maxNrOfUnmatchedWords;
    private Double weight;
}
