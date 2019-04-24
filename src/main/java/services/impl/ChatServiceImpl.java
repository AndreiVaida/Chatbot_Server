package services.impl;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.MessageService;
import services.api.UserService;

import javax.transaction.Transactional;
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
    @Transactional
    public Message addMessage(final String text, final Long fromUserId, Long toUserId) {
        if (toUserId == null || toUserId == 0) {
            toUserId = CHATBOT_ID;
        }

        // save the message in DB
        final User fromUser = userService.getUserById(fromUserId);
        final User toUser = userService.getUserById(toUserId);
        final Message message = messageService.addMessage(text, fromUser, toUser);

        // generate a response
        final Sentence sentence = chatbotService.addSentence(message.getText());
        final Message previousMessage = messageService.getPreviousMessage(fromUser.getId(), toUser.getId(), message.getId());
        chatbotService.addAnswer(previousMessage, message);
        final String responseText = chatbotService.generateResponse(message.getText());
        return messageService.addMessage(responseText, toUser, fromUser);
    }

    @Override
    @Transactional
    public List<Message> getMessages(final Long userId1, Long userId2) {
        if (userId2 == null || userId2 == 0) {
            userId2 = CHATBOT_ID;
        }
        return messageService.getMessagesByUsers(userId1, userId2);
    }

    @Override
    @Transactional
    public Message requestMessageFromChatbot(final Long userId) {
        final String text = chatbotService.pickRandomSentence();
        final User fromUser = userService.getUserById(CHATBOT_ID);
        final User toUser = userService.getUserById(userId);
        return messageService.addMessage(text, fromUser, toUser);
    }
}
