package cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import static org.mentaregex.Regex.match;


public class GraphUtils {

    String clusterLog;
    List<String> logs = new ArrayList<>();
    static DecimalFormat df = new DecimalFormat("#.##");
    //List to store records
    ObservableList<Record> recordList = FXCollections.observableArrayList();

    public class Record {

        private final SimpleStringProperty interval;
        private final SimpleStringProperty parentToChild;
        private final SimpleStringProperty parentNodes;
        private final SimpleStringProperty overlappingNodes;
        private final SimpleStringProperty threshold;

        public Record(String interval, String parentChild, String totalNodes, String commonNodes, String threshold) {
            this.interval = new SimpleStringProperty(interval);
            this.parentToChild = new SimpleStringProperty(parentChild);
            this.parentNodes = new SimpleStringProperty(totalNodes);
            this.overlappingNodes = new SimpleStringProperty(commonNodes);
            this.threshold = new SimpleStringProperty(threshold);
        }

        public String getInterval() {
            return interval.get();
        }

        public String getParentToChild() {
            return parentToChild.get();
        }

        public String getParentNodes() {
            return parentNodes.get();
        }

        public String getOverlappingNodes() {
            return overlappingNodes.get();
        }

        public String getThreshold() {
            return threshold.get();
        }

    }

    public static void main(String[] args) {
        System.out.println("Running ResultReader");
        GraphUtils rr = new GraphUtils();
        List<Map<Integer, List<Integer>>> read = rr.read("0 100,100 200", "MCL", 0);
        System.out.println(Arrays.deepToString(read.toArray()));
    }

    public List<Map<Integer, List<Integer>>> read(String range, String algo, double limit) {
        List<Map<Integer, List<Integer>>> list = new ArrayList<>();

        int count, start = 0;
        double totalParentNodes = 0.0, commonNodes = 0.0;
        count = range.split(",").length;
        while (count != 1) {
            String t1 = range.split(",")[start];
            String t2 = range.split(",")[start + 1];
            Map<Integer, List<Integer>> mapping = new TreeMap<>();
            String fileName = "data" + t1 + "_" + t2 + algo + "Results.txt";
//            System.out.println(fileName);
            try (Scanner in = new Scanner(getClass().getClassLoader().getResourceAsStream("cluster/resources/" + fileName))) {
                Pattern p2 = Pattern.compile("(\\s\\d+)");
                boolean newCluster = true;
                ArrayList<Integer> clusters = new ArrayList<>();
                Integer parentCluster = 0, childCluster;
                while (in.hasNext()) {
                    String line = in.nextLine();
//                    System.out.println(line);
                    String[] matches = match(line, "/(\\d+)/g"); // => ["11", "22"]
                    if (newCluster) {
                        totalParentNodes = Double.parseDouble(matches[3].trim());
//                        System.out.println("total Parent Nodes: " + totalParentNodes);
                        Matcher m = p2.matcher(line);
                        newCluster = false;
                        m.find();
                        parentCluster = new Integer(m.group().trim());
//                        System.out.println("Parent: " + parentCluster);
                    } else if (!line.contains("-")) {
                        Matcher m = p2.matcher(line);
                        m.find();
                        try {
                            commonNodes = Double.parseDouble(matches[3].trim());
//                            System.out.println("overlapping: " + overlappingNodes);
                            childCluster = new Integer(m.group().trim());
//                            System.out.println("Child: " + childCluster);
                        } catch (IllegalStateException ex) {
//                            System.out.println("exception called");
                            continue;
                        }
                        clusterLog = parentCluster + "==>" + childCluster + "\t";
                        double threshold = getThreshold(totalParentNodes, commonNodes, t1, t2);
                        if (threshold >= limit) {
                            recordList.add(new Record(t1 + "-" + t2, "" + parentCluster + "==>" + childCluster, "" + totalParentNodes, "" + commonNodes, "" + threshold));
                            clusters.add(childCluster);
                        }
                    }
                    if (line.contains("-")) {
                        if (!clusters.isEmpty()) {
                            ArrayList<Integer> temp = new ArrayList<>();
                            temp.addAll(clusters);
                            mapping.put(parentCluster, temp);
                            clusters.clear();
                        }
                        newCluster = true;
                    }
                }
                in.close();
                GraphUtils.writeLogs(logs, fileName);
                logs.clear();
            }
            if (!mapping.isEmpty()) {
                list.add(mapping);
            }
            start++;
            count--;
        }
//        System.out.println("List:" + list);
        return list;
    }

    public TableView getTableView() {
        //Creating a TableView to display overlapping nodes
        TableView tableView = new TableView();

        //Creating TableColumns for TableView
        TableColumn interval = new TableColumn("Interval");
        interval.setCellValueFactory(new PropertyValueFactory<>("interval"));
        interval.setMinWidth(60);

        TableColumn parentToChild = new TableColumn("Parent==>Child");
        parentToChild.setCellValueFactory(new PropertyValueFactory<>("parentToChild"));
        parentToChild.setMinWidth(60);

        TableColumn parentNodes = new TableColumn("Total Parent Nodes");
        parentNodes.setCellValueFactory(new PropertyValueFactory<>("parentNodes"));
        parentNodes.setMinWidth(60);

        TableColumn overlappingNodes = new TableColumn("Overlapping Nodes");
        overlappingNodes.setCellValueFactory(new PropertyValueFactory<>("overlappingNodes"));
        overlappingNodes.setMinWidth(60);

        TableColumn threshold = new TableColumn("Threshold(%)");
        threshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        threshold.setMinWidth(60);

        //Add record list
//        tableView.setItems(recordList);
        tableView.setEditable(false);
        tableView.getColumns().addAll(interval, parentToChild, parentNodes, overlappingNodes, threshold);
        return tableView;
    }

    public ObservableList<Record> getTableData() {
        return recordList;
    }

    private double getThreshold(double totalParentNodes, double overlappingNodes, String t1, String t2) {
//        System.out.println("Checking threshold");
        double threshold = Double.valueOf(df.format((overlappingNodes / totalParentNodes) * 100.0));
        clusterLog += totalParentNodes + "    " + overlappingNodes + "    " + threshold;
        logs.add(clusterLog);
        clusterLog = "";
//        System.out.println(threshold);
        return threshold;
    }

    public static String getFilePath() {
        File file = new File("");
        String absolutePath = file.getAbsolutePath();
        return absolutePath + "/src";
    }

    static void writeLogs(List<String> logs, String fileName) {
        try {
            try (PrintWriter printer = new PrintWriter(new File(getFilePath() + "/cluster/log/overlappingNodes-" + fileName))) {
                printer.println("parentCluster ==> childCluster" + "    " + "totalParentNodes" + "    " + "overlappingChildNodes" + "    " + "Threshold(%)");
                logs.stream().forEach(printer::println);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GraphUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Graph getDynamicGraph(int t1, int t2, String fileName) throws IOException {
        Graph graph = new MultiGraph("Dynamic Graph", false, true);
        int source, destination, time, duration;

        // generate the graph on the client side
        String style = "node{fill-mode:plain;fill-color:#567;size:6px;}";
        graph.addAttribute("stylesheet", style);
        graph.addAttribute("ui.antialias", true);
        graph.addAttribute("layout.stabilization-limit", 0);
        System.out.println("Making graph for interval " + t1 + " to " + t2);
        Scanner src = new Scanner(getClass().getClassLoader().getResourceAsStream("cluster/resources/" + fileName));
        while (src.hasNext()) {
            String[] line = src.nextLine().split("\\s+");
            System.out.println(Arrays.asList(line));
            source = Integer.parseInt(line[0]);
            destination = Integer.parseInt(line[1]);
            time = Integer.parseInt(line[2]);
            duration = Integer.parseInt(line[3]);
            if (time >= t1 && (time + duration) <= t2) {
                graph.addEdge(source + "-" + destination, source + "", destination + "");
            }
            if (time > t2) {
                break;
            }
        }
        return graph;
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

    public LineChart getLineChart(String range, String algo, double threshold) {
        recordList.clear();
        int maxClusterNumber = Integer.MIN_VALUE;
        int start = 0, previous, next;
        //defining the axes
        final CategoryAxis xAxis = new CategoryAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        xAxis.setLabel("Time Interval");
        yAxis.setLabel("Cluster Number");

        //creating the chart
        LineChart<String, String> lineChart
                = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Clusters Timeline");
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(false);

        List<Map<Integer, List<Integer>>> clusterMap = read(range, algo, threshold);

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
        return lineChart;

    }
}
