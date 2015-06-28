package chinesewhispers;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ChineseWhispers {

    public static void main(String[] args) throws Exception, IOException {
        Scanner userInput = new Scanner(System.in);
        String file_name, range;
        boolean exists = true;
        int count, start = 0, end;
        Directory();
        System.out.println("================ ChineseWhispers Algorithm ================");
        System.out.println("Enter the filename for data set: Ex. elec.txt");
        file_name = "slashdot.txt";
        //User Interface for Input
        do {
            System.out.println("Enter value of Time Interval separated by comma(,) ");
            System.out.println("Ex. 20 50,51 100");
            range = userInput.nextLine();
            count = range.split(",").length;
            while (count != 0) {
                String[] interval = range.split(",")[start].split("\\s+");
                CW cw = new CW();
                exists = true;
                try {
                    Utils.readDynamicData(file_name + " " + interval[0] + " " + interval[1], range.split(",")[start]);
                } catch (Exception e) {
                    System.out.println("Given data set file does not exist.Try again");
                    exists = false;
                    break;
                }
                cw.readData("data" + range.split(",")[start]);
                cw.Builder_Edge_File();
                cw.ChineseWhispers();
                cw.Display_Result();
                start++;
                count--;
            }

            String[] compare = range.split(",");
            Utils.compareCluster(compare[0], compare[1]);
            ChineseWhispers.main(args);

        } while (!exists);
    }

    public static void Directory() throws IOException {
        System.out.println("LIST OF FILES IN DIRECTORY");
        File f = new File("./src/chinesewhispers/dataset"); // current directory

        File[] files = f.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.print("directory:");
            } else {
                System.out.print("     file:");
            }
            System.out.println(file.getCanonicalPath());
        }

    }
}
