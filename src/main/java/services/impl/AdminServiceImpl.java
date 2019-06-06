package services.impl;

import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import domain.enums.MessageSource;
import dtos.MessageDto;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import repositories.LinguisticExpressionRepository;
import repositories.SentenceRepository;
import services.api.AdminService;
import services.api.ChatService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final SentenceRepository sentenceRepository;
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final ChatService chatService;

    public AdminServiceImpl(SentenceRepository sentenceRepository, LinguisticExpressionRepository linguisticExpressionRepository, ChatService chatService) {
        this.sentenceRepository = sentenceRepository;
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.chatService = chatService;
    }

    @Override
    public List<Sentence> getAllSentences() {
        return sentenceRepository.findAll();
    }

    @Override
    public Sentence saveSentence(final Sentence sentence) {
        return sentenceRepository.save(sentence);
    }

    @Override
    public List<LinguisticExpression> getAllLinguisticExpressions() {
        return linguisticExpressionRepository.findAll();
    }

    @Override
    public LinguisticExpression saveLinguisticExpression(final LinguisticExpression linguisticExpression) {
        return linguisticExpressionRepository.save(linguisticExpression);
    }

    @Override
    public Integer addMessages(final MultipartFile fileWithMessags) throws IOException {
        int numberOfAddedMessages = 0;
        final JSONParser jsonParser = new JSONParser(fileWithMessags.getInputStream());
        try {
            final JSONArray jsonMessages = (JSONArray) jsonParser.parse();
            for (Object messageObject : jsonMessages) {
                final JSONObject messageJson = (JSONObject) messageObject;
                final Long fromUserId = (Long) messageJson.get("fromUserId");
                final Long toUserId = (Long) messageJson.get("toUserId");
                final String text = (String) messageJson.get("text");
                chatService.addMessageAndLearn(text, fromUserId, toUserId, MessageSource.USER_USER_CONVERSATION);
                numberOfAddedMessages++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return numberOfAddedMessages;
    }
}
