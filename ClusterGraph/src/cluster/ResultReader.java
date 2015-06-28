/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cluster;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.mentaregex.Regex.match;

public class ResultReader {

    List<Map<Integer, List<Integer>>> list;
    String clusterLog;
    List<String> logs = new ArrayList<>();
    static DecimalFormat df = new DecimalFormat("#.##");

    public ResultReader() {
        this.list = new ArrayList<>();
    }

    public static void main(String[] args) {
        System.out.println("Running ResultReader");
        ResultReader rr = new ResultReader();
        List<Map<Integer, List<Integer>>> read = rr.read("20 40,41 60", "MCL");
        System.out.println(Arrays.deepToString(read.toArray()));
    }

    public List<Map<Integer, List<Integer>>> read(String range, String algo) {
        int count, start = 0;
        double totalParentNodes = 0.0, overlappingNodes = 0.0;
        count = range.split(",").length;
        while (count != 1) {
            Map<Integer, List<Integer>> mapping = new TreeMap<>();
            String fileName = "data" + range.split(",")[start] + "_" + range.split(",")[start + 1] + algo + "Results.txt";
            System.out.println(fileName);
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
                            overlappingNodes = Double.parseDouble(matches[3].trim());
//                            System.out.println("overlapping: " + overlappingNodes);
                            childCluster = new Integer(m.group().trim());
//                            System.out.println("Child: " + childCluster);
                        } catch (IllegalStateException ex) {
//                            System.out.println("exception called");
                            continue;
                        }
                        clusterLog = parentCluster + "==>" + childCluster + "\t";
                        if (aboveThreshold(totalParentNodes, overlappingNodes)) {
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
            list.add(mapping);
            start++;
            count--;
        }
        return list;
    }

    private boolean aboveThreshold(double totalParentNodes, double overlappingNodes) {
//        System.out.println("Checking threshold");
        double threshold = Double.valueOf(df.format((overlappingNodes / totalParentNodes) * 100.0));
        clusterLog += totalParentNodes + "    " + overlappingNodes + "    " + threshold;
        logs.add(clusterLog);
        clusterLog = "";
//        System.out.println(threshold);
        return threshold >= 0;
    }
}
