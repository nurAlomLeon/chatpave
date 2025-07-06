/*
 * Located in: src/chatpave/ui/NewsFeedScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A Canvas to display a list of posts in a news feed.
 * For now, it displays placeholder content.
 */
public class NewsFeedScreen extends Canvas {

    // --- UI Constants & Colors (Consistent with other screens) ---
    private static final int COLOR_BACKGROUND = 0xEFEFEF;
    private static final int COLOR_TEXT_DARK = 0x333333;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFF;
    private static final int COLOR_APP_BAR_TOP = 0x6D84B4;
    private static final int COLOR_APP_BAR_BOTTOM = 0x4A6398;
    private static final int APP_BAR_HEIGHT = 40;
    private static final int BOTTOM_BAR_HEIGHT = 40;

    public NewsFeedScreen() {
        setFullScreenMode(true);
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        // 1. Draw Background
        g.setColor(COLOR_BACKGROUND);
        g.fillRect(0, 0, width, height);
        
        // 2. Draw Top and Bottom Bars
        drawAppBar(g, "News Feed");
        drawBottomBar(g);

        // 3. Draw Placeholder Content
        g.setColor(COLOR_TEXT_DARK);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        g.drawString("News Feed Content", width / 2, height / 2, Graphics.HCENTER | Graphics.BASELINE);
        g.drawString("Will be implemented soon.", width / 2, height / 2 + 20, Graphics.HCENTER | Graphics.BASELINE);
    }

    // --- Drawing Helper Methods (Reused for consistent UI) ---

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
    }

    private void drawBottomBar(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int barY = height - BOTTOM_BAR_HEIGHT;

        for (int i = 0; i < BOTTOM_BAR_HEIGHT; i++) {
             int r = ((COLOR_APP_BAR_BOTTOM >> 16) & 0xFF) * (BOTTOM_BAR_HEIGHT - i) / BOTTOM_BAR_HEIGHT + ((COLOR_APP_BAR_TOP >> 16) & 0xFF) * i / BOTTOM_BAR_HEIGHT;
             int gr = ((COLOR_APP_BAR_BOTTOM >> 8) & 0xFF) * (BOTTOM_BAR_HEIGHT - i) / BOTTOM_BAR_HEIGHT + ((COLOR_APP_BAR_TOP >> 8) & 0xFF) * i / BOTTOM_BAR_HEIGHT;
             int b = (COLOR_APP_BAR_BOTTOM & 0xFF) * (BOTTOM_BAR_HEIGHT - i) / BOTTOM_BAR_HEIGHT + (COLOR_APP_BAR_TOP & 0xFF) * i / BOTTOM_BAR_HEIGHT;
             g.setColor(r, gr, b);
             g.drawLine(0, barY + i, width, barY + i);
        }
        
        g.setColor(COLOR_TEXT_LIGHT);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString("Back", 10, height - (BOTTOM_BAR_HEIGHT / 2), Graphics.LEFT | Graphics.VCENTER);
    }

    // --- Input Handling ---

    protected void keyPressed(int keyCode) {
        // For now, any key press will go back to the home screen.
        DisplayManager.showHomeScreen();
    }
}
