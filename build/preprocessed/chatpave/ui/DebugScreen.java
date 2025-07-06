/*
 * Located in: src/chatpave/ui/DebugScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import java.util.Vector;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A simple screen to display debug information, such as raw API responses.
 */
public class DebugScreen extends Canvas {

    private String title;
    private Vector lines = new Vector();
    private int topY = 0;

    public DebugScreen(String title, String content) {
        this.title = title;
        setFullScreenMode(true);
        // Wrap the content into lines
        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        this.lines = wrapText(content, font, getWidth() - 10);
    }

    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(0xFFFFFF); // White background
        g.fillRect(0, 0, width, height);
        
        // Draw Title
        g.setColor(0x000000); // Black text
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString(title, 10, 10, Graphics.LEFT | Graphics.TOP);
        g.drawLine(10, 30, width - 10, 30);

        // Draw Content
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        int y = 40 - topY; // Apply scrolling
        for (int i = 0; i < lines.size(); i++) {
            g.drawString((String) lines.elementAt(i), 5, y, Graphics.LEFT | Graphics.TOP);
            y += g.getFont().getHeight();
        }
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        if (gameAction == UP) {
            topY -= 20;
            if (topY < 0) topY = 0;
        } else if (gameAction == DOWN) {
            topY += 20;
        } else if (gameAction == FIRE || keyCode == -7) {
            // Go back to home screen on fire or back key
            DisplayManager.showHomeScreen();
        }
        repaint();
    }
    
    // Helper method to wrap long strings into multiple lines
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
