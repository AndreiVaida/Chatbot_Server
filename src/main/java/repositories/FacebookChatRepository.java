package repositories;

import dtos.MessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Repository;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class FacebookChatRepository {

    public List<MessageDto> readChatFromJsonFile(final String pathToFile) throws IOException, ParseException {
        final JSONParser parser = new JSONParser();
        final Object facebookChat = parser.parse(new FileReader(pathToFile));
        final JSONObject jsonFacebookChat = (JSONObject) facebookChat;
        final JSONArray jsonMessages = (JSONArray) jsonFacebookChat.get("messages");

        final Long user1Id = 1L;
        final Long user2Id = 2L;
        boolean isUser1Message = true;
        final List<MessageDto> messageDtos = new ArrayList<>();

        for (Object objectMessage : jsonMessages) {
            final JSONObject jsonMessage = (JSONObject) objectMessage;
            if (jsonMessage.containsKey("content")) {
                String message = (String) jsonMessage.get("content");

                if (message.contains("\\u")) {
                    System.out.println(message);
                }
                message = message.replace("\n", " ");
                message = message.replace("\\u0[0-9a-zA-Z]{3}", "");

                if (message.isEmpty() || message.length() > 200) {
                    continue;
                }

                final MessageDto messageDto = new MessageDto();
                messageDto.setMessage(message);
                if (isUser1Message) {
                    messageDto.setFromUserId(user1Id);
                    messageDto.setToUserId(user2Id);
                } else {
                    messageDto.setFromUserId(user2Id);
                    messageDto.setToUserId(user1Id);
                }
                isUser1Message = !isUser1Message;
                messageDtos.add(messageDto);
            }
        }

        Collections.reverse(messageDtos);
        return messageDtos;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class FacebookChat {
        private List<Participant> participants;
        private List<Message> messages;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class Participant {
        private String name;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private class Message {
        private String sender_name;
        private String content;
    }
}
