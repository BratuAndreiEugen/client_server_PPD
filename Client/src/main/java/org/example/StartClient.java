package org.example;

import org.example.objectprotocol.requests.FinalRequest;
import org.example.objectprotocol.requests.LeaderboardRequest;
import org.example.objectprotocol.requests.UpdateRequest;
import org.example.objectprotocol.responses.FinalContentResponse;
import org.example.objectprotocol.responses.LeaderboardResponse;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartClient {
    public static void main(String[] args) {

        int portNumber = 55555;
        String serverAddress = "localhost";
        int delayBetweenBatches = Integer.parseInt(args[2]) * 1000;

        try (Socket socket = new Socket(serverAddress, portNumber)) {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            File[] files = new File(args[0]).listFiles();
            int country = Integer.parseInt(args[1]);
            List<Participant> entries = new ArrayList<>();
            System.out.println("STARTED CLIENT " + args[1]);
            for (File file : files) {
                System.out.println("NEXT FILE ");
                try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))){
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        Participant participant = Participant.fromString(line, file);
                        entries.add(participant);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

            }

            for(int i = 0; i < entries.size(); i+= 20){
                int endIndex = Math.min(i + 20,entries.size());
                List<Participant> batch = new ArrayList<>(entries.subList(i, endIndex));
                output.writeObject(new UpdateRequest(entries, country));
                output.flush();
            }

            output.writeObject(new LeaderboardRequest());
            output.flush();
            Object response = input.readObject();
            if (response instanceof LeaderboardResponse leaderboardResponse) {
                System.out.println("[" + country + "]" + " Am primit leaderboardResponse: " + leaderboardResponse.getScores());
            }

            output.writeObject(new FinalRequest());
            output.flush();
            while(true) {
                try {
                    response = input.readObject();
                    break;
                }
                catch (Exception e){
                    continue;
                }
            }
            if (response instanceof FinalContentResponse finalContentResponse) {
                System.out.println("[" + country + "]" + " Am primit finalContentResponse: " + finalContentResponse.getParticipantLeaderboardContent());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}