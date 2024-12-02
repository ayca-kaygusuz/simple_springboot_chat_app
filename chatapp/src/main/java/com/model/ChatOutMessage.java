package com.model;

import java.util.*;

public class ChatOutMessage {

    private String message;
    private String groupName;
    private Date timestamp;

    // constructors
    public ChatOutMessage() {
		
	}
    public ChatOutMessage(String message) {
        this.message = message;
    }

    
    // getters and setters
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
