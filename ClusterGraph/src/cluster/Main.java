package cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //Application Title
        stage.setTitle("Timeline");

        int maxClusterNumber = Integer.MIN_VALUE;
        String algo, range;
        int start = 0;

        //defining the axes
        final CategoryAxis xAxis = new CategoryAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        xAxis.setLabel("Time Interval");
        yAxis.setLabel("Number of Clusters");

        //creating the chart
        final LineChart<String, String> lineChart
                = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Clusters Timeline");
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setLegendVisible(false);

        //Choice of algorithm
        algo = getAlgo();

        //Time Interval range
        range = getRange();

        ResultReader rr = new ResultReader();
        List<Map<Integer, List<Integer>>> clusterMap = rr.read(range, algo);

        for (Map<Integer, List<Integer>> clusterMap1 : clusterMap) {
            Set<Integer> keySet = clusterMap1.keySet();
            Integer max = Collections.max(keySet);
            if (maxClusterNumber < max) {
                maxClusterNumber = max;
            }
            for (Map.Entry<Integer, List<Integer>> entrySet : clusterMap1.entrySet()) {
                List<Integer> value = entrySet.getValue();
                Integer max1 = Collections.max(value);
                if (maxClusterNumber < max1) {
                    maxClusterNumber = max1;
                }
            }
        }
        List<String> categories = new ArrayList<>();

        for (int i = 1; i <= maxClusterNumber; i++) {
            categories.add("C" + i);
        }
        yAxis.setCategories(FXCollections.observableList(categories));
        if (range.split(",").length > 1) //defining a series
        {
            for (Map<Integer, List<Integer>> map : clusterMap) {
                for (Map.Entry<Integer, List<Integer>> entrySet : map.entrySet()) {
                    Integer key = entrySet.getKey();
                    List<Integer> value = entrySet.getValue();
                    for (Integer value1 : value) {
                        XYChart.Series series = new XYChart.Series();
                        series.getData().add(new XYChart.Data("T" + range.split(",")[start], "C" + (key)));
                        series.getData().add(new XYChart.Data("T" + range.split(",")[(start + 1)], "C" + (value1)));
                        lineChart.getData().addAll(series);
                    }
                }
                start++;
            }
        } else {
            System.out.println("Cluster Timeline can't be plotted due to insufficient Time Intervals");
        }

        final Scene scene = new Scene(lineChart, 800, 600);
        scene.getStylesheets().add(getClass().getResource("styles/global.css").toExternalForm());

        //To create snapshot of graph
//        WritableImage snapShot = lineChart.snapshot(new SnapshotParameters(), new WritableImage(800, 600));
//        ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File("ClusterTimeline " + new SimpleDateFormat("yyyy-MM-dd hhmm").format(new Date()) + ".png"));
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    public String getAlgo() {
        Scanner src = new Scanner(System.in);
        String algo = null;
        int algoChoice;
        do {
            System.out.println("Select the clustering Algorithm of your choice");
            System.out.println("1. MCL");
            System.out.println("2. MLRMCL");
            System.out.println("3. ChineseWhispers");
            algoChoice = src.nextInt();
            switch (algoChoice) {
                case 1:
                    algo = "MCL";
                    break;
                case 2:
                    algo = "MLRMCL";
                    break;
                case 3:
                    algo = "CW";
                    break;
                default:
                    System.out.println("Invalid choice try again");
            }
        } while (algoChoice < 1 || algoChoice > 3);
        return algo;
    }

    public String getRange() {
        Scanner src = new Scanner(System.in);
        String range;
        int count;
        do {
            System.out.println("Enter value of compared cluster's Time Intervals to create the Cluster Timeline");
            System.out.println("The Time Intervals must be separated by comma(,) with no whitespace after comma");
            System.out.println("Ex. 20 50,51 100");
//                range = src.nextLine();
            range = src.nextLine();
            count = range.split(",").length;
            if (count <= 1) {
                System.out.println("Please enter more than one Time Interval of Compare Cluster to create the Cluster Timeline");
            }
        } while (count <= 1);
        return range;
    }
}
