package controllers;

import dtos.MessageDto;
import dtos.RequestSendMessageDto;
import facades.api.MessageFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController extends AbstractController {
    private final MessageFacade messageFacade;

    @Autowired
    public MessageController(MessageFacade messageFacade) {
        this.messageFacade = messageFacade;
    }

    @GetMapping
    public ResponseEntity<List<MessageDto>> getMessages(@RequestParam final Long userId1, @RequestParam final Long userId2) {
        final List<MessageDto> messages = messageFacade.getMessages(userId1, userId2);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@RequestBody @Validated final RequestSendMessageDto requestSendMessageDto) {
        final MessageDto response = messageFacade.addMessage(requestSendMessageDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/sample")
    public ResponseEntity<MessageDto> requestMessageFromChatbot(@RequestParam final Long userId) {
        final MessageDto message = messageFacade.requestMessageFromChatbot(userId);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }
}
