package controllers;

import domain.enums.ChatbotRequestType;
import dtos.MessageDto;
import dtos.RequestSendMessageDto;
import dtos.ResponseMessageAndInformationDto;
import facades.api.ChatFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import services.api.UserPermissionService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("chat")
public class ChatController extends AbstractController {
    private final ChatFacade chatFacade;
    private final UserPermissionService userPermissionService;

    @Autowired
    public ChatController(ChatFacade chatFacade, UserPermissionService userPermissionService) {
        this.chatFacade = chatFacade;
        this.userPermissionService = userPermissionService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<MessageDto>> getAllMessages(@RequestParam final Long userId1, @RequestParam final Long userId2) {
        if (!(userPermissionService.hasUserAccess(userId1) || userPermissionService.hasUserAccess(userId2))) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        final List<MessageDto> messages = chatFacade.getMessages(userId1, userId2);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<MessageDto>> getMessages(@RequestParam final Long userId1, @RequestParam final Long userId2,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime maxDateTime,
                                                        @RequestParam(defaultValue = "20") final Integer nrOfMessages,
                                                        @RequestParam(defaultValue = "true") final boolean includeMaxMessage) {
        if (!(userPermissionService.hasUserAccess(userId1) || userPermissionService.hasUserAccess(userId2))) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        if (maxDateTime == null) {
            maxDateTime = LocalDateTime.now();
        }
        final List<MessageDto> messages = chatFacade.getMessages(userId1, userId2, maxDateTime, includeMaxMessage, nrOfMessages);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ResponseMessageAndInformationDto> sendMessage(@RequestBody @Validated final RequestSendMessageDto requestSendMessageDto) {
        if (!userPermissionService.hasUserAccess(requestSendMessageDto.getFromUserId())) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        final ResponseMessageAndInformationDto response = chatFacade.addMessage(requestSendMessageDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/guest")
    public ResponseEntity<MessageDto> sendMessageFromGuest(@RequestBody @NotNull final String message) {
        final MessageDto response = chatFacade.addMessageFromGuest(message);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/sample")
    public ResponseEntity<MessageDto> requestMessageFromChatbot(@RequestParam final Long userId, @Nullable @RequestParam ChatbotRequestType chatbotRequestType) {
        if (userId != null && !userPermissionService.hasUserAccess(userId)) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        final MessageDto message = chatFacade.requestMessageFromChatbot(userId, chatbotRequestType);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
