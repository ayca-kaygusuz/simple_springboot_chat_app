package com.controller;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

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

    @MessageExceptionHandler
    @SendTo("/topic/errors")
    public ChatOutMessage handleException(Throwable exception){

        ChatOutMessage manualErrorHandle = new ChatOutMessage("An error has occurred.");
        
        return manualErrorHandle;

    }

}
