package facades.impl;

import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import dtos.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.SentenceDto;
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
    public List<SentenceDto> getAllSentences() {
        return adminService.getAllSentences().stream().map(SentenceMapper::sentenceToSentenceDto).collect(Collectors.toList());
    }

    @Override
    public SentenceDto saveSentence(final SentenceDto sentenceDto) {
        final Sentence sentence = SentenceMapper.sentenceDtoToSentence(sentenceDto);
        return SentenceMapper.sentenceToSentenceDto(adminService.saveSentence(sentence));
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
    public Integer addMessagesFromFile(final MultipartFile fileWithMessages) throws IOException {
        return adminService.addMessagesFromFile(fileWithMessages);
    }

    @Override
    public Integer addMessageDtos(final List<MessageDto> messageDtos) {
        return adminService.addMessageDtos(messageDtos);
    }

    @Override
    public Integer addMessages(final List<String> messages) {
        return adminService.addMessages(messages);
    }
}
