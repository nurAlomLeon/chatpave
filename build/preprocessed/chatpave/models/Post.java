/*
 * Located in: src/chatpave/models/Post.java
 */
package chatpave.models;

import javax.microedition.lcdui.Image;

/**
 * A data model to hold all information about a single news feed post.
 * Now includes support for an image.
 */
public class Post {
    public int postId;
    public String authorUsername;
    public String content;
    public String timestamp;
    public int likeCount;
    public int commentCount;
    public boolean isLikedByMe;
    
    // --- NEW: Fields for image handling ---
    public String imageUrl; // The URL of the image to be loaded
    public Image image;     // The actual Image object, null until loaded

    public Post(int postId, String authorUsername, String content, String timestamp, int likeCount, int commentCount, boolean isLikedByMe, String imageUrl) {
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLikedByMe = isLikedByMe;
        this.imageUrl = imageUrl;
        this.image = null; // Image is initially null
    }
}
