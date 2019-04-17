package services.impl;

import domain.entities.Message;
import domain.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.MessageService;
import services.api.UserService;

import java.util.List;

import static app.Main.CHATBOT_ID;

@Service
public class ChatServiceImpl implements ChatService {
    private final UserService userService;
    private final MessageService messageService;
    private final ChatbotService chatbotService;

    @Autowired
    public ChatServiceImpl(MessageService messageService, UserService userService, ChatbotService chatbotService) {
        this.messageService = messageService;
        this.userService = userService;
        this.chatbotService = chatbotService;
    }

    @Override
    public Message addMessage(final String text, final Long fromUserId, final Long toUserId) {
        // save the message in DB
        final User fromUser = userService.getUserById(fromUserId);
        final User toUser = userService.getUserById(toUserId);
        final Message message = messageService.addMessage(text, fromUser, toUser);

        // generate a response
        final String responseText = chatbotService.generateResponse(message.getText());
        return messageService.addMessage(responseText, toUser, fromUser);
    }

    @Override
    public List<Message> getMessages(final Long userId1, final Long userId2) {
        return messageService.getMessagesByUsers(userId1, userId2);
    }

    @Override
    public Message requestMessageFromChatbot(final Long userId) {
        final String text = chatbotService.pickRandomSentence();
        final User fromUser = userService.getUserById(CHATBOT_ID);
        final User toUser = userService.getUserById(userId);
        return messageService.addMessage(text, fromUser, toUser);
    }
}
