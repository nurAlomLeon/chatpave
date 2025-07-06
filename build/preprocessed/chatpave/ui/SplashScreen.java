/*
 * Located in: src/chatpave/ui/SplashScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import chatpave.util.ImageUtil; // Import the new utility
import java.io.IOException;
import javax.microedition.lcdui.*;

/**
 * A Canvas that displays a splash screen with the application logo.
 * It now resizes the logo to ensure it fits on the screen.
 */
public class SplashScreen extends Canvas implements Runnable {
    
    private Image logo;
    private static final int SPLASH_DURATION = 3000; // 3 seconds

    public SplashScreen() {
        setFullScreenMode(true); 

        try {
            Image originalLogo = Image.createImage("/logo.png");
            
            // Define max dimensions for the logo (e.g., 80% of screen width)
            int maxWidth = getWidth() * 8 / 10;
            int maxHeight = getHeight() * 8 / 10;
            
            // Resize the logo using our new utility
            logo = ImageUtil.resizeImage(originalLogo, maxWidth, maxHeight);

        } catch (IOException e) {
            System.err.println("Failed to load logo.png: " + e.getMessage());
            e.printStackTrace();
        }
        
        Thread splashThread = new Thread(this);
        splashThread.start();
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        // A light beige background color similar to your logo
        g.setColor(0xF5F5DC);
        g.fillRect(0, 0, width, height);

        if (logo != null) {
            // Draw the (now resized) logo in the center of the screen
            g.drawImage(logo, width / 2, height / 2, Graphics.HCENTER | Graphics.VCENTER);
        } else {
            g.setColor(0x000000); 
            g.drawString("Chatpave", width / 2, height / 2, Graphics.HCENTER | Graphics.BASELINE);
        }
    }

    public void run() {
        try {
            Thread.sleep(SPLASH_DURATION);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            DisplayManager.showLoginScreen();
        }
    }
}
