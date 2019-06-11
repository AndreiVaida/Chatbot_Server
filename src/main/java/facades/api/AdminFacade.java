package facades.api;

import domain.enums.ChatbotRequestType;
import dtos.AddedDataStatus;
import dtos.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.SentenceDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminFacade {
    List<SentenceDto> getAllSentences();

    SentenceDto saveSentence(final SentenceDto sentenceDto);

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
}
