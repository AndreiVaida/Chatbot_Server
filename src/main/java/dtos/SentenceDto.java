package dtos;

import domain.enums.SpeechType;
import dtos.informationDtos.InformationClassDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SentenceDto {
    private Long id;
    private List<WordDto> words = new ArrayList<>();
    private SpeechType speechType;
    private InformationClassDto informationClassDto;
    private String informationFieldNamePath;
    private Map<Long, Integer> synonyms = new HashMap<>(); // <synonymId, frequency>
    private Map<Long, Integer> responses = new HashMap<>(); // <responseId, frequency>
}
