import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class GUI extends Application implements NativeKeyListener, NativeMouseListener {
    boolean hotspotSettingsWindowIsActive = false;
    boolean isWaitingForInput = false;

    Button autoclickerInputButton;
    Button hotspotSettingsButton;

    int clicksPerSecond = 0;

    Label currentActivationButtonLabel;

    private static ObservableList<String> myObservableList = FXCollections.observableArrayList();
    private static ListView<String> myListView = new ListView<>(myObservableList);
    private Button removeButton = new Button("Remove");

    Stage hotspotSettings;

    static TextArea chatArea;

    String currentActivationButton = String.valueOf(NativeMouseEvent.BUTTON5);
    String currentButton = "Current Button: ";


    public static void main(String[] args)  {
        Implementer imp = new Implementer();
        imp.start();
        Implementer.runAuto = false;
        Implementer.setIsMouse(true);
        Implementer.setAutoclickerButton(NativeMouseEvent.BUTTON5);

        launch(args);

        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
    }

    public void start() throws NativeHookException {
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
    }

    @Override
    public void start(Stage primaryStage) throws NativeHookException {
        start();
        primaryStage.setTitle("S1ths unreliable Auto clicker");

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);

        VBox topPaneBox = new VBox();
        HBox topPaneFirstRow = new HBox();
        HBox topPaneSecondRow = new HBox();

        //AC Input button

        autoclickerInputButton = new Button("Change key");
        autoclickerInputButton.setOnAction(event -> waitForKeyInput());
        currentActivationButtonLabel = new Label(currentButton + currentActivationButton);

        topPaneFirstRow.getChildren().addAll(currentActivationButtonLabel, autoclickerInputButton);
        topPaneFirstRow.setAlignment(Pos.CENTER);
        topPaneFirstRow.setSpacing(10);

        //CPS

        Label pre_CPSText = new Label("1 Click every");
        Label post_CPSText = new Label("milliseconds");
        TextField cps = new TextField();
        cps.setPromptText("Default: " + Autoclicker.getMilliseconds());

        //Must be a number and also maximum 10
        Pattern pattern = Pattern.compile("\\d{0,10}");
        TextFormatter<Integer> formatter = new TextFormatter<>(c -> {
            if (pattern.matcher(c.getControlNewText()).matches() && (c.getControlNewText().isEmpty() || Integer.parseInt(c.getControlNewText()) <= Integer.MAX_VALUE)) {
                return c;
            } else {
                return null;
            }
        });
        cps.setTextFormatter(formatter);

        // Save input as variable
        cps.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals("")) {
                clicksPerSecond = Integer.parseInt(newValue);
            } else {
                clicksPerSecond = 1;
            }
            Autoclicker.setMilliseconds(clicksPerSecond);
        });

        topPaneSecondRow.getChildren().addAll(pre_CPSText, cps, post_CPSText);
        topPaneSecondRow.setAlignment(Pos.CENTER);
        topPaneSecondRow.setSpacing(5);


        topPaneBox.getChildren().addAll(topPaneFirstRow, topPaneSecondRow);
        topPaneBox.setAlignment(Pos.CENTER);
        topPaneBox.setSpacing(7);

        StackPane topPane = new StackPane(topPaneBox);

        //End of topPane, start of bottomPane

        StackPane bottomPane = new StackPane();
        bottomPane.setAlignment(Pos.BOTTOM_RIGHT); //We can safely make this bottom right for the button as the chat area takes up the whole pane

        chatArea = new TextArea();
        chatArea.setEditable(false);

        hotspotSettingsButton = new Button("Hotspot settings");
        hotspotSettingsButton.setOnAction(event -> hotSpotSettings());
        //Adjust the button position to not mess with the scrollbar
        hotspotSettingsButton.setTranslateX(-17.5);
        hotspotSettingsButton.setTranslateY(-15.0);

        bottomPane.getChildren().addAll(chatArea, hotspotSettingsButton);

        splitPane.getItems().addAll(topPane, bottomPane);
        splitPane.setDividerPosition(0, 0.5);

        Scene scene = new Scene(splitPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public void hotSpotSettings() {

        if(!hotspotSettingsWindowIsActive) {
            hotspotSettingsWindowIsActive = true;

            // Convert the int arrays to strings and add them to the ObservableList
            for (int[] array : AutoMouseMover.getPositions()) {
                String arrayString = "Hotspot: \tX: " + array[0] + "\tY: " + array[1];
                myObservableList.add(arrayString);
            }

            // Set the ListView to display the ObservableList
            myListView.setItems(myObservableList);
            myListView.setCellFactory(TextFieldListCell.forListView());

            // Add an event listener to the remove button to handle button clicks
            removeButton.setOnAction(event -> {
                int selectedIndex = myListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex != -1) {
                    AutoMouseMover.removeSpecific(selectedIndex);
                    myObservableList.remove(selectedIndex);
                }
            });

            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            layout.getChildren().addAll(myListView, removeButton);

            Scene secondScene = new Scene(layout, 230, 100);
            hotspotSettings = new Stage();

            hotspotSettings.setTitle("Hotspot Settings");
            hotspotSettings.setScene(secondScene);
            hotspotSettings.show();
            hotspotSettings.setOnCloseRequest(event -> hotspotSettingsWindowIsActive = false);
        }
        else {
            hotspotSettings.toFront();
        }
    }

    public static void updateHotspotSettings() {
        Platform.runLater(() -> {
            myObservableList.clear();
            for (int[] array : AutoMouseMover.getPositions()) {
                String arrayString = "Hotspot: \tX: " + array[0] + "\tY: " + array[1];
                myObservableList.add(arrayString);
            }
            // Set the ListView to display the ObservableList
            myListView.setItems(myObservableList);
        });
    }

    private void waitForKeyInput() {
        autoclickerInputButton.setText("Waiting for input...");
        isWaitingForInput = true;
        autoclickerInputButton.requestFocus();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        if(isWaitingForInput && nativeKeyEvent.getKeyCode()== NativeKeyEvent.VC_ESCAPE) {
            Platform.runLater(() -> autoclickerInputButton.setText("Change key"));
            isWaitingForInput = false;
        }
        else if(isWaitingForInput && nativeKeyEvent.getKeyCode()!= NativeKeyEvent.VC_ESCAPE) {
            currentActivationButton = NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode());
            Platform.runLater(() -> autoclickerInputButton.setText("Change key"));
            Platform.runLater(() -> currentActivationButtonLabel.setText(currentButton + currentActivationButton));
            Implementer.setIsMouse(false);
            Implementer.setAutoclickerButton(nativeKeyEvent.getKeyCode());
            isWaitingForInput = false;
            Implementer.runAuto = true;
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        if(isWaitingForInput && nativeMouseEvent.getButton()!=NativeMouseEvent.BUTTON1) {
            currentActivationButton = String.valueOf(nativeMouseEvent.getButton());
            Platform.runLater(() -> autoclickerInputButton.setText("Change key"));
            Platform.runLater(() -> currentActivationButtonLabel.setText(currentButton + currentActivationButton));
            Implementer.setIsMouse(true);
            Implementer.setAutoclickerButton(nativeMouseEvent.getButton());
            isWaitingForInput = false;
            Implementer.runAuto = true;
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {

    }
}