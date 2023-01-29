import org.jnativehook.GlobalScreen;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Autoclicker extends Thread {
    Robot robot;
    Thread thread;
    static int milliseconds = 5;

    public Autoclicker() {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    //Make the Thread inside here so thread won't be null when started again
    public void startThread() {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopThread() {
        thread = null;
    }

    @Override
    public void run() {
        while(thread != null) {
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getMilliseconds() {
        return milliseconds;
    }

    public static void setMilliseconds(int ms) {
        milliseconds = ms;
    }
}
