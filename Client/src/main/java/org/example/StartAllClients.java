package org.example;

public class StartAllClients {
    public static void main(String[] args) {
        /// args[0] - root folder
        /// args[1] - nr of countries
        /// args[2] - delay
        String absoluteRootFolder = "C:\\Users\\Emi\\Downloads\\client_server_PPD-second_iteration\\Client\\src\\main\\resources\\" + args[0];
        Integer countries = Integer.parseInt(args[1]);
        Integer delay = Integer.parseInt(args[2]);
        for(int i = 0; i < countries; i++) {
            Thread client = new Thread(
                    new ClientThread(
                            delay*1000,
                            absoluteRootFolder + "\\c" + (i + 1),
                            String.valueOf(i + 1)
                    )
            );
            client.start();
        }

    }
}
