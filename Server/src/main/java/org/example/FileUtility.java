package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FileUtility {

    /**
     * Checks the equality of 2 file contents
     * @param fileName1
     * @param fileName2
     * @return
     */
    public static boolean checkEq(String fileName1, String fileName2){

        try {
            File file1 = new File(fileName1);
            File file2 = new File(fileName2);

            if(file1.length() != file2.length())
                return false;

            Scanner f1scanner = new Scanner(file1);
            Scanner f2scanner = new Scanner(file2);

            while(f1scanner.hasNextInt())
                if(f1scanner.nextInt() != f2scanner.nextInt()) {
                    return false;
                }

            f1scanner.close();
            f2scanner.close();

            return true;
        }
        catch(IOException exception){
            exception.printStackTrace();
        }
        return false;
    }

    public static void writeListToFile(List<Participant> lst, String filename) throws IOException{
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for(Participant p : lst){
                fileWriter.write(p.getId() + " " + p.getScore() + "\n");
            }
        }
    }
    public static void writeMapToFile(HashMap<Integer, Integer> map, String filename) throws IOException{
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for(Map.Entry<Integer, Integer> entry : map.entrySet()){
                fileWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
        }
    }
}
