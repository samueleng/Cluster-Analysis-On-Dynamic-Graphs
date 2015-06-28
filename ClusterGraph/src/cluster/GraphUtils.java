package cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

public class GraphUtils {

    public static String getFilePath() {
        File file = new File("");
        String absolutePath = file.getAbsolutePath();
        return absolutePath+"/src";
    }

    static void writeLogs(List<String> logs,String fileName) {
        try {
            try(PrintWriter printer=new PrintWriter(new File(getFilePath()+"/cluster/log/overlappingNodes-"+fileName))){
                printer.println("parentCluster ==> childCluster"+"    "+"totalParentNodes"+"    "+"overlappingChildNodes"+"    "+"Threshold(%)");
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

}
