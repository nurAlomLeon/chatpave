/*
 * Located in: src/chatpave/DisplayManager.java
 */
package chatpave;

import chatpave.auth.AuthManager;
import chatpave.ui.ChatScreen;
import chatpave.ui.HomeScreen;
import chatpave.ui.LoginScreen;
import chatpave.ui.MessagesScreen;
import chatpave.ui.NewsFeedScreen; // Assuming you will create this
import chatpave.ui.RegistrationScreen;
import chatpave.ui.SplashScreen;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

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

    /**
     * The app's entry point. It now correctly calls loadSession().
     */
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

    /**
     * Shows the individual chat screen with a specific user.
     * This version correctly accepts the partner's details.
     * @param partnerId The ID of the user to chat with.
     * @param partnerUsername The username of the person to chat with.
     */
    public static void showChatScreen(int partnerId, String partnerUsername) {
        show(new ChatScreen(partnerId, partnerUsername));
    }
    
    public static void showNewsFeedScreen() {
        // This is a placeholder for when you create the NewsFeedScreen
        // show(new NewsFeedScreen());
        System.out.println("Navigating to News Feed (Screen not yet created).");
    }

    public static void exitApp() {
        if (midlet != null) {
            midlet.destroyApp(false);
        }
    }
}
