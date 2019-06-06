package mappers;

import domain.entities.Sentence;
import domain.entities.Word;
import dtos.SentenceDto;
import dtos.WordDto;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SentenceMapper {
    private SentenceMapper() {}

    public static SentenceDto sentenceToSentenceDto(final Sentence sentence) {
        final SentenceDto sentenceDto = new SentenceDto();
        sentenceDto.setId(sentence.getId());
        sentenceDto.setWords(sentence.getWords().stream().map(SentenceMapper::wordToWordDto).collect(Collectors.toList()));
        sentenceDto.setSpeechType(sentence.getSpeechType());
        sentenceDto.setInformationClassDto(InformationMapper.informationClassToClassOfInformationDto(sentence.getInformationClass()));
        sentenceDto.setInformationFieldNamePath(sentence.getInformationFieldNamePath());
        sentenceDto.setSynonyms(sentence.getSynonyms().keySet().stream().collect(Collectors.toMap(Sentence::getId, synonym -> sentence.getSynonyms().get(synonym))));
        sentenceDto.setResponses(sentence.getResponses().keySet().stream().collect(Collectors.toMap(Sentence::getId, response -> sentence.getResponses().get(response))));
        return sentenceDto;
    }

    private static SentenceDto sentenceToSentenceDto_withoutSynonymsAndResponses(final Sentence sentence) {
        final SentenceDto sentenceDto = new SentenceDto();
        sentenceDto.setId(sentence.getId());
        sentenceDto.setWords(sentence.getWords().stream().map(SentenceMapper::wordToWordDto).collect(Collectors.toList()));
        sentenceDto.setSpeechType(sentence.getSpeechType());
        sentenceDto.setInformationClassDto(InformationMapper.informationClassToClassOfInformationDto(sentence.getInformationClass()));
        sentenceDto.setInformationFieldNamePath(sentence.getInformationFieldNamePath());
        return sentenceDto;
    }

    public static Sentence sentenceDtoToSentence(final SentenceDto sentenceDto) {
        final Sentence sentence = new Sentence();
        sentence.setId(sentenceDto.getId());
        sentence.setWords(sentenceDto.getWords().stream().map(SentenceMapper::wordDtoToWord).collect(Collectors.toList()));
        sentence.setSpeechType(sentenceDto.getSpeechType());
        sentence.setInformationClass(InformationMapper.informationClassDtoToClassOfInformation(sentenceDto.getInformationClassDto()));
        sentence.setInformationFieldNamePath(sentenceDto.getInformationFieldNamePath());
        sentence.setSynonyms(sentenceDto.getSynonyms().keySet().stream().collect(Collectors.toMap(Sentence::new, synonymId -> sentenceDto.getSynonyms().get(synonymId))));
        sentence.setResponses(sentenceDto.getResponses().keySet().stream().collect(Collectors.toMap(Sentence::new, responseId -> sentenceDto.getResponses().get(responseId))));
        return sentence;
    }

    private static WordDto wordToWordDto(final Word word) {
        final WordDto wordDto = new WordDto();
        wordDto.setId(word.getId());
        wordDto.setText(word.getText());
        wordDto.setSynonyms(word.getSynonyms().keySet().stream().collect(Collectors.toMap(SentenceMapper::wordToWordDto, synonym -> word.getSynonyms().get(synonym))));
        return wordDto;
    }

    private static Word wordDtoToWord(final WordDto wordDto1) {
        final Word word = new Word();
        word.setId(wordDto1.getId());
        word.setText(wordDto1.getText());
        final Map<Word, Integer> synonyms = new HashMap<>();
        for (WordDto wordDto : wordDto1.getSynonyms().keySet()) {
            synonyms.put(wordDtoToWord(wordDto), wordDto1.getSynonyms().get(wordDto));
        }
        word.setSynonyms(synonyms);
        return word;
    }
}
