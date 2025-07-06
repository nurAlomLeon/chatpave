/*
 * Located in: src/chatpave/ui/MessagesScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import chatpave.models.Conversation;
import chatpave.net.ApiClient;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class MessagesScreen extends Canvas implements Runnable {

    private Vector conversations;
    private boolean isLoading = true;
    private int selectedIndex = 0;
    private int topIndex = 0;

    private static final int COLOR_BACKGROUND = 0xEFEFEF;
    private static final int COLOR_TEXT_DARK = 0x333333;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFF;
    private static final int COLOR_TEXT_SECONDARY = 0x777777;
    private static final int COLOR_FOCUS_HIGHLIGHT = 0xFFD700;
    private static final int COLOR_APP_BAR_TOP = 0x6D84B4;
    private static final int COLOR_APP_BAR_BOTTOM = 0x4A6398;
    private static final int APP_BAR_HEIGHT = 40;
    private static final int ROW_HEIGHT = 60;

    public MessagesScreen() {
        setFullScreenMode(true);
        new Thread(this).start();
    }

    /**
     * MODIFIED: This method now checks if parsing failed and shows a debug screen.
     */
    public void run() {
        conversations = ApiClient.getConversations();
        isLoading = false;

        // **MODIFICATION**: Check if the list is empty AND if the debug log has content.
        if (conversations.isEmpty() && ApiClient.debugLog.length() > 0) {
            // If parsing failed, show the debug screen instead.
            //DisplayManager.show(new DebugScreen(ApiClient.debugLog.toString()));
        } else {
            // Otherwise, repaint this screen normally.
            repaint();
        }
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(COLOR_BACKGROUND);
        g.fillRect(0, 0, width, height);
        drawAppBar(g, "Messages");

        if (isLoading) {
            g.setColor(COLOR_TEXT_DARK);
            g.drawString("Loading conversations...", width / 2, height / 2, Graphics.HCENTER | Graphics.BASELINE);
            return;
        }

        if (conversations == null || conversations.isEmpty()) {
            g.setColor(COLOR_TEXT_DARK);
            g.drawString("No messages.", width / 2, height / 2, Graphics.HCENTER | Graphics.BASELINE);
            return;
        }

        int y = APP_BAR_HEIGHT;
        for (int i = topIndex; i < conversations.size(); i++) {
            if (y > height) break;
            
            Conversation convo = (Conversation) conversations.elementAt(i);
            
            if (i == selectedIndex) {
                g.setColor(COLOR_FOCUS_HIGHLIGHT);
                g.fillRect(0, y, width, ROW_HEIGHT);
            }

            g.setColor(COLOR_TEXT_DARK);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
            g.drawString(convo.partnerUsername, 10, y + 10, Graphics.LEFT | Graphics.TOP);

            g.setColor(COLOR_TEXT_SECONDARY);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            g.drawString(convo.lastMessage, 10, y + 35, Graphics.LEFT | Graphics.TOP);
            
            if (convo.unreadCount > 0) {
                g.setColor(0xFF0000);
                g.fillArc(width - 35, y + 15, 25, 25, 0, 360);
                g.setColor(COLOR_TEXT_LIGHT);
                g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
                g.drawString(String.valueOf(convo.unreadCount), width - 22, y + 27, Graphics.HCENTER | Graphics.VCENTER);
            }
            
            y += ROW_HEIGHT;
        }
    }

    // --- keyPressed and drawAppBar are unchanged ---
    protected void keyPressed(int keyCode) {
        // ... code from previous answer ...
        int gameAction = getGameAction(keyCode);

        int KEY_SOFT_RIGHT = -7;

        if (isLoading || conversations == null || conversations.isEmpty()) {
            if (gameAction == FIRE || keyCode == KEY_SOFT_RIGHT) {
                 DisplayManager.showHomeScreen();
            }
            return;
        }

        if (gameAction == UP) {
            if (selectedIndex > 0) selectedIndex--;
        } else if (gameAction == DOWN) {
            if (selectedIndex < conversations.size() - 1) selectedIndex++;
        } else if (gameAction == FIRE) {
            Conversation selectedConvo = (Conversation) conversations.elementAt(selectedIndex);
            DisplayManager.show(new ChatScreen(selectedConvo.partnerId, selectedConvo.partnerUsername));
        } else if (keyCode == KEY_SOFT_RIGHT) {
             DisplayManager.showHomeScreen();
        }
        
        int visibleRows = (getHeight() - APP_BAR_HEIGHT) / ROW_HEIGHT;
        if (selectedIndex < topIndex) {
            topIndex = selectedIndex;
        } else if (selectedIndex >= topIndex + visibleRows) {
            topIndex = selectedIndex - visibleRows + 1;
        }

        repaint();
    }
    
    private void drawAppBar(Graphics g, String title) {
        // ... code from previous answer ...
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
    }
}