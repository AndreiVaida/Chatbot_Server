package dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class WordDto {
    private Long id;
    private String text;
    private Map<Long, Integer> synonyms = new HashMap<>(); // <synonymId, frequency>
}
