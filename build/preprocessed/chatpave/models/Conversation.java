/*
 * Located in: src/chatpave/models/Conversation.java
 * (You may need to create the 'models' package/folder)
 */
package chatpave.models;

/**
 * A simple data model to hold information about a single conversation thread
 * for display in the main message list.
 */
public class Conversation {
    public int partnerId;
    public String partnerUsername;
    public String lastMessage;
    public String timestamp;
    public int unreadCount;

    public Conversation(int partnerId, String partnerUsername, String lastMessage, String timestamp, int unreadCount) {
        this.partnerId = partnerId;
        this.partnerUsername = partnerUsername;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
    }
}
