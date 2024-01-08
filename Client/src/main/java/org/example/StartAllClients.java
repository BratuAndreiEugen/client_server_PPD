package org.example;

public class StartAllClients {
    public static void main(String[] args) {
        /// args[0] - root folder
        /// args[1] - nr of countries
        /// args[2] - delay
        String absoluteRootFolder = "C:\\Proiecte SSD\\PPD\\P1\\Client\\src\\main\\resources\\" + args[0];
        for(int i = 0; i < Integer.parseInt(args[1]); i++){
            Thread client = new Thread(new ClientThread(Integer.parseInt(args[2])*1000, absoluteRootFolder + "/c" + Integer.valueOf(i+1).toString(), Integer.valueOf(i+1).toString()));
            client.start();
        }

    }
}
