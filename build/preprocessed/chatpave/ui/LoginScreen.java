/*
 * Located in: src/chatpave/ui/LoginScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import chatpave.auth.AuthManager;
import chatpave.net.ApiClient;
import javax.microedition.lcdui.*;

/**
 * The final, clean version of the LoginScreen.
 */
public class LoginScreen extends Canvas implements CommandListener, Runnable {

    // --- State Management and UI Constants ---
    private static final int FOCUS_USERNAME = 0;
    private static final int FOCUS_PASSWORD = 1;
    private static final int FOCUS_LOGIN_BUTTON = 2;
    private static final int FOCUS_REGISTER_BUTTON = 3;
    private StringBuffer username = new StringBuffer();
    private StringBuffer password = new StringBuffer();
    private int currentFocus = FOCUS_USERNAME;
    private boolean isButtonPressed = false;
    private int editingField;
    private boolean isLoading = false;
    private String statusMessage = "";
    private final Command okCommand = new Command("OK", Command.OK, 1);
    private final Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);
    private static final int COLOR_BACKGROUND = 0xEFEFEF;
    private static final int COLOR_TEXT_DARK = 0x333333;
    private static final int COLOR_TEXT_LIGHT = 0xFFFFFF;
    private static final int COLOR_PLACEHOLDER = 0xAAAAAA;
    private static final int COLOR_FIELD_BG = 0xFFFFFF;
    private static final int COLOR_FIELD_BORDER = 0xCCCCCC;
    private static final int COLOR_FOCUS_BORDER = 0x3B5998;
    private static final int COLOR_BUTTON_BG = 0x5B7DC1;
    private static final int COLOR_BUTTON_PRESSED_BG = 0x4A6398;
    private static final int COLOR_APP_BAR_TOP = 0x6D84B4;
    private static final int COLOR_APP_BAR_BOTTOM = 0x4A6398;
    private static final int APP_BAR_HEIGHT = 40;

    public LoginScreen() {
        setFullScreenMode(true);
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(COLOR_BACKGROUND);
        g.fillRect(0, 0, width, height);
        
        drawAppBar(g);

        if (isLoading) {
            g.setColor(COLOR_TEXT_DARK);
            g.drawString("Logging in...", width / 2, height / 2, Graphics.HCENTER | Graphics.BASELINE);
            return;
        }

        // Draw the form content
        int contentY = APP_BAR_HEIGHT + 30;
        int fieldWidth = width - 60;
        int fieldHeight = 35;
        int fieldX = 30;
        
        drawTextField(g, "Username", username, fieldX, contentY, fieldWidth, fieldHeight, currentFocus == FOCUS_USERNAME);
        drawTextField(g, "Password", password, fieldX, contentY + 80, fieldWidth, fieldHeight, currentFocus == FOCUS_PASSWORD);
        
        if (statusMessage.length() > 0) {
            g.setColor(0xFF0000);
            g.drawString(statusMessage, width / 2, contentY + 140, Graphics.HCENTER | Graphics.TOP);
        }
        
        int buttonWidth = width / 2 - 40;
        int buttonHeight = 40;
        int buttonY = height - buttonHeight - 20;
        
        drawButton(g, "Login", (width / 2) - buttonWidth - 10, buttonY, buttonWidth, buttonHeight, currentFocus == FOCUS_LOGIN_BUTTON);
        drawButton(g, "Register", (width / 2) + 10, buttonY, buttonWidth, buttonHeight, currentFocus == FOCUS_REGISTER_BUTTON);
    }

    // --- All other methods are required for this class to function ---

    private void drawAppBar(Graphics g) {
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
        g.drawString("Chatpave", width / 2, APP_BAR_HEIGHT / 2, Graphics.HCENTER | Graphics.VCENTER);
    }

    private void drawTextField(Graphics g, String label, StringBuffer value, int x, int y, int w, int h, boolean hasFocus) {
        g.setColor(COLOR_FIELD_BG);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(hasFocus ? COLOR_FOCUS_BORDER : COLOR_FIELD_BORDER);
        g.drawRoundRect(x, y, w - 1, h - 1, 10, 10);
        g.setColor(COLOR_TEXT_DARK);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
        g.drawString(label, x + 5, y - 25, Graphics.LEFT | Graphics.TOP);

        if (value.length() > 0) {
            String textToDraw = (label.equals("Password")) ? maskPassword(value.toString()) : value.toString();
            g.drawString(textToDraw, x + 8, y + h / 2, Graphics.LEFT | Graphics.VCENTER);
        } else {
            g.setColor(COLOR_PLACEHOLDER);
            g.drawString("Click to edit", x + 8, y + h / 2, Graphics.LEFT | Graphics.VCENTER);
        }
    }

    private void drawButton(Graphics g, String text, int x, int y, int w, int h, boolean hasFocus) {
        boolean isThisButtonPressed = hasFocus && isButtonPressed;
        g.setColor(isThisButtonPressed ? COLOR_BUTTON_PRESSED_BG : COLOR_BUTTON_BG);
        g.fillRoundRect(x, y, w, h, 15, 15);
        g.setColor(COLOR_TEXT_LIGHT);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString(text, x + w / 2, y + h / 2, Graphics.HCENTER | Graphics.VCENTER);
        if (hasFocus) {
            g.setColor(COLOR_FOCUS_BORDER);
            g.drawRoundRect(x - 2, y - 2, w + 3, h + 3, 18, 18);
        }
    }

    private String maskPassword(String s) {
        StringBuffer masked = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            masked.append('*');
        }
        return masked.toString();
    }

    protected void keyPressed(int keyCode) {
        if (isLoading) return;
        int gameAction = getGameAction(keyCode);
        if (gameAction == FIRE) {
            isButtonPressed = true;
        } else if (gameAction == UP) {
            currentFocus = (currentFocus == 0) ? 3 : currentFocus - 1;
        } else if (gameAction == DOWN) {
            currentFocus = (currentFocus + 1) % 4;
        } else if (gameAction == LEFT) {
            if (currentFocus == FOCUS_REGISTER_BUTTON) currentFocus = FOCUS_LOGIN_BUTTON;
        } else if (gameAction == RIGHT) {
            if (currentFocus == FOCUS_LOGIN_BUTTON) currentFocus = FOCUS_REGISTER_BUTTON;
        }
        repaint();
    }
    
    protected void keyReleased(int keyCode) {
        if (isLoading) return;
        if (getGameAction(keyCode) == FIRE) {
            isButtonPressed = false;
            repaint();
            handleFireAction();
        }
    }
    
    /**
     * This method now correctly handles the boolean return type from ApiClient.login().
     */
    public void run() {
        // The ApiClient.login method returns true on success, false on failure.
        boolean loginSuccess = ApiClient.login(username.toString(), password.toString());
        
        // This code runs after the network call is complete.
        isLoading = false;
        
        if (loginSuccess) {
            // If login was successful, go to the home screen.
            DisplayManager.showHomeScreen();
        } else {
            // **MODIFICATION**: Use the detailed error from ApiClient.
            // If ApiClient.apiError is null or empty, provide a default message.
            if (ApiClient.apiError != null && ApiClient.apiError.length() > 0) {
                statusMessage = "Error: " + ApiClient.apiError;
            } else {
                statusMessage = "Login Failed: Unknown error.";
            }
            repaint();
        }
    }

    private void handleFireAction() {
        if (currentFocus == FOCUS_LOGIN_BUTTON) {
            isLoading = true;
            statusMessage = "";
            repaint();
            new Thread(this).start();
        } else if (currentFocus == FOCUS_USERNAME) {
            editingField = FOCUS_USERNAME;
            showTextBox("Enter Username", username, TextField.ANY);
        } else if (currentFocus == FOCUS_PASSWORD) {
            editingField = FOCUS_PASSWORD;
            showTextBox("Enter Password", password, TextField.PASSWORD);
        } else if (currentFocus == FOCUS_REGISTER_BUTTON) {
            // This should navigate to your registration screen
            DisplayManager.showRegistrationScreen();
        }
    }

    private void showTextBox(String title, StringBuffer text, int constraints) {
        TextBox textBox = new TextBox(title, text.toString(), 256, constraints);
        textBox.addCommand(okCommand);
        textBox.addCommand(cancelCommand);
        textBox.setCommandListener(this);
        DisplayManager.show(textBox);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == okCommand) {
            String text = ((TextBox) d).getString();
            if (editingField == FOCUS_USERNAME) {
                username.setLength(0);
                username.append(text);
            } else if (editingField == FOCUS_PASSWORD) {
                password.setLength(0);
                password.append(text);
            }
        }
        DisplayManager.show(this);
    }
}
