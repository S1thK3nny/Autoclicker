import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AutoMouseMover extends Thread implements NativeKeyListener {
    Robot robot;
    Thread thread;
    Point point;
    static ArrayList<int[]> positions = new ArrayList<>();
    static int hotspotButton;
    static int hotspotRemoveButton;

    public AutoMouseMover() {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        GlobalScreen.addNativeKeyListener(this);
        hotspotButton = NativeKeyEvent.VC_CONTROL_L;
        hotspotRemoveButton = NativeKeyEvent.VC_CONTROL_R;
    }

    public void startThread() {
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();

    }

    public void stopThread() {
        thread = null;
        positions.removeAll(positions);
    }

    @Override
    public void run() {
        while(thread != null) {
            sleep(5000);
            if(positions.size()>0) {
                Point point_old = MouseInfo.getPointerInfo().getLocation();
                int x_old = (int) point_old.getX();
                int y_old = (int) point_old.getY();
                for(int[] arr : positions) {
                    robot.mouseMove(arr[0], arr[1]);
                    sleep(25);
                }
                sleep(50);
                robot.mouseMove(x_old, y_old);
            }
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if(e.getKeyCode() == hotspotButton) {
            point = MouseInfo.getPointerInfo().getLocation();
            int[] pos = {(int) point.getX(), (int) point.getY()};
            positions.add(pos);
            GUI.chatArea.appendText("Added hotspot:" + "\tX: " + pos[0] + "\tY: " + pos[1] + "\n");
        }
        else if(e.getKeyCode() == hotspotRemoveButton) {
            positions.removeAll(positions);
            GUI.chatArea.appendText("Removed all hotspots!\n");
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    public void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<int[]> getPositions() {
        return positions;
    }

    public static void setAutoMouseMoverAddHotSpotButton(NativeKeyEvent e) {
        hotspotButton = e.getKeyCode();
    }

    public static void setAutoMouseMoverRemoveHotSpotButton(NativeKeyEvent e) {
        hotspotRemoveButton = e.getKeyCode();
    }
}