/*
 * Located in: src/chatpave/ui/HomeScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import chatpave.auth.AuthManager;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * The main dashboard screen. This version has a complete icon set.
 */
public class HomeScreen extends Canvas {

    // --- State Management ---
    private int currentFocus = 0; // 0-8 for grid icons, 9 for Menu, 10 for Logout
    private boolean isButtonPressed = false;

    // --- UI Constants & Colors ---
    private static final int COLOR_BACKGROUND = 0xEFEFEF;
    private static final int COLOR_TEXT_DARK = 0x333333;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFF;
    private static final int COLOR_FOCUS_HIGHLIGHT = 0xFFD700;
    private static final int COLOR_ICON_PRIMARY = 0x555555;
    private static final int COLOR_APP_BAR_TOP = 0x6D84B4;
    private static final int COLOR_APP_BAR_BOTTOM = 0x4A6398;
    private static final int APP_BAR_HEIGHT = 40;
    private static final int BOTTOM_BAR_HEIGHT = 40;

    private String[] iconLabels = {
        "News Feed", "Messages", "Add Friend",
        "Notifications", "Photos", "Profile",
        "Friends", "Search", "Settings"
    };

    public HomeScreen() {
        setFullScreenMode(true);
    }

    protected void paint(Graphics g) {
        try {
            int width = getWidth();
            int height = getHeight();

            g.setColor(COLOR_BACKGROUND);
            g.fillRect(0, 0, width, height);
            
            drawAppBar(g, "Home");
            drawBottomBar(g);
            drawIconGrid(g);

        } catch (Exception e) {
            g.setColor(0xFF0000);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(0xFFFFFF);
            g.drawString("Paint Error!", 10, 10, Graphics.LEFT | Graphics.TOP);
            g.drawString(e.toString(), 10, 30, Graphics.LEFT | Graphics.TOP);
            e.printStackTrace();
        }
    }

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
        
        if (currentFocus == 9) g.setColor(COLOR_FOCUS_HIGHLIGHT);
        g.drawString("Menu", 10, height - (BOTTOM_BAR_HEIGHT / 2), Graphics.LEFT | Graphics.VCENTER);
        
        g.setColor(currentFocus == 10 ? COLOR_FOCUS_HIGHLIGHT : COLOR_TEXT_LIGHT);
        g.drawString("Logout", width - 10, height - (BOTTOM_BAR_HEIGHT / 2), Graphics.RIGHT | Graphics.VCENTER);
    }

    private void drawIconGrid(Graphics g) {
        int gridTop = APP_BAR_HEIGHT + 10;
        int gridLeft = 10;
        int cellWidth = (getWidth() - 20) / 3;
        int cellHeight = (getHeight() - gridTop - BOTTOM_BAR_HEIGHT - 10) / 3;

        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            int cellX = gridLeft + col * cellWidth;
            int cellY = gridTop + row * cellHeight;

            if (currentFocus == i) {
                g.setColor(COLOR_FOCUS_HIGHLIGHT);
                g.fillRoundRect(cellX + 2, cellY + 2, cellWidth - 4, cellHeight - 4, 10, 10);
            }
            
            drawIcon(g, i, cellX + cellWidth / 2, cellY + cellHeight / 2 - 10);

            g.setColor(COLOR_TEXT_DARK);
            g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
            g.drawString(iconLabels[i], cellX + cellWidth / 2, cellY + cellHeight - 20, Graphics.HCENTER | Graphics.TOP);
        }
    }
    
    /**
     * A master function to call the correct icon drawing method.
     * This version includes drawings for all icons.
     */
    private void drawIcon(Graphics g, int iconIndex, int cx, int cy) {
        g.setColor(COLOR_ICON_PRIMARY);
        switch (iconIndex) {
            case 0: drawNewsFeedIcon(g, cx, cy); break;
            case 1: drawMessagesIcon(g, cx, cy); break;
            case 2: drawAddFriendIcon(g, cx, cy); break;
            case 3: drawNotificationsIcon(g, cx, cy); break;
            case 4: drawPhotosIcon(g, cx, cy); break;
            case 5: drawProfileIcon(g, cx, cy); break;
            case 6: drawFriendsIcon(g, cx, cy); break;
            case 7: drawSearchIcon(g, cx, cy); break;
            case 8: drawSettingsIcon(g, cx, cy); break;
            default: break;
        }
    }

    // --- Programmatic Icon Drawing ---

    private void drawNewsFeedIcon(Graphics g, int cx, int cy) {
        g.drawRect(cx - 15, cy - 12, 30, 24);
        g.drawLine(cx - 12, cy - 8, cx + 12, cy - 8);
        g.drawLine(cx - 12, cy - 2, cx + 12, cy - 2);
        g.drawLine(cx - 12, cy + 4, cx + 12, cy + 4);
    }

    private void drawMessagesIcon(Graphics g, int cx, int cy) {
        g.drawRoundRect(cx - 16, cy - 12, 32, 22, 8, 8);
        g.drawLine(cx - 10, cy + 10, cx - 2, cy + 16);
        g.drawLine(cx - 2, cy + 16, cx + 4, cy + 10);
    }

    private void drawAddFriendIcon(Graphics g, int cx, int cy) {
        g.fillArc(cx - 14, cy - 10, 20, 20, 0, 360); // Head
        g.fillRect(cx + 4, cy - 15, 4, 12); // Plus vertical
        g.fillRect(cx, cy - 11, 12, 4); // Plus horizontal
    }

    private void drawNotificationsIcon(Graphics g, int cx, int cy) {
        g.drawArc(cx - 12, cy - 12, 24, 24, 45, 270); // Bell shape
        g.fillRect(cx - 2, cy + 12, 4, 4); // Clapper
    }

    private void drawPhotosIcon(Graphics g, int cx, int cy) {
        g.drawRect(cx - 16, cy - 12, 22, 22); // Frame
        g.fillArc(cx - 10, cy + 2, 8, 8, 0, 360); // Sun
        g.drawLine(cx, cy - 8, cx + 4, cy - 2); // Mountain
        g.drawLine(cx + 4, cy - 2, cx + 8, cy - 8); // Mountain
    }

    private void drawProfileIcon(Graphics g, int cx, int cy) {
        g.fillArc(cx - 8, cy - 12, 16, 16, 0, 360); // Head
        g.fillArc(cx - 14, cy + 4, 28, 20, 0, 180); // Body
    }

    private void drawFriendsIcon(Graphics g, int cx, int cy) {
        g.fillArc(cx - 16, cy - 8, 16, 16, 0, 360); // Left head
        g.fillArc(cx, cy - 8, 16, 16, 0, 360); // Right head
        g.fillArc(cx - 12, cy + 8, 24, 16, 0, 180); // Shared body
    }

    private void drawSearchIcon(Graphics g, int cx, int cy) {
        g.drawArc(cx - 14, cy - 14, 20, 20, 0, 360); // Magnifying glass circle
        g.drawLine(cx, cy, cx + 10, cy + 10); // Handle
    }
    
    private void drawSettingsIcon(Graphics g, int cx, int cy) {
        g.fillArc(cx - 10, cy - 10, 20, 20, 0, 360); // Center gear
        g.setColor(COLOR_BACKGROUND);
        g.fillArc(cx - 5, cy - 5, 10, 10, 0, 360); // Hole in gear
    }

    // --- Input Handling ---

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        if (gameAction == FIRE) {
            isButtonPressed = true;
        } else if (gameAction == UP) {
            if (currentFocus >= 3 && currentFocus <= 8) currentFocus -= 3;
            else if (currentFocus >= 9) currentFocus = 7;
        } else if (gameAction == DOWN) {
            if (currentFocus <= 5) currentFocus += 3;
            else if (currentFocus <= 8) currentFocus = 9;
        } else if (gameAction == LEFT) {
            if (currentFocus > 0 && currentFocus != 3 && currentFocus != 6 && currentFocus != 10) currentFocus--;
            else if (currentFocus == 10) currentFocus = 9;
        } else if (gameAction == RIGHT) {
            if (currentFocus < 8 && currentFocus != 2 && currentFocus != 5) currentFocus++;
            else if (currentFocus == 9) currentFocus = 10;
        }
        repaint();
    }

    protected void keyReleased(int keyCode) {
        if (getGameAction(keyCode) == FIRE) {
            isButtonPressed = false;
            repaint();
            handleFireAction();
        }
    }

    private void handleFireAction() {
        switch (currentFocus) {
            case 0: // News Feed
                DisplayManager.showNewsFeedScreen();
                break;
            case 1: // Messages
                DisplayManager.showMessagesScreen();
                break;
            // Add cases for other icons as their screens are created
            // case 2: DisplayManager.showAddFriendScreen(); break;
            
            case 10: // Logout button
                AuthManager.logout();
                DisplayManager.showLoginScreen();
                break;
            default:
                // For icons without a screen yet
                System.out.println("Selected: " + iconLabels[currentFocus]);
                break;
        }
    }
}
