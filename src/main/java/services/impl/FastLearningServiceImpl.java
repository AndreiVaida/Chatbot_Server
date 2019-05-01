package services.impl;

import domain.enums.MessageSource;
import dtos.MessageDto;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.api.ChatService;
import services.api.FastLearningService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

@Data
@Service
public class FastLearningServiceImpl implements FastLearningService {
    final ChatService chatService;
    final String pathToFolder = "/conversations/";

    @Autowired
    public FastLearningServiceImpl(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void addKnowledgeToChatbotIfRequested() {
        final Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.print("Conversation file (type X to cancel): ");
        final String fileName = scanner.nextLine();
        if (fileName.toLowerCase().equals("x")) {
            return;
        }

        final String pathToFile = pathToFolder + fileName;
        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(pathToFile)));
            final Long user1Id = Long.valueOf(bufferedReader.readLine());
            final Long user2Id = Long.valueOf(bufferedReader.readLine());

            String message;
            boolean isUser1Message = true;
            while ((message = bufferedReader.readLine()) != null) {
                if (isUser1Message) {
                    chatService.addMessage(message, user1Id, user2Id, MessageSource.USER_USER_CONVERSATION);
                } else {
                    chatService.addMessage(message, user2Id, user1Id, MessageSource.USER_USER_CONVERSATION);
                }
                isUser1Message = !isUser1Message;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("--------------------------------------------------");
        System.out.println();
    }

    @Override
    public void addMessages(final List<MessageDto> messageDtos) {
        for (MessageDto messageDto : messageDtos) {
            chatService.addMessage(messageDto.getMessage(), messageDto.getFromUserId(), messageDto.getToUserId(), MessageSource.USER_USER_CONVERSATION);
        }
    }
}
