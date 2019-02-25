package controllers;

import dtos.MessageDto;
import dtos.RequestAddMessageDto;
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
import services.api.MessageService;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController extends AbstractController {
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<MessageDto>> getMessages(@RequestParam final Long userId1, @RequestParam final Long userId2) {
        final List<MessageDto> messages = messageService.getMessages(userId1, userId2);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@RequestBody @Validated final RequestAddMessageDto requestSendMessageDto) {
        final MessageDto sentMessage = messageService.addMessage(requestSendMessageDto);
        return new ResponseEntity<>(sentMessage, HttpStatus.OK);
    }
}
