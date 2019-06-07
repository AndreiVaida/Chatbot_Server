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

import javax.transaction.Transactional;
import java.io.IOException;
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
    @Transactional
    public List<Sentence> getAllSentences() {
        return sentenceRepository.findAll();
    }

    @Override
    @Transactional
    public Sentence saveSentence(final Sentence sentence) {
        return sentenceRepository.save(sentence);
    }

    @Override
    @Transactional
    public List<LinguisticExpression> getAllLinguisticExpressions() {
        return linguisticExpressionRepository.findAll();
    }

    @Override
    @Transactional
    public LinguisticExpression saveLinguisticExpression(final LinguisticExpression linguisticExpression) {
        return linguisticExpressionRepository.save(linguisticExpression);
    }

    @Override
    @Transactional
    public Integer addMessagesFromFile(final MultipartFile fileWithMessags) throws IOException {
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

    @Override
    @Transactional
    public Integer addMessages(final List<MessageDto> messageDtos) {
        int numberOfAddedMessages = 0;
        for (MessageDto messageDto : messageDtos) {
            chatService.addMessageAndLearn(messageDto.getMessage(), messageDto.getFromUserId(), messageDto.getToUserId(), MessageSource.USER_USER_CONVERSATION);
            numberOfAddedMessages++;
        }
        return numberOfAddedMessages;
    }
}
