package facades.api;

import domain.enums.ChatbotRequestType;
import dtos.admin.AddedDataStatus;
import dtos.admin.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.admin.RejectingExpressionDto;
import dtos.admin.SentenceDetectionParametersDto;
import dtos.admin.SentenceDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminFacade {
    // Sentence
    long getNumberOfSentences();

    List<SentenceDto> getAllSentences();

    List<SentenceDto> getSentences(final Integer pageNumber, final Integer itemsPerPage);

    List<SentenceDto> findSentencesByWords(final String wordsAsString);

    List<SentenceDto> getSentencesById(final List<Long> sentencesId);

    SentenceDto saveSentence(final SentenceDto sentenceDto);

    SentenceDto updateSentenceSynonymFrequency(final Long sentenceId, final Long synonymId, final Integer newFrequency);

    SentenceDto updateSentenceResponseFrequency(final Long sentenceId, final Long responseId, final Integer newFrequency);

    List<SentenceDetectionParametersDto> getSentenceDetectionParameters();

    void setSentenceDetectionParameters(final List<SentenceDetectionParametersDto> sentenceDetectionParametersDto);

    // LinguisticExpression
    List<LinguisticExpressionDto> getAllLinguisticExpressions();

    LinguisticExpressionDto saveLinguisticExpression(final LinguisticExpressionDto linguisticExpressionDto);

    void deleteLinguisticExpression(final Long linguisticExpressionId);

    // RejectingExpression
    List<RejectingExpressionDto> getAllRejectingExpressions();

    RejectingExpressionDto saveRejectingExpression(final RejectingExpressionDto rejectingExpressionDto);

    void deleteRejectingExpression(final Long rejectingExpressionId);

    // File/data upload
    AddedDataStatus addMessagesFromFile(final MultipartFile fileWithMessages) throws IOException;

    AddedDataStatus addMessageDtos(final List<MessageDto> messageDtos);

    AddedDataStatus addMessages(final List<String> messages);

    AddedDataStatus addMessagesFromCsvString(final String csvString);

    AddedDataStatus addSentencesFromJsonFile(final MultipartFile sentencesJsonFile) throws IOException;

    AddedDataStatus addLinguisticExpressionsFromJsonFile(final MultipartFile linguisticExpressionsJsonFile) throws IOException;

    AddedDataStatus addRejectingExpressionsFromJsonFile(final MultipartFile rejectingExpressionsJsonFile) throws IOException;

    ChatbotRequestType getChatbotRequestType();

    void setChatbotRequestType(final ChatbotRequestType chatbotRequestType);

    AddedDataStatus addConversationsFromWebsite();
}
