package facades.api;

import domain.enums.ChatbotRequestType;
import dtos.admin.AddedDataStatus;
import dtos.admin.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.admin.SentenceDetectionParametersDto;
import dtos.admin.SentenceDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminFacade {
    long getNumberOfSentences();

    List<SentenceDto> getAllSentences();

    List<SentenceDto> getSentences(final Integer pageNumber, final Integer itemsPerPage);

    List<SentenceDto> findSentencesByWords(final String wordsAsString);

    List<SentenceDto> getSentencesById(final List<Long> sentencesId);

    SentenceDto saveSentence(final SentenceDto sentenceDto);

    List<SentenceDetectionParametersDto> getSentenceDetectionParameters();

    void setSentenceDetectionParameters(final List<SentenceDetectionParametersDto> sentenceDetectionParametersDto);

    List<LinguisticExpressionDto> getAllLinguisticExpressions();

    LinguisticExpressionDto saveLinguisticExpression(final LinguisticExpressionDto linguisticExpressionDto);

    AddedDataStatus addMessagesFromFile(final MultipartFile fileWithMessages) throws IOException;

    AddedDataStatus addMessageDtos(final List<MessageDto> messageDtos);

    AddedDataStatus addMessages(final List<String> messages);

    AddedDataStatus addMessagesFromCsvString(final String csvString);

    AddedDataStatus addSentencesFromJsonFile(final MultipartFile sentencesJsonFile) throws IOException;

    AddedDataStatus addLinguisticExpressionsFromJsonFile(final MultipartFile linguisticExpressionsJsonFile) throws IOException;

    ChatbotRequestType getChatbotRequestType();

    void setChatbotRequestType(final ChatbotRequestType chatbotRequestType);

    AddedDataStatus addConversationsFromWebsite();
}
