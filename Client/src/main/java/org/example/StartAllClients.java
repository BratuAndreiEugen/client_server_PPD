package org.example;

public class StartAllClients {
    public static void main(String[] args) {
        /// args[0] - root folder
        /// args[1] - nr of countries
        /// args[2] - delay
        for(int i = 0; i < Integer.parseInt(args[1]); i++){
            Thread client = new Thread(new ClientThread(Integer.parseInt(args[2]), args[0], Integer.valueOf(i+1).toString()));
            client.start();
        }

    }
}
