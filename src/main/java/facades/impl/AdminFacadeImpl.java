package facades.impl;

import domain.entities.LinguisticExpression;
import domain.entities.RejectingExpression;
import domain.entities.Sentence;
import domain.enums.ChatbotRequestType;
import dtos.MessageDto;
import dtos.admin.AddedDataStatus;
import dtos.admin.LinguisticExpressionDto;
import dtos.admin.RejectingExpressionDto;
import dtos.admin.SentenceDetectionParametersDto;
import dtos.admin.SentenceDto;
import facades.api.AdminFacade;
import mappers.InformationMapper;
import mappers.SentenceMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import services.api.AdminService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminFacadeImpl implements AdminFacade {
    private final AdminService adminService;

    public AdminFacadeImpl(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public long getNumberOfSentences() {
        return adminService.getNumberOfSentences();
    }

    @Override
    public List<SentenceDto> getAllSentences() {
        return adminService.getAllSentences().stream().map(SentenceMapper::sentenceToSentenceDto).collect(Collectors.toList());
    }

    @Override
    public List<SentenceDto> getSentences(final Integer pageNumber, final Integer itemsPerPage) {
        return adminService.getSentences(pageNumber, itemsPerPage).stream().map(SentenceMapper::sentenceToSentenceDto).collect(Collectors.toList());
    }

    @Override
    public List<SentenceDto> findSentencesByWords(final String wordsAsString) {
        return adminService.findSentencesByWords(wordsAsString).stream().map(SentenceMapper::sentenceToSentenceDto).collect(Collectors.toList());
    }

    @Override
    public List<SentenceDto> getSentencesById(final List<Long> sentencesId) {
        return adminService.getSentencesById(sentencesId).stream().map(SentenceMapper::sentenceToSentenceDto).collect(Collectors.toList());
    }

    @Override
    public SentenceDto saveSentence(final SentenceDto sentenceDto) {
        final Sentence sentence = SentenceMapper.sentenceDtoToSentence(sentenceDto);
        return SentenceMapper.sentenceToSentenceDto(adminService.saveSentence(sentence));
    }

    @Override
    public SentenceDto updateSentenceSynonymFrequency(final Long sentenceId, final Long synonymId, final Integer newFrequency) {
        return SentenceMapper.sentenceToSentenceDto(adminService.updateSentenceSynonymFrequency(sentenceId, synonymId, newFrequency));
    }

    @Override
    public SentenceDto updateSentenceResponseFrequency(final Long sentenceId, final Long responseId, final Integer newFrequency) {
        return SentenceMapper.sentenceToSentenceDto(adminService.updateSentenceResponseFrequency(sentenceId, responseId, newFrequency));
    }

    @Override
    public List<SentenceDetectionParametersDto> getSentenceDetectionParameters() {
        return adminService.getSentenceDetectionParameters().stream()
                .map(SentenceMapper::sentenceDetectionParametersToSentenceDetectionParametersDto)
                .collect(Collectors.toList());
    }

    @Override
    public void setSentenceDetectionParameters(final List<SentenceDetectionParametersDto> sentenceDetectionParametersDto) {
        adminService.setSentenceDetectionParameters(sentenceDetectionParametersDto.stream()
                .map(SentenceMapper::sentenceDetectionParametersDtoToSentenceDetectionParameters)
                .collect(Collectors.toList()));
    }

    @Override
    public List<LinguisticExpressionDto> getAllLinguisticExpressions() {
        return adminService.getAllLinguisticExpressions().stream().map(InformationMapper::linguisticExpressionToLinguisticExpressionDto).collect(Collectors.toList());
    }

    @Override
    public LinguisticExpressionDto saveLinguisticExpression(final LinguisticExpressionDto linguisticExpressionDto) {
        final LinguisticExpression linguisticExpression = InformationMapper.linguisticExpressionDtoToLinguisticExpression(linguisticExpressionDto);
        return InformationMapper.linguisticExpressionToLinguisticExpressionDto(adminService.saveLinguisticExpression(linguisticExpression));
    }

    @Override
    public void deleteLinguisticExpression(final Long linguisticExpressionId) {
        adminService.deleteLinguisticExpression(linguisticExpressionId);
    }

    @Override
    public List<RejectingExpressionDto> getAllRejectingExpressions() {
        return adminService.getAllRejectingExpressions().stream().map(InformationMapper::rejectingExpressionToRejectingExpressionDto).collect(Collectors.toList());
    }

    @Override
    public RejectingExpressionDto saveRejectingExpression(final RejectingExpressionDto rejectingExpressionDto) {
        final RejectingExpression rejectingExpression = InformationMapper.rejectingExpressionDtoToRejectingExpression(rejectingExpressionDto);
        return InformationMapper.rejectingExpressionToRejectingExpressionDto(adminService.saveRejectingExpression(rejectingExpression));
    }

    @Override
    public void deleteRejectingExpression(final Long rejectingExpressionId) {
        adminService.deleteRejectingExpression(rejectingExpressionId);
    }

    @Override
    public AddedDataStatus addMessagesFromFile(final MultipartFile fileWithMessages) throws IOException {
        return adminService.addMessagesFromFile(fileWithMessages);
    }

    @Override
    public AddedDataStatus addMessageDtos(final List<MessageDto> messageDtos) {
        return adminService.addMessageDtos(messageDtos);
    }

    @Override
    public AddedDataStatus addMessages(final List<String> messages) {
        return adminService.addMessages(messages);
    }

    @Override
    public AddedDataStatus addMessagesFromCsvString(final String csvString) {
        return adminService.addMessagesFromCsvString(csvString);
    }

    @Override
    public AddedDataStatus addAnswersAndQuestionsFromCsvString(final MultipartFile csvFile) throws IOException {
        return adminService.addAnswersAndQuestionsFromCsvFile(csvFile);
    }

    @Override
    public AddedDataStatus addSentencesFromJsonFile(final MultipartFile sentencesJsonFile) throws IOException {
        return adminService.addSentencesFromJsonFile(sentencesJsonFile);
    }

    @Override
    public AddedDataStatus addLinguisticExpressionsFromJsonFile(final MultipartFile linguisticExpressionsJsonFile) throws IOException {
        return adminService.addLinguisticExpressionsFromJsonFile(linguisticExpressionsJsonFile);
    }

    @Override
    public AddedDataStatus addRejectingExpressionsFromJsonFile(final MultipartFile rejectingExpressionsJsonFile) throws IOException {
        return adminService.addRejectingExpressionsFromJsonFile(rejectingExpressionsJsonFile);
    }

    @Override
    public ChatbotRequestType getChatbotRequestType() {
        return adminService.getChatbotRequestType();
    }

    @Override
    public void setChatbotRequestType(final ChatbotRequestType chatbotRequestType) {
        adminService.setChatbotRequestType(chatbotRequestType);
    }

    @Override
    public AddedDataStatus addConversationsFromWebsite() {
        return adminService.loadFileConversationsFromWebsite();
    }

    @Override
    public AddedDataStatus getDataLoadingStatus() {
        return adminService.getDataLoadingStatus();
    }
}
