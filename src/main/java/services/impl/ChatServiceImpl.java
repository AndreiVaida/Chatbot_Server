package services.impl;

import domain.entities.Message;
import domain.entities.ResponseMessageAndInformation;
import domain.entities.Sentence;
import domain.entities.User;
import domain.enums.AddressingMode;
import domain.enums.ChatbotRequestType;
import domain.enums.MessageSource;
import domain.information.Information;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.InformationDetectionService;
import services.api.MessageService;
import services.api.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static app.Main.CHATBOT_ID;
import static domain.enums.ChatbotRequestType.DEFAULT;
import static domain.enums.ChatbotRequestType.GET_INFORMATION_FROM_USER;
import static domain.enums.SpeechType.DIRECTIVE;
import static services.api.ChatbotService.MINUTES_TO_WAIT_TO_REQUEST_AGAIN_SAME_INFORMATION;

@Service
public class ChatServiceImpl implements ChatService {
    private final UserService userService;
    private final MessageService messageService;
    private final ChatbotService chatbotService;
    private final InformationDetectionService informationDetectionService;
    private ChatbotRequestType chatbotRequestType = DEFAULT;
    private final Random random;

    @Autowired
    public ChatServiceImpl(MessageService messageService, UserService userService, ChatbotService chatbotService, InformationDetectionService informationDetectionService) {
        this.messageService = messageService;
        this.userService = userService;
        this.chatbotService = chatbotService;
        this.informationDetectionService = informationDetectionService;
        random = new Random();
    }

    @Override
    public ChatbotRequestType getChatbotRequestType() {
        return chatbotRequestType;
    }

    @Override
    public void setChatbotRequestType(final ChatbotRequestType chatbotRequestType) {
        this.chatbotRequestType = chatbotRequestType;
    }

    @Override
    public Message addMessage(final String text, final Long fromUserId, Long toUserId, final MessageSource messageSource) {
        if (toUserId == null || toUserId == 0) {
            toUserId = CHATBOT_ID;
        }

        // save the message in DB
        final User fromUser = userService.getUserById(fromUserId);
        final User toUser = userService.getUserById(toUserId);
        final Sentence sentence = chatbotService.getExistingSentenceOrCreateANewOne(text);
        final Message message = messageService.addMessage(text, fromUser, toUser, sentence, messageSource);
//        final Message previousMessage = messageService.getPreviousMessage(fromUser.getId(), toUser.getId(), message.getId());
//        if (previousMessage != null) {
//            chatbotService.addResponseAndSynonym(previousMessage.getEquivalentSentence(), sentence);
//        }
        return message;
    }

    @Override
    public Message addMessageAndLearn(final String text, final User learningUser1, User learningUser2, final Message previousMessage, final MessageSource messageSource) {
        return addMessageAndSetItAsResponse(text, learningUser1, learningUser2, messageSource, previousMessage);
    }

    @Override
    public ResponseMessageAndInformation addMessageAndIdentifyInformationAndGetResponse(final String text, final Long fromUserId, Long toUserId) {
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
        String informationFieldNamePath = null;
        if (previousMessage != null) {
            informationClass = previousMessage.getEquivalentSentence().getInformationClass();
            informationFieldNamePath = previousMessage.getEquivalentSentence().getInformationFieldNamePath();
        }
        List<Object> updatedInformationValues = null;
        try {
            updatedInformationValues = informationDetectionService.identifyAndSetInformation(informationClass, informationFieldNamePath, message, fromUser);
            if (updatedInformationValues != null) {
                userService.updateUserFirstName(fromUser);
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        // generate a response
        // exceptional case: Andy requested an information and the user didn't answered => ask again (but just one time in 2 hours)
        if (updatedInformationValues == null && previousMessage != null && previousMessage.getEquivalentSentence().getInformationClass() != null // user didn't answered
                && LocalDateTime.now().minusMinutes(MINUTES_TO_WAIT_TO_REQUEST_AGAIN_SAME_INFORMATION)
                .isAfter(messageService.getLastMessageByInformationClassAndInformationFieldNamePath(
                        previousMessage.getFromUser().getId(), previousMessage.getToUser().getId(),
                        previousMessage.getEquivalentSentence().getInformationClass(), previousMessage.getEquivalentSentence().getInformationFieldNamePath())
                        .getDateTime())) { // ask user just 2 times in a row, wait at least 2 hours before asking again
            final AddressingMode addressingMode = message.getFromUser().getAddressingModeStatus().getPreferredAddressingMode();
            final String previousSentenceText = chatbotService.translateSentenceToText(previousMessage.getEquivalentSentence(), addressingMode);
            final String responseText = getRandomRequestAgainText(addressingMode) + " " + previousSentenceText;
            final Message response = messageService.addMessage(responseText, message.getToUser(), message.getFromUser(), previousMessage.getEquivalentSentence(), MessageSource.USER_CHATBOT_CONVERSATION);
            System.out.println("Requestion");
            return new ResponseMessageAndInformation(response, "");
        }

        // normal case
        final Message response = generateResponse(message);

        StringBuilder informationResponse = new StringBuilder();
        if (updatedInformationValues != null) {
            informationResponse.append(updatedInformationValues.get(0).toString());
            for (int i = 1; i < updatedInformationValues.size(); i++) {
                final Object updatedInformationValue = updatedInformationValues.get(i);
                informationResponse.append(", ").append(updatedInformationValue.toString());
            }
            informationResponse.append(" s-a detectat pentru ").append(informationFieldNamePath);
        }

        return new ResponseMessageAndInformation(response, informationResponse.toString());
    }

    private Message addMessageAndSetItAsResponse(final String text, final User fromUser, final User toUser, final MessageSource messageSource, final Message previousMessage) {
        final Sentence sentence = chatbotService.getExistingSentenceOrCreateANewOne(text);

        // save the message in DB
        final Message message = messageService.addMessage(text, fromUser, toUser, sentence, messageSource);

        // add this message as a response for the previous message
        if (previousMessage != null) {
            chatbotService.addResponseAndSynonym(previousMessage.getEquivalentSentence(), sentence);
        }

        return message;
    }

    private Message generateResponse(final Message message) {
        Sentence responseSentence;
        if (chatbotRequestType == GET_INFORMATION_FROM_USER) {
            responseSentence = chatbotService.pickSentenceRequestingInformation(message.getFromUser());
        } else {
            responseSentence = chatbotService.generateResponse(message);
        }

        boolean isUnknownMessage = false;
        if (responseSentence == null) {
            responseSentence = getSentenceAccordingToUserAndRequestType(message.getFromUser(), null);
            isUnknownMessage = true;
        }

        // default, if the response is not a directive also include in response an information request or a sentence with few replies
        Sentence additionalSentence = null;
        if (isUnknownMessage || (chatbotRequestType == DEFAULT && responseSentence.getSpeechType() != DIRECTIVE && random.nextBoolean())) {
            if (random.nextInt(4) == 0) {
                additionalSentence = chatbotService.pickSentenceWithFewReplies();
                System.out.println("BOOL: pickSentenceWithFewReplies            " + isUnknownMessage);
            } else {
                additionalSentence = chatbotService.pickSentenceRequestingInformation(message.getFromUser());
                System.out.println("BOOL: pickSentenceRequestingInformation     " + isUnknownMessage);
            }
        }

        // compose message (response + additional)
        Message responseMessage = null;
        Message additionalMessage = null;
        if (!isUnknownMessage) {
            final String responseText = chatbotService.translateSentenceToText(responseSentence, message.getFromUser().getAddressingModeStatus().getPreferredAddressingMode());
            responseMessage = messageService.addMessage(responseText, message.getToUser(), message.getFromUser(), responseSentence, MessageSource.USER_CHATBOT_CONVERSATION);
            responseMessage.setIsUnknownMessage(false); // always false
        }
        if (additionalSentence != null) {
            final String additionalText = chatbotService.translateSentenceToText(additionalSentence, message.getFromUser().getAddressingModeStatus().getPreferredAddressingMode());
            additionalMessage = messageService.addMessage(additionalText, message.getToUser(), message.getFromUser(), additionalSentence, MessageSource.USER_CHATBOT_CONVERSATION);
        }

        // save the response
        if (additionalMessage != null) {
            if (responseMessage != null) {
                additionalMessage.setText(responseMessage.getText() + ". " + additionalMessage.getText());
            }
            return additionalMessage;
        }
        return responseMessage;
    }

    private String getRandomRequestAgainText(final AddressingMode addressingMode) {
        final List<String> requestAgainSentences = new ArrayList<>();
        if (addressingMode == AddressingMode.FORMAL) {
            requestAgainSentences.add("Nu mi-ați răspuns la întrebare.");
        } else {
            requestAgainSentences.add("Nu mi-ai răspuns la întrebare.");
            requestAgainSentences.add("Ce ?");
        }
        requestAgainSentences.add("Nu înțeleg.");
        requestAgainSentences.add("Nu accept acest răspuns.");
        requestAgainSentences.add("Pardon ?");
        return requestAgainSentences.get(random.nextInt(requestAgainSentences.size()));
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
    public List<Message> getMessages(final Long userId1, Long userId2, final LocalDateTime maxDateTime, final Integer nrOfMessages) {
        if (userId2 == null || userId2 == 0) {
            userId2 = CHATBOT_ID;
        }
        return messageService.getMessagesByUsers(userId1, userId2, maxDateTime, nrOfMessages);
    }

    @Override
    @Transactional
    public Message requestMessageFromChatbot(final Long userId, ChatbotRequestType chatbotRequestType) {
        final User fromUser = userService.getUserById(CHATBOT_ID);
        final User toUser = userService.getUserById(userId);

        final Message lastMessage = messageService.getLastMessageOfUsers(fromUser.getId(), toUser.getId());
        final Sentence sentence;

        // if requestType is default and passed >30 minutes from last conversation => greets again
        if ((chatbotRequestType == null || chatbotRequestType == DEFAULT) &&
                lastMessage != null && LocalDateTime.now().minusMinutes(30).isAfter(lastMessage.getDateTime())) {
            sentence = chatbotService.generateGreetingSentence();
        } else {
            sentence = getSentenceAccordingToUserAndRequestType(toUser, chatbotRequestType);
        }
        return messageService.addMessage(chatbotService.translateSentenceToText(sentence, toUser.getAddressingModeStatus().getPreferredAddressingMode()),
                fromUser, toUser, sentence, MessageSource.USER_CHATBOT_CONVERSATION);
    }

    /**
     * @param chatbotRequestType only LEARN_TO_SPEAK and GET_INFORMATION_FROM_USER, otherwise return a random one
     */
    private Sentence getSentenceAccordingToUserAndRequestType(final User toUser, ChatbotRequestType chatbotRequestType) {
        if (chatbotRequestType == null) {
            chatbotRequestType = this.chatbotRequestType;
        }
        switch (chatbotRequestType) {
            case LEARN_TO_SPEAK:
                return chatbotService.pickSentenceWithFewReplies();
            case GET_INFORMATION_FROM_USER:
                return chatbotService.pickSentenceRequestingInformation(toUser);
            default:
                return chatbotService.pickRandomSentence();
        }
    }
}
