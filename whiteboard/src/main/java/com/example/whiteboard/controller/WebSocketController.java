package com.example.whiteboard.controller;

import com.example.whiteboard.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate template;

    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/update/{classCode}")
    public void sendUpdate(@DestinationVariable(value = "classCode") String classCode, Message message) {
        template.convertAndSend("/sub/class/" + classCode, message);
        log.info("update : {} ", message);
    }

    @MessageMapping("/join/{classCode}")
    public void joinClassroom(@DestinationVariable(value = "classCode") String classCode, Message message,
                              SimpMessageHeaderAccessor headerAccessor) {
        message.setSessionId(headerAccessor.getSessionId());
        template.convertAndSend("/sub/class/" + classCode + "/join", message);
    }

    @MessageMapping("/leave/{classCode}")
    public void leaveClassroom(@DestinationVariable(value = "classCode") String classCode, Message message,
                               SimpMessageHeaderAccessor headerAccessor) {
        message.setSessionId(headerAccessor.getSessionId());
        template.convertAndSend("/sub/class/" + classCode + "/leave", message);
    }
}
