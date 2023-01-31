import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {
    private ArrayList<int[]> myList = new ArrayList<>();
    private ObservableList<String> myObservableList = FXCollections.observableArrayList();
    private ListView<String> myListView = new ListView<>(myObservableList);
    private Button removeButton = new Button("Remove");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Add some int arrays to the ArrayList
        myList.add(new int[]{1, 2});
        myList.add(new int[]{3, 4});
        myList.add(new int[]{5, 6});

        // Convert the int arrays to strings and add them to the ObservableList
        for (int[] array : myList) {
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
                myList.remove(selectedIndex);
                myObservableList.remove(selectedIndex);
            }
        });

        // Add the ListView and remove button to a VBox layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(myListView, removeButton);

        // Show the window
        primaryStage.setScene(new Scene(layout));
        primaryStage.show();
    }
}