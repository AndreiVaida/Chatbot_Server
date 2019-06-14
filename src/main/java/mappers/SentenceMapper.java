package mappers;

import domain.entities.Sentence;
import domain.entities.Word;
import domain.enums.SpeechType;
import dtos.informationDtos.InformationClassDto;
import dtos.SentenceDto;
import dtos.WordDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static Sentence sentenceJsonToSentence(final String[] wordArray, final SpeechType speechType, final InformationClassDto informationClassDto, final String informationFieldNamePath) {
        final Sentence sentence = new Sentence();
        final List<Word> words = new ArrayList<>();
        for (String wordText : wordArray) {
            words.add(new Word(wordText));
        }
        sentence.setWords(words);
        sentence.setSpeechType(speechType);
        sentence.setInformationClass(InformationMapper.informationClassDtoToClassOfInformation(informationClassDto));
        sentence.setInformationFieldNamePath(informationFieldNamePath);
        return sentence;
    }

    private static WordDto wordToWordDto(final Word word) {
        final WordDto wordDto = new WordDto();
        wordDto.setId(word.getId());
        wordDto.setText(word.getText());
        wordDto.setSynonyms(word.getSynonyms().keySet().stream().collect(Collectors.toMap(Word::getId, synonym -> word.getSynonyms().get(synonym))));
        return wordDto;
    }

    /**
     * WARNING: the synonyms have only the id
     */
    private static Word wordDtoToWord(final WordDto wordDto) {
        final Word word = new Word();
        word.setId(wordDto.getId());
        word.setText(wordDto.getText());
        final Map<Word, Integer> synonyms = new HashMap<>();
        for (Long synonymId : wordDto.getSynonyms().keySet()) {
            final Word synonym = new Word();
            synonym.setId(synonymId);
            synonyms.put(word, wordDto.getSynonyms().get(synonymId));
        }
        word.setSynonyms(synonyms);
        return word;
    }
}
