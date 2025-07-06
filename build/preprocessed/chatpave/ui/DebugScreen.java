/*
 * Located in: src/chatpave/ui/DebugScreen.java
 */
package chatpave.ui;

import chatpave.DisplayManager;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

/**
 * A simple screen to display debug messages on a physical device.
 */
public class DebugScreen extends Form implements CommandListener {
    private final Command backCommand = new Command("Back", Command.BACK, 1);

    public DebugScreen(String debugText) {
        super("Debug Output");
        append(new StringItem(null, debugText));
        addCommand(backCommand);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {
            // Go back to the main messages screen, which will show "No messages"
            DisplayManager.showMessagesScreen();
        }
    }
}