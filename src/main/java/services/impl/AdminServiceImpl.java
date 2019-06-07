package services.impl;

import app.Main;
import domain.entities.CsvConversationTimestamp;
import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import domain.enums.MessageSource;
import dtos.AddedDataStatus;
import dtos.MessageDto;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import repositories.CsvConversationTimestampRepository;
import repositories.LinguisticExpressionRepository;
import repositories.SentenceRepository;
import services.api.AdminService;
import services.api.ChatService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {
    private final SentenceRepository sentenceRepository;
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final ChatService chatService;
    private final CsvConversationTimestampRepository csvConversationTimestampRepository;

    public AdminServiceImpl(SentenceRepository sentenceRepository, LinguisticExpressionRepository linguisticExpressionRepository, ChatService chatService, CsvConversationTimestampRepository csvConversationTimestampRepository) {
        this.sentenceRepository = sentenceRepository;
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.chatService = chatService;
        this.csvConversationTimestampRepository = csvConversationTimestampRepository;
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
    public AddedDataStatus addMessagesFromFile(final MultipartFile fileWithMessags) throws IOException {
        int numberOfMessages = 0;
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
                numberOfMessages++;
                numberOfAddedMessages++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessageDtos(final List<MessageDto> messageDtos) {
        int numberOfMessages = 0;
        int numberOfAddedMessages = 0;
        for (MessageDto messageDto : messageDtos) {
            chatService.addMessageAndLearn(messageDto.getMessage(), messageDto.getFromUserId(), messageDto.getToUserId(), MessageSource.USER_USER_CONVERSATION);
            numberOfMessages++;
            numberOfAddedMessages++;
        }
        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessages(List<String> messages) {
        Long user1Id = Main.USER_FOR_LEARNING_1_ID;
        Long user2Id = Main.USER_FOR_LEARNING_2_ID;

        int numberOfMessages = 0;
        int numberOfAddedMessages = 0;
        for (String message : messages) {
            numberOfMessages++;
            if (message.isEmpty()) {
                continue;
            }
            chatService.addMessageAndLearn(message, user1Id, user2Id, MessageSource.USER_USER_CONVERSATION);
            numberOfAddedMessages++;
            final Long auxId = user1Id;
            user1Id = user2Id;
            user2Id = auxId;
        }
        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessagesFromCsvString(final String csvString) {
        final Map<LocalDateTime, List<String>> conversations = getConversationsFromCsvString(csvString);

        int numberOfConversations = 0;
        int numberOfAddedConversations = 0;
        for (LocalDateTime timestamp : conversations.keySet()) {
            numberOfConversations++;
            if (!csvConversationTimestampRepository.existsByTimestamp(timestamp)) {
                addMessages(conversations.get(timestamp));
                csvConversationTimestampRepository.save(new CsvConversationTimestamp(timestamp));
                numberOfAddedConversations++;
            }
        }

        return new AddedDataStatus(numberOfConversations, numberOfAddedConversations);
    }

    private Map<LocalDateTime, List<String>> getConversationsFromCsvString(final String csvString) {
        final String[] lines = csvString.split("\\R");
        final Map<LocalDateTime, List<String>> conversations = new HashMap<>();
        List<String> conversation = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            final String line = lines[i];
            if (line.replaceAll("\\s+","").isEmpty()) {
                continue;
            }
            if (line.startsWith("\"")) {
                // new conversation
                final String[] timestampAndFirstMessage = line.split("\",\"");
                final LocalDateTime timestamp = convertStringToLocalDateTime(timestampAndFirstMessage[0].substring(1));
                final String message = timestampAndFirstMessage[1];
                conversation = new ArrayList<>();
                conversation.add(message);
                conversations.put(timestamp, conversation);
            } else {
                // inside a conversation
                if (line.endsWith("\"")) {
                    // end of conversation
                    final String message = line.substring(0, line.length() - 1);
                    conversation.add(message);
                }
                else {
                    conversation.add(line);
                }
            }
        }
        return conversations;
    }

    private LocalDateTime convertStringToLocalDateTime(final String timestampeString) {
        final String[] sections = timestampeString.split(" ");
        final String[] dateSection = sections[0].split("/");
        final String[] timeSection = sections[1].split(":");
        return LocalDateTime.of(Integer.parseInt(dateSection[0]), Integer.parseInt(dateSection[1]), Integer.parseInt(dateSection[2]),
                Integer.parseInt(timeSection[0]), Integer.parseInt(timeSection[1]), Integer.parseInt(timeSection[2]));
    }
}
