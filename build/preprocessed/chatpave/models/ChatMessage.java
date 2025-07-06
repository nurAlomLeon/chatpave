/*
 * Located in: src/chatpave/models/ChatMessage.java
 */
package chatpave.models;

/**
 * A simple data model to hold information about a single chat message
 * within a conversation.
 */
public class ChatMessage {
    public String content;
    public boolean isSentByMe; // To determine if the bubble is on the left or right
    public String timestamp;

    public ChatMessage(String content, boolean isSentByMe, String timestamp) {
        this.content = content;
        this.isSentByMe = isSentByMe;
        this.timestamp = timestamp;
    }
}
