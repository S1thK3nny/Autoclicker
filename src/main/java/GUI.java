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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class GUI extends Application implements NativeKeyListener, NativeMouseListener {
    boolean hotspotSettingsWindowIsActive = false;
    boolean isWaitingForInput = false;

    Button autoclickerInputButton;
    Button hotspotSettingsButton;

    int newX = 0;
    int newY = 0;
    int position = 0;

    int[] selectedArray = {0, 0};

    Label currentActivationButtonLabel;

    private static final ObservableList<String> hotspotObservableList = FXCollections.observableArrayList();
    private static final ListView<String> hotspotListView = new ListView<>(hotspotObservableList);
    private final Button hotspotRemoveButton = new Button("Remove");
    static Properties properties;

    Stage hotspotSettings;

    static TextArea chatArea;
    static boolean editingHotspot = false; //Need this in case someone tries to remove the hotspots whilst editing one!

    String currentActivationButton;
    String currentButton = "Current Button: ";


    public static void main(String[] args)  {
        properties = PropertySaver.loadProperties();
        Implementer imp = new Implementer();
        imp.start();
        Implementer.runAuto = false;

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

        //Top pane stuff start

        SplitPane splitPaneTop = new SplitPane();
        splitPaneTop.setOrientation(Orientation.HORIZONTAL);

        VBox topPaneLeftVBox = new VBox();
        HBox topPaneLeftFirstRow = new HBox();
        HBox topPaneLeftSecondRow;
        Label titleAC = new Label("Auto Clicker");

        VBox topPaneRightVBox = new VBox();
        HBox topPaneRightFirstRow = new HBox();
        HBox topPaneRightSecondRow = new HBox();
        HBox topPaneRightThirdRow;
        HBox topPaneRightFourthRow;
        Label titleAMM = new Label("Auto Mouse Mover");

        //Top pane stuff stop



        //Top pane left stuff start

        titleAC.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleAC.setAlignment(Pos.CENTER);

        //AC Input button
        autoclickerInputButton = new Button("Change key");
        autoclickerInputButton.setOnAction(event -> waitForKeyInput());

        if(Boolean.parseBoolean(properties.getProperty("isMouse"))) {
            currentActivationButton = "MB" + properties.getProperty("acButton");
        }
        else {
            currentActivationButton = NativeKeyEvent.getKeyText(Integer.parseInt(properties.getProperty("acButton")));
        }

        currentActivationButtonLabel = new Label(currentButton + currentActivationButton);

        topPaneLeftFirstRow.getChildren().addAll(currentActivationButtonLabel, autoclickerInputButton);
        topPaneLeftFirstRow.setAlignment(Pos.CENTER);
        topPaneLeftFirstRow.setSpacing(10);

        //CPS
        topPaneLeftSecondRow = textAndNumInput("1 Click every", "milliseconds", "millis");

        topPaneLeftVBox.getChildren().addAll(titleAC, topPaneLeftFirstRow, topPaneLeftSecondRow);
        topPaneLeftVBox.setAlignment(Pos.CENTER);
        topPaneLeftVBox.setSpacing(7);

        //Top pane left stuff stop



        //Top pane right stuff start

        titleAMM.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleAMM.setAlignment(Pos.CENTER);

        topPaneRightThirdRow = textAndNumInput("Move every", "milliseconds", "moveTime");
        topPaneRightFourthRow = textAndNumInput("Delay of", "milliseconds between moving", "moveInBetweenTime");

        topPaneRightVBox.getChildren().addAll(titleAMM, topPaneRightThirdRow, topPaneRightFourthRow);
        topPaneRightVBox.setAlignment(Pos.CENTER);
        topPaneRightVBox.setSpacing(7);

        StackPane topPaneLeft = new StackPane(topPaneLeftVBox);
        StackPane topPaneRight = new StackPane(topPaneRightVBox);

        splitPaneTop.getItems().addAll(topPaneLeft, topPaneRight);

        //Top pane right stuff stop



        //Bot pane stuff start

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

        splitPane.getItems().addAll(splitPaneTop, bottomPane);
        splitPane.setDividerPosition(0, 0.35);

        //Bot pane stuff stop



        Scene scene = new Scene(splitPane, 750, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            PropertySaver.saveProperties(properties);
            Platform.exit();
            System.exit(0);
        });
    }



    public HBox textAndNumInput(String pre, String post, String property) {
        HBox hbox = new HBox();
        Label preText = new Label(pre);
        Label postText = new Label(post);
        TextField textField = new TextField();
        textField.setPromptText("Default: " + properties.getProperty(property));

        //Must be a number and also maximum 10
        Pattern pattern = Pattern.compile("\\d{0,10}");
        TextFormatter<Integer> formatter = new TextFormatter<>(c -> {
            if (pattern.matcher(c.getControlNewText()).matches() && (c.getControlNewText().isEmpty() || Integer.parseInt(c.getControlNewText()) <= Integer.MAX_VALUE)) {
                return c;
            } else {
                return null;
            }
        });
        textField.setTextFormatter(formatter);

        // Save input as variable
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals("")) {
                properties.setProperty(property, newValue);
            } else {
                properties.setProperty(property, String.valueOf(1));
            }
        });

        hbox.getChildren().addAll(preText, textField, postText);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5);
        return hbox;
    }



    public void hotSpotSettings() {
        if(!hotspotSettingsWindowIsActive) {
            hotspotSettingsWindowIsActive = true;

            // Convert the int arrays to Strings and add them to the ObservableList
            updateHotspotSettings();
            hotspotListView.setCellFactory(TextFieldListCell.forListView());

            // Add an event listener to the remove button to handle button clicks
            hotspotRemoveButton.setOnAction(event -> {
                int selectedIndex = hotspotListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex != -1) {
                    AutoMouseMover.removeSpecific(selectedIndex);
                    hotspotObservableList.remove(selectedIndex);
                }
            });

            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            layout.getChildren().addAll(hotspotListView, hotspotRemoveButton);

            Scene secondScene = new Scene(layout, 450, 250);
            hotspotSettings = new Stage();

            hotspotSettings.setTitle("Hotspot settings");
            hotspotSettings.setScene(secondScene);
            hotspotSettings.show();
            hotspotSettings.setOnCloseRequest(event -> hotspotSettingsWindowIsActive = false);

            hotspotListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    int selectedIndex = hotspotListView.getSelectionModel().getSelectedIndex();
                    if (selectedIndex != -1) {
                        selectedArray = AutoMouseMover.getArray(selectedIndex);
                        //Open new modality window
                        editSpecificHotspot(selectedIndex);
                    }
                }
            });

        }
        else {
            hotspotSettings.toFront();
        }
    }



    public static void updateHotspotSettings() {
        //Do Platform.runLater() or you will get errors due to the thread differences
        Platform.runLater(() -> {
            hotspotObservableList.clear();
            for (int[] array : AutoMouseMover.getPositions()) {
                String arrayString = "Hotspot: \tX: " + array[0] + "\tY: " + array[1];
                hotspotObservableList.add(arrayString);
            }
            // Set the ListView to display the ObservableList
            hotspotListView.setItems(hotspotObservableList);
        });
    }



    private void editSpecificHotspot(int selectedIndex) {
        editingHotspot = true;
        setNewX(selectedArray[0]);
        setNewY(selectedArray[1]);
        StackPane newLayout = new StackPane();
        Scene specificHotspotScene = new Scene(newLayout, 250, 100);
        Stage specificHotspotStage = new Stage();
        specificHotspotStage.setTitle("Edit Hotspot");
        specificHotspotStage.setScene(specificHotspotScene);

        HBox hbox = new HBox();
        VBox xList = new VBox();
        VBox yList = new VBox();

        //Takes care of editing X
        Label editX = new Label("Edit X:");
        TextField editXTextField = new TextField();
        editXTextField.setPromptText("Current: " + selectedArray[0]);
        xList.getChildren().addAll(editX, editXTextField);
        editSpecificHotspotTextStuff(editXTextField, selectedArray, 0);

        //Takes care of editing Y
        Label editY = new Label("Edit Y:");
        TextField editYTextField = new TextField();
        editYTextField.setPromptText("Current: " + selectedArray[1]);
        xList.getChildren().addAll(editY, editYTextField);
        editSpecificHotspotTextStuff(editYTextField, selectedArray, 1);

        xList.setAlignment(Pos.CENTER);
        yList.setAlignment(Pos.CENTER);

        hbox.getChildren().addAll(xList, yList);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(5.0);

        newLayout.getChildren().add(hbox);

        // Specifies the modality for new window.
        specificHotspotStage.initModality(Modality.WINDOW_MODAL);
        specificHotspotStage.initOwner(hotspotSettings);
        specificHotspotStage.show();

        specificHotspotStage.setOnCloseRequest(event -> {
            selectedArray = new int[]{newX, newY};
            AutoMouseMover.setArray(selectedIndex, selectedArray);
            updateHotspotSettings();
            editingHotspot = false;
        });
    }



    public void editSpecificHotspotTextStuff(TextField textField, int[] selectedArray, int arrayPos) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals("")) {
                position = Integer.parseInt(newValue);
                if(arrayPos==0) {
                    setNewX(position);
                }
                else {
                    setNewY(position);
                }
            } else {
                position = selectedArray[arrayPos];
            }
        });

        Pattern pattern = Pattern.compile("\\d{0,4}");
        TextFormatter<Integer> formatter = new TextFormatter<>(c -> {
            if (pattern.matcher(c.getControlNewText()).matches() && (c.getControlNewText().isEmpty() || Integer.parseInt(c.getControlNewText()) <= Integer.MAX_VALUE)) {
                return c;
            } else {
                return null;
            }
        });
        textField.setTextFormatter(formatter);
    }



    private void setNewX(int pos) {
        newX = pos;
    }

    private void setNewY(int pos) {
        newY = pos;
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
            properties.setProperty("isMouse", String.valueOf(false));
            properties.setProperty("acButton", String.valueOf(nativeKeyEvent.getKeyCode()));
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
            currentActivationButton = "MB" + nativeMouseEvent.getButton();
            Platform.runLater(() -> autoclickerInputButton.setText("Change key"));
            Platform.runLater(() -> currentActivationButtonLabel.setText(currentButton + currentActivationButton));
            properties.setProperty("isMouse", String.valueOf(true));
            properties.setProperty("acButton", String.valueOf(nativeMouseEvent.getButton()));
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