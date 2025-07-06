/*
 * Located in: src/chatpave/ui/NewsFeedScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import chatpave.models.Post;
import chatpave.net.ApiClient;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;

/**
 * A Canvas to display a fully interactive news feed with scrolling, liking, and photos.
 */
public class NewsFeedScreen extends Canvas implements Runnable {

    private Vector posts = new Vector();
    private boolean isLoading = true;
    private int selectedIndex = 0;
    private int topY = 0;
    private Hashtable postYPositions = new Hashtable();

    private int currentPage = 1;
    private boolean hasMorePosts = true;
    private boolean isLoadingMore = false;

    private Hashtable imageCache = new Hashtable();
    private Vector imageQueue = new Vector();
    private Thread imageLoaderThread;

    // --- UI Constants ---
    private static final int COLOR_BACKGROUND = 0xFFFFFF;
    private static final int COLOR_CARD_BG = 0xEFEFEF;
    private static final int COLOR_TEXT_DARK = 0x333333;
    private static final int COLOR_TEXT_SECONDARY = 0x777777;
    private static final int COLOR_FOCUS_HIGHLIGHT = 0x3B5998;
    private static final int COLOR_APP_BAR_TOP = 0x6D84B4;
    private static final int COLOR_APP_BAR_BOTTOM = 0x4A6398;
    private static final int APP_BAR_HEIGHT = 40;
    private static final int POST_PADDING = 10;
    private static final int ACTION_BAR_HEIGHT = 30;
    private static final int LOAD_MORE_HEIGHT = 40;

    public NewsFeedScreen() {
        setFullScreenMode(true);
        new Thread(this).start();
    }

    public void run() {
        isLoading = true;
        repaint();
        Vector initialPosts = ApiClient.getNewsFeed(1);
        
        // --- NEW: Check for API errors ---
        if (initialPosts == null) {
            // If the API call failed, show the debug screen with the log
            DisplayManager.showDebugScreen("API Error Log", ApiClient.debugLog.toString());
            return; // Stop processing
        }
        
        posts = initialPosts;
        if (initialPosts.size() < 10) hasMorePosts = false;
        queueImagesForLoading();
        
        isLoading = false;
        repaint();
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(COLOR_BACKGROUND);
        g.fillRect(0, 0, width, height);
        drawAppBar(g, "News Feed");

        g.translate(0, -topY);

        if (isLoading) {
            g.setColor(COLOR_TEXT_DARK);
            g.drawString("Loading feed...", width / 2, height / 2 + topY, Graphics.HCENTER | Graphics.BASELINE);
        } else if (posts.isEmpty()) {
            g.setColor(COLOR_TEXT_DARK);
            g.drawString("No posts in feed.", width / 2, height / 2 + topY, Graphics.HCENTER | Graphics.BASELINE);
        } else {
            int currentY = APP_BAR_HEIGHT + 5;
            postYPositions.clear();
            for (int i = 0; i < posts.size(); i++) {
                postYPositions.put(new Integer(i), new Integer(currentY));
                Post post = (Post) posts.elementAt(i);
                int postHeight = drawPostCard(g, post, currentY, i == selectedIndex);
                currentY += postHeight + 10;
            }
            if (hasMorePosts) {
                postYPositions.put(new Integer(posts.size()), new Integer(currentY));
                drawLoadMore(g, currentY);
            }
        }
        g.translate(0, topY);
    }

    private int getPostHeight(Post post) {
        Font contentFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        int width = getWidth();
        int contentWidth = width - (POST_PADDING * 4);
        Vector wrappedContent = wrapText(post.content, contentFont, contentWidth);
        int contentHeight = wrappedContent.size() * contentFont.getHeight();
        int imageHeight = 0;
        if (post.image != null) {
            imageHeight = post.image.getHeight() + 5;
        } else if (post.imageUrl != null) {
            imageHeight = 105;
        }
        return 30 + contentHeight + ACTION_BAR_HEIGHT + imageHeight + (POST_PADDING * 2);
    }

    private int drawPostCard(Graphics g, Post post, int y, boolean isSelected) {
        int width = getWidth();
        int cardHeight = getPostHeight(post);
        Font contentFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        Font metaFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);

        g.setColor(COLOR_CARD_BG);
        g.fillRect(5, y, width - 10, cardHeight);
        if (isSelected) {
            g.setColor(COLOR_FOCUS_HIGHLIGHT);
            g.drawRect(4, y - 1, width - 9, cardHeight + 1);
        }

        g.setColor(COLOR_TEXT_DARK);
        g.setFont(metaFont);
        g.drawString(post.authorUsername, POST_PADDING, y + POST_PADDING, Graphics.LEFT | Graphics.TOP);
        
        int textY = y + POST_PADDING + 20;
        
        if (post.image != null) {
            g.drawImage(post.image, width / 2, textY, Graphics.HCENTER | Graphics.TOP);
            textY += post.image.getHeight() + 5;
        } else if (post.imageUrl != null) {
            g.setColor(0xCCCCCC);
            g.fillRect(width/2 - 60, textY, 120, 100);
            g.setColor(0x777777);
            g.drawString("Loading...", width/2, textY + 50, Graphics.HCENTER | Graphics.BASELINE);
            textY += 105;
        }

        g.setFont(contentFont);
        Vector wrappedContent = wrapText(post.content, contentFont, width - (POST_PADDING * 4));
        for (int i = 0; i < wrappedContent.size(); i++) {
            g.drawString((String) wrappedContent.elementAt(i), POST_PADDING * 2, textY + (i * contentFont.getHeight()), Graphics.LEFT | Graphics.TOP);
        }
        
        drawActionBar(g, post, y + cardHeight - ACTION_BAR_HEIGHT);
        
        return cardHeight;
    }
    
    private void drawActionBar(Graphics g, Post post, int y) {
        g.setColor(post.isLikedByMe ? COLOR_FOCUS_HIGHLIGHT : COLOR_TEXT_SECONDARY);
        g.drawString("Like (" + post.likeCount + ")", 20, y + 5, Graphics.LEFT | Graphics.TOP);
        g.setColor(COLOR_TEXT_SECONDARY);
        g.drawString("Comment (" + post.commentCount + ")", getWidth() / 2, y + 5, Graphics.HCENTER | Graphics.TOP);
    }

    private void drawLoadMore(Graphics g, int y) {
        int width = getWidth();
        boolean isSelected = (selectedIndex == posts.size());
        if (isSelected) {
            g.setColor(COLOR_FOCUS_HIGHLIGHT);
            g.fillRect(5, y, width - 10, LOAD_MORE_HEIGHT);
        }
        g.setColor(COLOR_TEXT_DARK);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        String text = isLoadingMore ? "Loading..." : "Load More";
        g.drawString(text, width / 2, y + (LOAD_MORE_HEIGHT / 2), Graphics.HCENTER | Graphics.VCENTER);
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        int maxIndex = hasMorePosts ? posts.size() : posts.size() - 1;

        if (gameAction == UP) {
            if (selectedIndex > 0) selectedIndex--;
        } else if (gameAction == DOWN) {
            if (selectedIndex < maxIndex) selectedIndex++;
        } else if (gameAction == FIRE) {
            handleFireAction();
        } else if (keyCode == -7 || keyCode == -21) {
            DisplayManager.showHomeScreen();
        }
        
        ensureSelectionIsVisible();
        repaint();
    }
    
    private void handleFireAction() {
        if (selectedIndex >= posts.size()) {
            if (hasMorePosts && !isLoadingMore) loadMorePosts();
        } else {
            toggleLikeOnSelectedPost();
        }
    }
    
    private void toggleLikeOnSelectedPost() {
        final Post post = (Post) posts.elementAt(selectedIndex);
        post.isLikedByMe = !post.isLikedByMe;
        post.likeCount += post.isLikedByMe ? 1 : -1;
        repaint();
        
        new Thread(new Runnable() {
            public void run() { ApiClient.toggleLike(post.postId); }
        }).start();
    }

    private void ensureSelectionIsVisible() {
        Integer postYInteger = (Integer) postYPositions.get(new Integer(selectedIndex));
        if (postYInteger == null) return;
        int postY = postYInteger.intValue();
        int postHeight = (selectedIndex < posts.size()) ? getPostHeight((Post) posts.elementAt(selectedIndex)) : LOAD_MORE_HEIGHT;
        int screenHeight = getHeight();

        if (postY < topY) {
            topY = postY;
        } else if (postY + postHeight > topY + screenHeight - APP_BAR_HEIGHT) {
            topY = postY + postHeight - (screenHeight - APP_BAR_HEIGHT);
        }
    }
    
    private void loadMorePosts() {
        isLoadingMore = true;
        repaint();
        new Thread(new Runnable() {
            public void run() {
                currentPage++;
                Vector newPosts = ApiClient.getNewsFeed(currentPage);
                if (newPosts != null && !newPosts.isEmpty()) {
                    for (int i = 0; i < newPosts.size(); i++) {
                        posts.addElement(newPosts.elementAt(i));
                    }
                    if (newPosts.size() < 10) hasMorePosts = false;
                    queueImagesForLoading();
                } else {
                    hasMorePosts = false;
                }
                isLoadingMore = false;
                repaint();
            }
        }).start();
    }
    
    private void queueImagesForLoading() {
        if (imageLoaderThread != null && imageLoaderThread.isAlive()) {
            return; // Loader is already running
        }
        for (int i = 0; i < posts.size(); i++) {
            Post p = (Post) posts.elementAt(i);
            if (p.imageUrl != null && p.image == null && !imageQueue.contains(p.imageUrl)) {
                imageQueue.addElement(p.imageUrl);
            }
        }
        imageLoaderThread = new Thread(new Runnable() {
            public void run() {
                while (!imageQueue.isEmpty()) {
                    String url = (String) imageQueue.firstElement();
                    imageQueue.removeElementAt(0);
                    try {
                        HttpConnection hc = (HttpConnection) Connector.open(url);
                        InputStream is = hc.openInputStream();
                        Image img = Image.createImage(is);
                        is.close();
                        hc.close();
                        
                        // Scale image down if it's too large
                        int maxWidth = getWidth() - (POST_PADDING * 4);
                        if (img.getWidth() > maxWidth) {
                            // img = ImageUtil.resizeImage(img, maxWidth, 1000);
                        }
                        
                        imageCache.put(url, img);
                        updatePostsWithImage(url, img);
                        repaint();
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
        });
        imageLoaderThread.start();
    }

    private void updatePostsWithImage(String url, Image img) {
        for (int i = 0; i < posts.size(); i++) {
            Post p = (Post) posts.elementAt(i);
            if (p.imageUrl != null && p.imageUrl.equals(url)) {
                p.image = img;
            }
        }
    }
    
    // --- Helper methods ---
        private void drawAppBar(Graphics g, String title) {
        int width = getWidth();
        for (int i = 0; i < APP_BAR_HEIGHT; i++) {
            int r = ((COLOR_APP_BAR_TOP >> 16) & 0xFF) * (APP_BAR_HEIGHT - i) / APP_BAR_HEIGHT + ((COLOR_APP_BAR_BOTTOM >> 16) & 0xFF) * i / APP_BAR_HEIGHT;
            int gr = ((COLOR_APP_BAR_TOP >> 8) & 0xFF) * (APP_BAR_HEIGHT - i) / APP_BAR_HEIGHT + ((COLOR_APP_BAR_BOTTOM >> 8) & 0xFF) * i / APP_BAR_HEIGHT;
            int b = (COLOR_APP_BAR_TOP & 0xFF) * (APP_BAR_HEIGHT - i) / APP_BAR_HEIGHT + (COLOR_APP_BAR_BOTTOM & 0xFF) * i / APP_BAR_HEIGHT;
            g.setColor(r, gr, b);
            g.drawLine(0, i, width, i);
        }
        g.setColor(0xFFFFFF);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
        g.drawString(title, width / 2, APP_BAR_HEIGHT / 2, Graphics.HCENTER | Graphics.VCENTER);
    }

    private Vector wrapText(String text, Font font, int maxWidth) {
        Vector lines = new Vector();
        if (text == null || text.length() == 0) return lines;
        
        String[] words = split(text, " ");
        StringBuffer currentLine = new StringBuffer();
        
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (font.stringWidth(currentLine.toString() + " " + word) <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                lines.addElement(currentLine.toString());
                currentLine = new StringBuffer(word);
            }
        }
        lines.addElement(currentLine.toString());
        return lines;
    }
    
    private static String[] split(String original, String separator) {
        Vector nodes = new Vector();
        String str = original;
        int index = str.indexOf(separator);
        while(index >= 0) {
            nodes.addElement(str.substring(0, index));
            str = str.substring(index + separator.length());
            index = str.indexOf(separator);
        }
        nodes.addElement(str);
        String[] result = new String[nodes.size()];
        nodes.copyInto(result);
        return result;
    }
}
