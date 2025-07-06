/*
 * Located in: src/chatpave/ui/ChatScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import chatpave.models.ChatMessage;
import chatpave.net.ApiClient;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.*;

public class ChatScreen extends Canvas implements Runnable, CommandListener {

    private int partnerId;
    private String partnerUsername;
    private Vector messages = new Vector();
    private boolean isLoading = true;
    private int scrollY = 0;
    private int totalContentHeight = 0;

    private int currentPage = 1;
    private boolean hasMoreMessages = true;
    private boolean isLoadingMore = false;
    private int selectedIndex = -1;
    
    private boolean isPartnerOnline = false;

    private final Command sendCommand = new Command("Send", Command.OK, 1);
    private final Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);
    private TextBox messageBox;

    // --- UI Constants ---
    private static final int COLOR_BG = 0xECE5DD;
    private static final int COLOR_MY_BUBBLE = 0xDCF8C6;
    private static final int COLOR_THEIR_BUBBLE = 0xFFFFFF;
    private static final int COLOR_TEXT_DARK = 0x000000;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFF;
    private static final int COLOR_ONLINE = 0x40C040;
    
    // **NEW**: Added a color constant for the offline indicator.
    private static final int COLOR_OFFLINE = 0xFF0000; // Red
    
    private static final int COLOR_APP_BAR_TOP = 0x075E54;
    private static final int COLOR_APP_BAR_BOTTOM = 0x128C7E;
    private static final int COLOR_TEXT_SECONDARY = 0x888888;
    private static final int COLOR_FOCUS_HIGHLIGHT = 0xFFD700;
    
    private static final int APP_BAR_HEIGHT = 40;
    private static final int BUBBLE_PADDING = 10;
    private static final int BUBBLE_MARGIN = 5;
    private static final int INPUT_AREA_HEIGHT = 50;
    private static final int LOAD_MORE_HEIGHT = 40;

    public ChatScreen(int partnerId, String partnerUsername) {
        this.partnerId = partnerId;
        this.partnerUsername = partnerUsername;
        setFullScreenMode(true);
        new Thread(this).start();
    }

    public void run() {
        isLoading = true;
        repaint();
        
        Hashtable response = ApiClient.getChatHistory(partnerId, 1);
        if (response != null && response.get("messages") != null && response.get("is_online") != null) {
            Vector initialMessages = (Vector) response.get("messages");
            isPartnerOnline = ((Boolean) response.get("is_online")).booleanValue();
            
            if (initialMessages.size() < 20) {
                hasMoreMessages = false;
            }
            messages = initialMessages;
        }

        isLoading = false;
        calculateContentHeight();
        repaint();
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, width, height);
        drawAppBar(g, partnerUsername);
        
        g.setClip(0, APP_BAR_HEIGHT, width, height - APP_BAR_HEIGHT - INPUT_AREA_HEIGHT);

        if (isLoading) {
            g.setColor(COLOR_TEXT_DARK);
            g.drawString("Loading chat...", width / 2, height / 2, Graphics.HCENTER | Graphics.BASELINE);
        } else {
            int y = height - INPUT_AREA_HEIGHT - BUBBLE_MARGIN + scrollY;
            if (!messages.isEmpty()) {
                for (int i = messages.size() - 1; i >= 0; i--) {
                    ChatMessage msg = (ChatMessage) messages.elementAt(i);
                    int bubbleHeight = getBubbleHeight(msg, g.getFont());
                    drawMessageBubble(g, msg, y, bubbleHeight);
                    y -= (bubbleHeight + BUBBLE_MARGIN);
                }
            } else {
                 g.setColor(COLOR_TEXT_DARK);
                 g.drawString("No messages yet.", width / 2, height / 2, Graphics.HCENTER | Graphics.VCENTER);
            }
            if (hasMoreMessages) {
                drawLoadMore(g, y);
            }
        }
        g.setClip(0, 0, width, height);
        drawInputArea(g);
    }
    
    public void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        int KEY_SOFT_LEFT = -6;

        if (gameAction == UP) {
            if (hasMoreMessages && scrollY >= totalContentHeight - LOAD_MORE_HEIGHT) {
                selectedIndex = 0;
            }
            scrollY += 40;
            int maxScroll = totalContentHeight;
            if (scrollY > maxScroll) scrollY = maxScroll;
            repaint();
        } else if (gameAction == DOWN) {
            selectedIndex = -1;
            scrollY -= 40;
            if (scrollY < 0) scrollY = 0;
            repaint();
        } else if (gameAction == FIRE) {
            if (selectedIndex == 0 && hasMoreMessages && !isLoadingMore) {
                loadMoreMessages();
            } else {
                showSendMessageBox();
            }
        } else if (getGameAction(keyCode) == LEFT || keyCode == KEY_SOFT_LEFT) {
             DisplayManager.showMessagesScreen();
        }
    }
    
    private void loadMoreMessages() {
        new Thread(new Runnable() {
            public void run() {
                isLoadingMore = true;
                repaint();
                currentPage++;
                Hashtable response = ApiClient.getChatHistory(partnerId, currentPage);
                if (response != null && response.get("messages") != null) {
                    Vector olderMessages = (Vector) response.get("messages");
                    if (olderMessages.isEmpty() || olderMessages.size() < 20) {
                        hasMoreMessages = false;
                    }
                    for(int i = olderMessages.size() - 1; i >= 0; i--) {
                        messages.insertElementAt(olderMessages.elementAt(i), 0);
                    }
                    calculateContentHeight();
                } else {
                    hasMoreMessages = false;
                }
                isLoadingMore = false;
                repaint();
            }
        }).start();
    }
    
    private void showSendMessageBox() {
        messageBox = new TextBox("Send Message", "", 256, TextField.ANY);
        messageBox.addCommand(sendCommand);
        messageBox.addCommand(cancelCommand);
        messageBox.setCommandListener(this);
        DisplayManager.show(messageBox);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == sendCommand) {
            final String messageText = messageBox.getString();
            if (messageText != null && messageText.length() > 0) {
                new Thread(new Runnable() {
                    public void run() {
                        messages.addElement(new ChatMessage(messageText, true, ""));
                        calculateContentHeight();
                        scrollY = 0;
                        repaint();
                        boolean success = ApiClient.sendMessage(partnerId, messageText);
                        if (!success) {
                           // Could add error handling here in the future
                        }
                    }
                }).start();
            }
        }
        DisplayManager.show(this);
    }
    
    /**
     * MODIFIED: This method now draws a red dot for offline users.
     */
    private void drawAppBar(Graphics g, String title) {
        int width = getWidth();
        for (int i = 0; i < APP_BAR_HEIGHT; i++) {
            int r = ((COLOR_APP_BAR_TOP >> 16) & 0xFF) * (APP_BAR_HEIGHT - i) / APP_BAR_HEIGHT + ((COLOR_APP_BAR_BOTTOM >> 16) & 0xFF) * i / APP_BAR_HEIGHT;
            int gr = ((COLOR_APP_BAR_TOP >> 8) & 0xFF) * (APP_BAR_HEIGHT - i) / APP_BAR_HEIGHT + ((COLOR_APP_BAR_BOTTOM >> 8) & 0xFF) * i / APP_BAR_HEIGHT;
            int b = (COLOR_APP_BAR_TOP & 0xFF) * (APP_BAR_HEIGHT - i) / APP_BAR_HEIGHT + (COLOR_APP_BAR_BOTTOM & 0xFF) * i / APP_BAR_HEIGHT;
            g.setColor(r, gr, b);
            g.drawLine(0, i, width, i);
        }
        g.setColor(COLOR_TEXT_LIGHT);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE));
        g.drawString(title, width / 2, APP_BAR_HEIGHT / 2, Graphics.HCENTER | Graphics.VCENTER);
        
        // --- MODIFIED: Draw green for online, red for offline ---
        if (isPartnerOnline) {
            g.setColor(COLOR_ONLINE);
            g.fillArc(width - 25, APP_BAR_HEIGHT / 2 - 5, 10, 10, 0, 360);
        } else {
            g.setColor(COLOR_OFFLINE);
            g.fillArc(width - 25, APP_BAR_HEIGHT / 2 - 5, 10, 10, 0, 360);
        }
    }
    
    // --- All other helper methods are unchanged ---
    private void drawLoadMore(Graphics g, int y) {
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        if (isLoadingMore) {
             g.drawString("Loading...", getWidth() / 2, y - LOAD_MORE_HEIGHT / 2, Graphics.HCENTER | Graphics.VCENTER);
        } else {
             if (selectedIndex == 0) {
                 g.setColor(COLOR_FOCUS_HIGHLIGHT);
                 g.fillRect(5, y - LOAD_MORE_HEIGHT, getWidth() - 10, LOAD_MORE_HEIGHT);
             }
             g.setColor(COLOR_TEXT_DARK);
             g.drawString("Load More...", getWidth() / 2, y - LOAD_MORE_HEIGHT / 2, Graphics.HCENTER | Graphics.VCENTER);
        }
    }
    private void drawInputArea(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int y = height - INPUT_AREA_HEIGHT;
        g.setColor(0xF0F0F0);
        g.fillRect(0, y, width, INPUT_AREA_HEIGHT);
        g.setColor(0xCCCCCC);
        g.drawLine(0, y, width, y);
        g.setColor(COLOR_TEXT_SECONDARY);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString("Press OK to type...", width / 2, height - INPUT_AREA_HEIGHT / 2, Graphics.HCENTER | Graphics.VCENTER);
    }
    private void drawMessageBubble(Graphics g, ChatMessage msg, int y, int bubbleHeight) {
        int bubbleWidth = getWidth() * 7 / 10;
        int x = msg.isSentByMe ? getWidth() - bubbleWidth - BUBBLE_MARGIN : BUBBLE_MARGIN;
        g.setColor(msg.isSentByMe ? COLOR_MY_BUBBLE : COLOR_THEIR_BUBBLE);
        g.fillRoundRect(x, y - bubbleHeight, bubbleWidth, bubbleHeight, 15, 15);
        g.setColor(COLOR_TEXT_DARK);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        Vector wrappedText = wrapText(msg.content, g.getFont(), bubbleWidth - (BUBBLE_PADDING * 2));
        for (int i = 0; i < wrappedText.size(); i++) {
            String line = (String) wrappedText.elementAt(i);
            g.drawString(line, x + BUBBLE_PADDING, y - bubbleHeight + BUBBLE_PADDING + (i * g.getFont().getHeight()), Graphics.LEFT | Graphics.TOP);
        }
    }
    private int getBubbleHeight(ChatMessage msg, Font font) {
        int bubbleWidth = getWidth() * 7 / 10;
        Vector wrappedText = wrapText(msg.content, font, bubbleWidth - (BUBBLE_PADDING * 2));
        return (wrappedText.size() * font.getHeight()) + (BUBBLE_PADDING * 2);
    }
    private void calculateContentHeight() {
        if (messages == null || messages.isEmpty()) { totalContentHeight = 0; return; }
        int height = 0;
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = (ChatMessage) messages.elementAt(i);
            height += getBubbleHeight(msg, font);
            height += BUBBLE_MARGIN;
        }
        if (hasMoreMessages) {
            height += LOAD_MORE_HEIGHT;
        }
        totalContentHeight = height;
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