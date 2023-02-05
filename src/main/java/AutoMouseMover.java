import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AutoMouseMover extends Thread implements NativeKeyListener, NativeMouseListener {
    Robot robot;
    Thread thread;
    Point point;
    static ArrayList<int[]> positions = new ArrayList<>();

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
        GlobalScreen.addNativeMouseListener(this);
    }

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
            sleep(Integer.parseInt(GUI.properties.getProperty("moveTime")));
            if(positions.size()>0) {
                Point point_old = MouseInfo.getPointerInfo().getLocation();
                int x_old = (int) point_old.getX();
                int y_old = (int) point_old.getY();
                for(int[] arr : positions) {
                    robot.mouseMove(arr[0], arr[1]);
                    sleep(Integer.parseInt(GUI.properties.getProperty("moveInBetweenTime")));
                }
                robot.mouseMove(x_old, y_old);
            }
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if(e.getKeyCode() == Integer.parseInt(GUI.properties.getProperty("addHotspotButton")) && !Boolean.parseBoolean(GUI.properties.getProperty("isMouseAddHotspot"))) {
            addHotspot();
        }
        else if(e.getKeyCode() == Integer.parseInt(GUI.properties.getProperty("removeHotspotButton")) && !GUI.editingHotspot && !Boolean.parseBoolean(GUI.properties.getProperty("isMouseRemoveHotspot"))) {
            removeHotspot();
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

    public static void removeSpecific(int i) {
        GUI.chatArea.appendText("Removed hotspot:" + "\tX: " + positions.get(i)[0] + "\tY: " + positions.get(i)[1] + "\n");
        positions.remove(i);
        GUI.updateHotspotSettings();
    }

    public static void setArray(int index, int[] arr) {
        int oldX = positions.get(index)[0];
        int oldY = positions.get(index)[1];
        positions.set(index, arr);
        GUI.chatArea.appendText("Edited hotspot:" + "\tOld X: " + oldX + "\tOld Y: " + oldY + "\t\tNew X: " + positions.get(index)[0] + "\tNew Y: " + positions.get(index)[1] + "\n");
    }

    public static int[] getArray(int index) {
        return positions.get(index);
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        if(nativeMouseEvent.getButton() == Integer.parseInt(GUI.properties.getProperty("addHotspotButton")) && Boolean.parseBoolean(GUI.properties.getProperty("isMouseAddHotspot"))) {
            addHotspot();
        }
        else if(nativeMouseEvent.getButton() == Integer.parseInt(GUI.properties.getProperty("removeHotspotButton")) && !GUI.editingHotspot && Boolean.parseBoolean(GUI.properties.getProperty("isMouseRemoveHotspot"))) {
            removeHotspot();
        }
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {

    }

    private void addHotspot() {
        point = MouseInfo.getPointerInfo().getLocation();
        int[] pos = {(int) point.getX(), (int) point.getY()};
        positions.add(pos);
        GUI.chatArea.appendText("Added hotspot:" + "\tX: " + pos[0] + "\tY: " + pos[1] + "\n");
        GUI.updateHotspotSettings();
    }

    private void removeHotspot() {
        positions.clear();
        GUI.chatArea.appendText("Removed all hotspots!\n");
        GUI.updateHotspotSettings();
    }
}