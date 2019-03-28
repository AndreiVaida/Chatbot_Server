package services.impl;

import domain.entities.ConceptMessage;
import domain.entities.Conversation;
import domain.entities.Message;
import domain.entities.User;
import dtos.MessageDto;
import dtos.RequestSendMessageDto;
import mappers.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import repositories.ConceptMessageRepository;
import repositories.ConversationRepository;
import repositories.MessageRepository;
import repositories.UserRepository;
import services.api.MessageService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static app.Main.CHATBOT_ID;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConceptMessageRepository conceptMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final Random random;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, ConceptMessageRepository conceptMessageRepository, ConversationRepository conversationRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.conceptMessageRepository = conceptMessageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        random = new Random();
    }

    @Override
    @Transactional
    public MessageDto addMessage(final RequestSendMessageDto requestSendMessageDto) {
        // save the message chat
        if (requestSendMessageDto.getToUserId() == null || requestSendMessageDto.getToUserId() == 0) {
            requestSendMessageDto.setToUserId(CHATBOT_ID);
        }
        final User fromUser = userRepository.findById(requestSendMessageDto.getFromUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        final User toUser = userRepository.findById(requestSendMessageDto.getToUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        final Message message = MessageMapper.requestAddMessageDtoToMessage(requestSendMessageDto, fromUser, toUser, LocalDateTime.now());
        messageRepository.save(message);

        // AI part
        assignConceptMessage(message);
        // set this message as a reply of the previous message
        final Message previousMessage = getPreviousMessage(message);
        if (previousMessage != null) {
            addReply(previousMessage, message);
        }
        // process a reply to user
        final Message response = generateResponse(message);
        messageRepository.save(response);

        return MessageMapper.messageToMessageDto(response);
    }

    private void assignConceptMessage(final Message message) {
        ConceptMessage conceptMessage = detectConceptMessage(message);
        if (conceptMessage == null) {
            conceptMessage = new ConceptMessage();
            conceptMessage.getEquivalentMessages().add(message);
            conceptMessageRepository.save(conceptMessage);
        }
        message.setConceptMessage(conceptMessage);
        conceptMessage.getEquivalentMessages().add(message);
        messageRepository.save(message);
        conceptMessageRepository.save(conceptMessage);
    }

    private Message getPreviousMessage(final Message message) {
        final List<Message> previousMessages = messageRepository.getPreviousMessages(message.getFromUser().getId(), message.getToUser().getId(),
                message.getId(), PageRequest.of(0, 1));
        if (!previousMessages.isEmpty()) {
            return previousMessages.get(0);
        }
        return null;
    }

    private void addReply(final Message message, final Message reply) {
        if (message.getConceptMessage() != null && reply.getConceptMessage() != null) {
            message.getConceptMessage().getResponses().add(reply.getConceptMessage());
        }
    }

    private Message generateResponse(final Message message) {
        if (message.getConceptMessage() == null || message.getConceptMessage().getResponses().isEmpty()) {
            return getUnknownResponse(message.getToUser(), message.getFromUser(), message.getMessage());
        }
        // get a random concept message response
        int index = random.nextInt(message.getConceptMessage().getResponses().size());
        final Optional<ConceptMessage> responseConceptMessage = message.getConceptMessage().getResponses()
                .stream()
                .skip(index)
                .findFirst();
        if (!responseConceptMessage.isPresent()) {
            return getUnknownResponse(message.getToUser(), message.getFromUser(), message.getMessage());
        }
        // get a random message response
        index = random.nextInt(responseConceptMessage.get().getEquivalentMessages().size());
        final Optional<Message> responseMessageOptional = responseConceptMessage.get().getEquivalentMessages()
                .stream()
                .skip(index)
                .findFirst();
        if (!responseMessageOptional.isPresent()) {
            return getUnknownResponse(message.getToUser(), message.getFromUser(), message.getMessage());
        }

        final Message responseMessage = responseMessageOptional.get();
        return createReplyFromMessage(responseMessage, responseMessage.getToUser(), responseMessage.getFromUser());
    }

    private Message createReplyFromMessage(final Message message, final User fromUser, final User toUser) {
        final Message messageCopy = new Message();
        messageCopy.setFromUser(fromUser);
        messageCopy.setToUser(toUser);
        messageCopy.setMessage(message.getMessage());
        messageCopy.setDateTime(LocalDateTime.now());
        messageCopy.setConceptMessage(message.getConceptMessage());
        messageCopy.setMessageType(message.getMessageType());
        return messageCopy;
    }

    private ConceptMessage getNextConceptMessageFromConversation(final ConceptMessage conceptMessage) {
        final List<Conversation> conversations = conversationRepository.findAllByConceptMessageList(conceptMessage);
        for (Conversation conversation : conversations) {
            for (int i = 0; i < conversation.getConceptMessageList().size() - 1; i++) {
                final ConceptMessage c = conversation.getConceptMessageList().get(i);
                if (c.equals(conceptMessage)) {
                    return conversation.getConceptMessageList().get(i + 1);
                }
            }
        }
        return null;
    }

    private Message getUnknownResponse(final User fromUser, final User toUser, final String message) {
        final Message response = new Message();
        response.setToUser(toUser);
        response.setFromUser(fromUser);
        response.setDateTime(LocalDateTime.now());
        final int randomNumber = random.nextInt(3);
        switch (randomNumber) {
            case 0: {
                response.setMessage("Nu știu.");
                break;
            }
            case 1: {
                response.setMessage("Nu știu ce vrei să spui prin „" + message + "”.");
                break;
            }
            case 2: {
                response.setMessage("?");
                break;
            }
            default: {
                response.setMessage(message);
            }
        }
        return response;
    }

    /**
     * @return best detected ConceptMessage or null.
     */
    private ConceptMessage detectConceptMessage(final Message message) {
        // find equivalent messages in DB
        final List<String> words = Arrays.asList(message.getMessage().split("\\W+"));
        final Set<Message> matchedMessagesSet = new HashSet<>();
        for (String word : words) {
            matchedMessagesSet.addAll(messageRepository.findAllByMessage(word));
        }
        if (matchedMessagesSet.size() == 0) {
            return null;
        }
        // sort founded messages by nr. of matched words
        final int[] bestCount = {0};
        final List<Message> sortedMessages = matchedMessagesSet.stream()
                .sorted((Message m1, Message m2) -> {
                    final Integer nrOfMatchedWordsM1 = getNrOfPointsOfMatchedWords(words, Arrays.asList(m1.getMessage().split("\\W+")));
                    final Integer nrOfMatchedWordsM2 = getNrOfPointsOfMatchedWords(words, Arrays.asList(m2.getMessage().split("\\W+")));
                    if (bestCount[0] < nrOfMatchedWordsM1) { bestCount[0] = nrOfMatchedWordsM1; }
                    if (bestCount[0] < nrOfMatchedWordsM2) { bestCount[0] = nrOfMatchedWordsM2; }
                    return nrOfMatchedWordsM2.compareTo(nrOfMatchedWordsM1);
                })
                .collect(Collectors.toList());
        // return the bet found message or nothing
        final List<ConceptMessage> matchedConceptMessages = conceptMessageRepository.findAllByEquivalentMessages(sortedMessages.get(0));
        if (matchedConceptMessages.size() == 0) {
            return null;
        }
        // return the best concept message or null if we didn't find a good one
        if (bestCount[0] < words.size() / 2) {
            return null;
        }
        return matchedConceptMessages.get(random.nextInt(matchedConceptMessages.size()));
    }

    private int getNrOfPointsOfMatchedWords(final List<String> list1, final List<String> list2) {
        list1.replaceAll(String::toUpperCase);
        list2.replaceAll(String::toUpperCase);
        int nrOfMatches = 0;
        for (String word : list1) {
            if (list2.contains(word)) {
                nrOfMatches++;
            }
        }
        for (String word : list2) {
            if (!list1.contains(word)) {
                nrOfMatches--;
            }
        }
        return nrOfMatches;
    }

    private Message getRandomElement(final Set<Message> messageSet) {
        if (messageSet.size() == 0) {
            return null;
        }
        int index = random.nextInt(messageSet.size());
        final Iterator<Message> iterator = messageSet.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    @Override
    @Transactional
    public List<MessageDto> getMessages(final Long userId1, Long userId2) {
        if (!userRepository.existsById(userId1)) {
            throw new EntityNotFoundException("User 1 not found.");
        }
        if (userId2 == null || userId2 == 0) {
            userId2 = CHATBOT_ID;
        }
        if (!userRepository.existsById(userId2)) {
            throw new EntityNotFoundException("User 2 not found.");
        }

        final List<Message> messages = messageRepository.findAllByUsers(userId1, userId2);
        return messages.stream()
                .map(MessageMapper::messageToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageDto requestMessageFromChatbot(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        final User chatbot = userRepository.findById(CHATBOT_ID)
                .orElseThrow(() -> new EntityNotFoundException("Chatbot not found."));

        final Message message = getRandomMessageWithLessResponses(user, chatbot);
        messageRepository.save(message);
        return MessageMapper.messageToMessageDto(message);
    }

    private Message getRandomMessageWithLessResponses(final User user, final User chatbot) {
        // sort concept messages by no. of responses they have
        final List<ConceptMessage> conceptMessages = conceptMessageRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(cm -> cm.getResponses().size()))
                .collect(Collectors.toList());
        if (conceptMessages.size() == 0) {
            return getUnknownResponse(chatbot, user, "Nu știu ce să zic...");
        }
        // pick a random concept message
        int index = random.nextInt(conceptMessages.size() / 10 + 1);
        final Optional<ConceptMessage> optionalConceptMessage = conceptMessages
                .stream()
                .skip(index)
                .findFirst();
        if (!optionalConceptMessage.isPresent()) {
            return getUnknownResponse(chatbot, user, "Nu știu ce să zic...");
        }
        // pick a random message
        index = random.nextInt(optionalConceptMessage.get().getEquivalentMessages().size());
        final Optional<Message> optionalMessage = optionalConceptMessage.get().getEquivalentMessages()
                .stream()
                .skip(index)
                .findFirst();
        if (!optionalMessage.isPresent()) {
            return getUnknownResponse(chatbot, user, "Nu știu ce să zic...");
        }
        // duplicate and customize the message
        final Message message = optionalMessage.get();
        return createReplyFromMessage(message, chatbot, user);
    }
}
