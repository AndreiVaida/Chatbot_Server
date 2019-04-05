package services.impl;

import domain.entities.ConceptMessage;
import domain.entities.Conversation;
import domain.entities.Message;
import domain.entities.User;
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
    public Message addMessage(final String textMessage, final Long fromUserId, Long toUserId) {
        // save the message chat
        if (toUserId == null || toUserId == 0) {
            toUserId = CHATBOT_ID;
        }
        final User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        final User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        final Message message = new Message();
        message.setFromUser(fromUser);
        message.setToUser(toUser);
        message.setMessage(textMessage);
        message.setDateTime(LocalDateTime.now());
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

        return response;
    }

    /**
     * Assign a suitable concept message for the given message.
     * If no suitable concept message is found, it's created a new one.
     * Save the message and the concept message in repository.
     */
    private void assignConceptMessage(final Message message) {
        ConceptMessage conceptMessage = detectConceptMessage(message);
        if (conceptMessage == null) {
            conceptMessage = new ConceptMessage();
            conceptMessage.getEquivalentMessages().add(message);
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

    private Message getPreviousMessageFromHuman(final Message message) {
        final List<Message> previousMessages = messageRepository.getPreviousMessagesFromHumans(message.getFromUser().getId(), message.getToUser().getId(),
                message.getId(), CHATBOT_ID, PageRequest.of(0, 1));
        if (!previousMessages.isEmpty()) {
            return previousMessages.get(0);
        }
        return null;
    }

    private void addReply(final Message message, final Message reply) {
        if (message.getConceptMessage() != null && reply.getConceptMessage() != null) {
            final ConceptMessage conceptMessage = message.getConceptMessage();
            final Set<ConceptMessage> responses = conceptMessage.getResponses();
            responses.add(reply.getConceptMessage());
            conceptMessage.setResponses(responses); // TODO: don't add the reply if the message already contains it (the same text message)
            conceptMessageRepository.save(conceptMessage);
        }
    }

    private Message generateResponse(final Message message) {
        if (message.getConceptMessage() == null || message.getConceptMessage().getResponses().isEmpty()) {
            return getUnknownResponse(message.getToUser(), message.getFromUser(), message.getMessage());
        }
        // get a random concept message response
        final Set<ConceptMessage> conceptMessagesWithMessagesFromHuman = message.getConceptMessage().getResponses().stream()
                .filter(cm -> cm.getEquivalentMessages().stream().anyMatch(m -> !m.getFromUser().getId().equals(CHATBOT_ID)))
                .collect(Collectors.toSet());
        int index = random.nextInt(conceptMessagesWithMessagesFromHuman.size());
        final Optional<ConceptMessage> responseConceptMessage = conceptMessagesWithMessagesFromHuman.stream()
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
        response.setIsUnknownMessage(true);
        final int randomNumber = random.nextInt(2);
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
     * @return best suitable ConceptMessage for the given message or null.
     */
    private ConceptMessage detectConceptMessage(final Message message) {
        // find equivalent messages in DB
        final List<String> words = Arrays.asList(message.getMessage().split("\\W+"));
        final Set<Message> matchedMessagesSet = new HashSet<>();
        for (String word : words) {
            matchedMessagesSet.addAll(messageRepository.findAllByMessageLikeIgnoreCaseAndIdNotAndIsUnknownMessageNot(word, message.getId(), true));
        }
        if (matchedMessagesSet.isEmpty()) {
            return null;
        }
        // sort founded messages by nr. of matched words
        final int[] bestCount = {0};
        final List<Message> sortedMessages = matchedMessagesSet.stream()
                .sorted((Message m1, Message m2) -> {
                    final Integer nrOfMatchedWordsM1 = getNrOfPointsOfMatchedWords(words, Arrays.asList(m1.getMessage().split("\\W+")));
                    final Integer nrOfMatchedWordsM2 = getNrOfPointsOfMatchedWords(words, Arrays.asList(m2.getMessage().split("\\W+")));
                    if (bestCount[0] < nrOfMatchedWordsM1) {
                        bestCount[0] = nrOfMatchedWordsM1;
                    }
                    if (bestCount[0] < nrOfMatchedWordsM2) {
                        bestCount[0] = nrOfMatchedWordsM2;
                    }
                    return nrOfMatchedWordsM2.compareTo(nrOfMatchedWordsM1);
                })
                .collect(Collectors.toList());
        // return the best concept message or null if we didn't find a good one
        if (bestCount[0] < words.size() / 2) {
            return null;
        }
        return sortedMessages.get(random.nextInt(sortedMessages.size())).getConceptMessage();
    }

    private int getNrOfPointsOfMatchedWords(final List<String> list1, final List<String> list2) {
        list1.replaceAll(String::toUpperCase);
        list2.replaceAll(String::toUpperCase);
        int nrOfMatches = 0;
        for (String word : list1) {
            if (partialContains(list2, word)) {
                nrOfMatches++;
            }
        }
        int nrOfExtraWords = 0;
        for (String word : list2) {
            if (!partialContains(list1, word)) {
                if (random.nextBoolean()) {
                    nrOfExtraWords--;
                }
            }
        }
        return nrOfMatches - nrOfExtraWords/2;
    }

    private boolean partialContains(final List<String> words, final String wordToFind) {
        for (String word : words) {
            if (word.equals(wordToFind) || word.contains(wordToFind) || wordToFind.contains(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public List<Message> getMessages(final Long userId1, Long userId2) {
        if (!userRepository.existsById(userId1)) {
            throw new EntityNotFoundException("User 1 not found.");
        }
        if (userId2 == null || userId2 == 0) {
            userId2 = CHATBOT_ID;
        }
        if (!userRepository.existsById(userId2)) {
            throw new EntityNotFoundException("User 2 not found.");
        }

        return messageRepository.findAllByUsers(userId1, userId2);
    }

    @Override
    @Transactional
    public Message requestMessageFromChatbot(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        final User chatbot = userRepository.findById(CHATBOT_ID)
                .orElseThrow(() -> new EntityNotFoundException("Chatbot not found."));

        final Message message = getRandomMessageWithLessResponses(user, chatbot);
        messageRepository.save(message);
        return message;
    }

    @Override
    @Transactional
    public Message getMessageById(Long id) {
        return messageRepository.getOne(id);
    }

    private Message getRandomMessageWithLessResponses(final User user, final User chatbot) {
        // sort concept messages by no. of responses they have
        final List<ConceptMessage> conceptMessages = conceptMessageRepository.findAll()
                .stream()
                .filter(cm ->
                        // keep only those concept messages which has at least 1 message from human
                        cm.getEquivalentMessages().stream().anyMatch(message -> !message.getFromUser().getId().equals(CHATBOT_ID))
                )
                .sorted(Comparator.comparingInt(cm -> cm.getResponses().size()))
                .collect(Collectors.toList());
        if (conceptMessages.isEmpty()) {
            return getUnknownResponse(chatbot, user, "Nu știu ce să zic...");
        }
        // pick a random concept message
        int index = random.nextInt(conceptMessages.size() / 2 + 1);
        final Optional<ConceptMessage> optionalConceptMessage = conceptMessages.stream()
                .skip(index)
                .findFirst();
        if (!optionalConceptMessage.isPresent()) {
            return getUnknownResponse(chatbot, user, "Nu știu ce să zic...");
        }
        // pick a random message
        index = random.nextInt(optionalConceptMessage.get().getEquivalentMessages().size());
        final Optional<Message> optionalMessage = optionalConceptMessage.get().getEquivalentMessages().stream()
                .skip(index)
                .findFirst();
        if (!optionalMessage.isPresent()) {
            return getUnknownResponse(chatbot, user, "Nu știu ce să zic...");
        }
        // duplicate and customize the message
        return createReplyFromMessage(optionalMessage.get(), chatbot, user);
    }
}
