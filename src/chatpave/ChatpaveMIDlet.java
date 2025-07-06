/*
 * Located in: src/chatpave/ChatpaveMIDlet.java
 */
package chatpave;

import javax.microedition.midlet.*;

/**
 * The main entry point for the Chatpave application.
 * This MIDlet initializes the DisplayManager which handles all screen transitions.
 */
public class ChatpaveMIDlet extends MIDlet {

    /**
     * Called when the application is started. This is the main entry point.
     */
    public void startApp() {
        // Initialize the DisplayManager.
        DisplayManager.init(this);
        
        // Let the DisplayManager decide which screen to show first.
        DisplayManager.startApp();
    }

    /**
     * Called when the application is paused.
     */
    public void pauseApp() {
        // This method is left empty intentionally for now.
    }

    /**
     * Called by the system to end the application.
     * @param unconditional If true, the MIDlet must cleanup and exit.
     */
    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }
}
