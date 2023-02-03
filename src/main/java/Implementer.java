import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Implementer implements NativeMouseInputListener, NativeKeyListener {
    static boolean runAuto = false;
    static Autoclicker theClicker;
    static AutoMouseMover theMover;

    public Implementer() {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {

    }

    public void nativeMousePressed(NativeMouseEvent e) {
        if(e.getButton() == Integer.parseInt(GUI.properties.getProperty("acButton")) && runAuto && Boolean.parseBoolean(GUI.properties.getProperty("isMouse"))) {
            stopThreads();
        }

        else if (e.getButton() == Integer.parseInt(GUI.properties.getProperty("acButton")) && Boolean.parseBoolean(GUI.properties.getProperty("isMouse")) && !runAuto) {
            startThreads();
        }
    }

    public void nativeMouseReleased(NativeMouseEvent e) {

    }

    public void start() {
        theClicker = new Autoclicker();
        theMover = new AutoMouseMover();
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        GlobalScreen.addNativeMouseListener(new Implementer());
        GlobalScreen.addNativeKeyListener(new Implementer());
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        System.out.println("Hi");
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {
        System.out.println("Hi");
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        if(nativeKeyEvent.getKeyCode() == Integer.parseInt(GUI.properties.getProperty("acButton")) && runAuto && !Boolean.parseBoolean(GUI.properties.getProperty("isMouse"))) {
            stopThreads();
        }

        else if (nativeKeyEvent.getKeyCode() == Integer.parseInt(GUI.properties.getProperty("acButton")) && !Boolean.parseBoolean(GUI.properties.getProperty("isMouse")) && !runAuto) {
            startThreads();
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    public void stopThreads() {
        runAuto = false;
        theClicker.stopThread();
        theMover.stopThread();
    }

    public void startThreads() {
        runAuto = true;
        theClicker.startThread();
        theMover.startThread();
    }
}