package com.controller;

import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;
//import org.springframework.web.util.HtmlUtils;

import com.model.ChatInMessage;
import com.model.ChatOutMessage;

@Controller
public class ChatController {
    
    @MessageMapping("/guestchat")
    @SendTo("/topic/guestchats")
    public ChatOutMessage handleMessaging(ChatInMessage message) throws Exception{

        // simulate delay
        Thread.sleep(1000);

        return new ChatOutMessage(message.getMessage());
    }

    // handling user typing
    @MessageMapping("/guestupdate")
    @SendTo("/topic/guestupdates")
    public ChatOutMessage handleUserIsTyping(ChatInMessage message) throws Exception{
        return new ChatOutMessage("Someone is typing...");
    }

}
