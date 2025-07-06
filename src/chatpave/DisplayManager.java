/*
 * Located in: src/chatpave/DisplayManager.java
 */
package chatpave;

import chatpave.auth.AuthManager;
import chatpave.ui.ChatScreen;
import chatpave.ui.HomeScreen;
import chatpave.ui.LoginScreen;
import chatpave.ui.MessagesScreen;
import chatpave.ui.NewsFeedScreen; // Import the NewsFeedScreen
import chatpave.ui.RegistrationScreen;
import chatpave.ui.SplashScreen;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import chatpave.ui.DebugScreen; // Import the new DebugScreen

/**
 * A singleton utility class to manage screen transitions.
 */
public class DisplayManager {

    private static ChatpaveMIDlet midlet;
    private static Display display;

    private DisplayManager() {}

    public static void init(ChatpaveMIDlet m) {
        midlet = m;
        display = Display.getDisplay(midlet);
    }

    public static void show(Displayable d) {
        if (display != null) {
            display.setCurrent(d);
        }
    }

    public static void startApp() {
        if (AuthManager.loadSession()) {
            System.out.println("Session found, skipping login.");
            showHomeScreen();
        } else {
            System.out.println("No session found, showing splash screen.");
            show(new SplashScreen());
        }
    }

    public static void showLoginScreen() {
        show(new LoginScreen());
    }
    
    public static void showHomeScreen() {
        show(new HomeScreen());
    }

    public static void showRegistrationScreen() {
        show(new RegistrationScreen());
    }

    public static void showMessagesScreen() {
        show(new MessagesScreen());
    }

    public static void showChatScreen(int partnerId, String partnerUsername) {
        show(new ChatScreen(partnerId, partnerUsername));
    }
    
    /**
     * Shows the main news feed screen.
     * This now correctly creates and shows the NewsFeedScreen.
     */
    public static void showNewsFeedScreen() {
        show(new NewsFeedScreen());
    }

    public static void exitApp() {
        if (midlet != null) {
            midlet.destroyApp(false);
        }
    }
        public static void showDebugScreen(String title, String content) {
        show(new DebugScreen(title, content));
    }
}
