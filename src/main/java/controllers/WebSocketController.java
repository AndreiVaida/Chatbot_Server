package controllers;

import dtos.MessageDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController extends AbstractController {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public MessageDto greeting(MessageDto message) throws Exception {
        return new MessageDto(1L, 2L, "Hello, " + HtmlUtils.htmlEscape(message.getMessage()) + "!", null);
    }

}
