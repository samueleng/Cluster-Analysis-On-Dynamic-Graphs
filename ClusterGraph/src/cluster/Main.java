package cluster;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Main extends Application {

    GraphUtils graphUtils = new GraphUtils();

    LineChart<String, String> lineChart = null;

    int previousEdge = 0;

    @Override
    public void start(Stage stage) throws IOException { 
        
        //Define a VBox
        VBox vBox = new VBox();
        vBox.setSpacing(5.0);
        vBox.setPadding(new Insets(5, 5, 5, 5));

        //Application Title
        stage.setTitle("Timeline");

        //input to store type of Algorithm and series of input intervals
        String algo, range;

        //Choice of algorithm
        algo = graphUtils.getAlgo();

        //Time Interval range
        range = graphUtils.getRange();

        if (range.split(",").length > 1) {
            lineChart = graphUtils.getLineChart(range, algo, 0);
        } else {
            System.out.println("Cluster Timeline can't be plotted due to insufficient Time Intervals");
        }

        //TableView for display
        TableView tableView = graphUtils.getTableView(); 
        
        //Populate the tableView 
        tableView.setItems(graphUtils.getTableData());

        tableView.setOnMouseClicked((MouseEvent e) -> {
            highlightEdge(tableView, lineChart);
        });
        TextField textFieldThreshold = new TextField();
        textFieldThreshold.promptTextProperty().set("Enter Threshold value");

        Button btnThreshold = new Button("Set Threshold");
        btnThreshold.setOnMouseClicked((MouseEvent e) -> {
            double threshold = 0;
            try {
                threshold = Double.parseDouble(textFieldThreshold.getText());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Please enter numeric value only");
                textFieldThreshold.setText("");
                textFieldThreshold.requestFocus();
            }
            if (threshold >= 0) {
                getNewLineChart(range, algo, Double.parseDouble(textFieldThreshold.getText()));
                vBox.getChildren().remove(0);
                vBox.getChildren().add(0, lineChart);
                tableView.setItems(graphUtils.getTableData());
            }
        });

        Button btnSnapShot = new Button("Take SnapShot"); 
        
        btnSnapShot.setOnMouseClicked((MouseEvent e) -> {
            try {
                //To create snapshot of graph
                WritableImage snapShot = lineChart.snapshot(new SnapshotParameters(), new WritableImage(800, 600));
                ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File("ClusterTimeline " + new SimpleDateFormat("yyyy-MM-dd hhmm").format(new Date()) + ".png"));
                JOptionPane.showMessageDialog(null, "Snapshot created");
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }); 
        
        //ComboBox to select algorithm
        ComboBox comboBoxAlgo = new ComboBox(FXCollections.observableArrayList("MCL", "MLRMCL", "CW"));
        comboBoxAlgo.setPromptText("Select Algorithm");

        //Take range as input
        TextField textFieldRange = new TextField();
        textFieldRange.promptTextProperty().set("Ex. 20 50,51 100");
        textFieldRange.tooltipProperty().set(new Tooltip("The Time Intervals must be separated by comma(,) with no whitespace after comma"));

        //Create LineChart based on Input Range
        Button btnCreateChart = new Button("Create Chart");
        btnCreateChart.setOnMouseClicked((MouseEvent e) -> {
            String inputAlgo = (String) comboBoxAlgo.getSelectionModel().getSelectedItem();
            String inputRange = textFieldRange.getText();
            System.out.println(inputAlgo);
            System.out.println(inputRange);
            if (inputAlgo != null && inputRange != null) {
                getNewLineChart(inputRange, inputAlgo, 0);
                vBox.getChildren().remove(0);
                vBox.getChildren().add(0, lineChart);
                tableView.setItems(graphUtils.getTableData());
            } else {
                JOptionPane.showMessageDialog(null, "Please select Algorithm Category and give range");
            }
        }); 
        
        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(textFieldThreshold, btnThreshold, btnSnapShot, comboBoxAlgo, textFieldRange, btnCreateChart);
        vBox.getChildren().addAll(lineChart, tableView, hBox);
        
        //Adding VBox to the scene
        final Scene scene = new Scene(vBox, 700, 700);
        scene.getStylesheets().add(getClass().getResource("styles/global.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void getNewLineChart(String range, String algo, double threshold) {
        previousEdge = 0;
        lineChart = graphUtils.getLineChart(range, algo, threshold);
    }

    private void highlightEdge(TableView tableView, LineChart<String, String> lineChart) {
        ObservableList<XYChart.Series<String, String>> data = lineChart.getData();
        data.get(previousEdge).getNode().setStyle("-fx-stroke:black;");
        data.get(tableView.getSelectionModel().getSelectedIndex()).getNode().setStyle("-fx-stroke:red;");
        previousEdge = tableView.getSelectionModel().getSelectedIndex();
    }
}
