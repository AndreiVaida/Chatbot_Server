package services.impl;

import domain.entities.Message;
import domain.entities.Sentence;
import domain.entities.User;
import domain.enums.ChatbotRequestType;
import domain.enums.MessageSource;
import domain.information.Information;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.InformationService;
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
    private final InformationService informationService;

    @Autowired
    public ChatServiceImpl(MessageService messageService, UserService userService, ChatbotService chatbotService, InformationService informationService) {
        this.messageService = messageService;
        this.userService = userService;
        this.chatbotService = chatbotService;
        this.informationService = informationService;
    }

    @Override
    @Transactional
    public Message addMessage(final String text, final Long fromUserId, Long toUserId, final MessageSource messageSource) {
        if (toUserId == null || toUserId == 0) {
            toUserId = CHATBOT_ID;
        }

        // save the message in DB
        final User fromUser = userService.getUserById(fromUserId);
        final User toUser = userService.getUserById(toUserId);
        final Sentence sentence = chatbotService.getSentence(text);
        final Message message = messageService.addMessage(text, fromUser, toUser, sentence, messageSource);

        final Message previousMessage = messageService.getPreviousMessage(fromUser.getId(), toUser.getId(), message.getId());
        if (previousMessage != null) {
            chatbotService.addResponseAndSynonym(previousMessage.getEquivalentSentence(), sentence);
        }

        return message;
    }

    @Override
    @Transactional
    public Message addMessageAndGetResponse(final String text, final Long fromUserId, Long toUserId) {
        MessageSource messageSource = MessageSource.USER_USER_CONVERSATION;
        if (toUserId == null || toUserId == 0) {
            toUserId = CHATBOT_ID;
            messageSource = MessageSource.USER_CHATBOT_CONVERSATION;
        }
        final User fromUser = userService.getUserById(fromUserId);
        final User toUser = userService.getUserById(toUserId);

        // save the message in DB and add this message as a response for previous one
        final Message previousMessage = messageService.getLastMessage(toUserId, fromUserId);
        final Message message = addMessageAndSetItAsResponse(text, fromUser, toUser, messageSource, previousMessage);

        // extract the information from the message and update the user details
        Class<Information> informationClass = null;
        String informationFieldName = null;
        if (previousMessage != null) {
            informationClass = previousMessage.getEquivalentSentence().getInformationClass();
            informationFieldName = previousMessage.getEquivalentSentence().getInformationFieldNamePath();
        }
        try {
            final Information information = informationService.identifyInformation(informationClass, informationFieldName, message);
            if (information != null) {
                userService.updateUserInformation(information, fromUser);
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        // generate a response
        return generateResponse(message);
    }

    private Message addMessageAndSetItAsResponse(final String text, final User fromUser, final User toUser, final MessageSource messageSource, final Message previousMessage) {
        final Sentence sentence = chatbotService.getSentence(text);

        // save the message ind DB
        final Message message = messageService.addMessage(text, fromUser, toUser, sentence, messageSource);

        // add this message as a response for the previous message

        if (previousMessage != null) {
            chatbotService.addResponseAndSynonym(previousMessage.getEquivalentSentence(), sentence);
        }

        return message;
    }

    private Message generateResponse(final Message message) {
        Sentence responseSentence = chatbotService.generateResponse(message);
        boolean isUnknownMessage = false;
        if (responseSentence == null) {
            responseSentence = chatbotService.pickSentenceWithFewReplies();
            isUnknownMessage = true;
        }

        // save the response
        final String responseText = chatbotService.translateSentenceToText(responseSentence);
        final Message responseMessage = messageService.addMessage(responseText, message.getToUser(), message.getFromUser(), responseSentence, MessageSource.USER_CHATBOT_CONVERSATION);
        responseMessage.setIsUnknownMessage(isUnknownMessage);
        return responseMessage;
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
    public Message requestMessageFromChatbot(final Long userId, ChatbotRequestType chatbotRequestType) {
        if (chatbotRequestType == null) {
            chatbotRequestType = ChatService.CHATBOT_REQUEST_TYPE;
        }
        final User fromUser = userService.getUserById(CHATBOT_ID);
        final User toUser = userService.getUserById(userId);

        final Sentence sentence;
        switch (chatbotRequestType) {
            case LEARN_TO_SPEAK: sentence = chatbotService.pickSentenceWithFewReplies(); break;
            case GET_INFORMATION_FROM_USER: sentence = chatbotService.pickSentenceRequestingInformation(toUser); break;
            default: sentence = chatbotService.pickRandomSentence(); break;
        }
        return messageService.addMessage(chatbotService.translateSentenceToText(sentence), fromUser, toUser, sentence, MessageSource.USER_CHATBOT_CONVERSATION);
    }
}
