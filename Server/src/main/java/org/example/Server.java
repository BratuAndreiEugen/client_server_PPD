package org.example;

import org.example.objectprotocol.requests.FinalRequest;
import org.example.objectprotocol.requests.LeaderboardRequest;
import org.example.objectprotocol.requests.Request;
import org.example.objectprotocol.requests.UpdateRequest;
import org.example.objectprotocol.responses.FinalContentResponse;
import org.example.objectprotocol.responses.LeaderboardResponse;
import org.example.objectprotocol.responses.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.signum;

public class Server {
    private int port;

    private HashMap<Integer, Integer> countryScoreList = new HashMap<>();
    private ServerSocket server = null;
    private List<Socket> connectedClientsSockets = new ArrayList<>();
    private List<ObjectInputStream> connectedClientsInputs = new ArrayList<>();
    private List<ObjectOutputStream> connectedClientsOutputs = new ArrayList<>();
    private AtomicInteger counter;

    private AtomicInteger nrClients = new AtomicInteger(0);
    private AtomicBoolean finishedList = new AtomicBoolean(false);

    ThreadSafeQueue<Participant> queue;
    ThreadSafeLinkedList<Participant> lst;
    Set<Participant> synchronizedBlackListSet;
    private ExecutorService exec;

    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;

    private Integer maxClients;

    public Server(int port, int refresh_rate, ThreadSafeQueue<Participant> queue, ThreadSafeLinkedList<Participant> lst, Set<Participant> synchronizedBlackListSet, AtomicInteger counter, Integer maxClients) {
        this.port = port;
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new CountryRanking(), 0, refresh_rate, TimeUnit.MILLISECONDS);
        this.queue = queue;
        this.lst = lst;
        this.synchronizedBlackListSet = synchronizedBlackListSet;
        this.counter = counter;
        this.maxClients = maxClients;
    }

    public void start(Integer pW, Integer pR) throws Exception{
        try {
            CyclicBarrier barrier = new CyclicBarrier(pW);
            Consumer[] consumers = new Consumer[pW];
            for(int i = 0; i < pW; i++) {
                consumers[i] = new Consumer(queue, lst, synchronizedBlackListSet, counter, barrier, finishedList);
                consumers[i].start();
            }
            exec = Executors.newFixedThreadPool(pR);
            server = new ServerSocket(port);
            while (nrClients.get() != maxClients) {
                System.out.println("Waiting for clients ...");
                Socket client = server.accept();
                connectedClientsSockets.add(client);
                ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                output.flush();
                connectedClientsOutputs.add(output);
                ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                connectedClientsInputs.add(input);

                nrClients.incrementAndGet();
                System.out.println("Client connected ...");
                Thread thread = new Thread(() ->
                    handleRequests(output, input)
                );
                thread.start();
            }
            while(!finishedList.get()){
                Thread.sleep(1000);
            }
            System.out.println("All Consumers finished");
            scheduledFuture.cancel(true);
            scheduledExecutorService.shutdown();

            /// TODO: Send final files
            synchronized (lst){
                lst.sort();
            }

            FileUtility.writeListToFile(lst.getJavaLinkedList(),"C:\\Users\\Emi\\Downloads\\client_server_PPD-second_iteration\\Client\\src\\main\\resources\\outputs\\parallelParticipantLeaderboard.txt");
            boolean participantLeaderboardsEqual = FileUtility.checkEq(
                    "C:\\Users\\Emi\\Downloads\\client_server_PPD-second_iteration\\Client\\src\\main\\resources\\outputs\\parallelParticipantLeaderboard.txt",
                    "C:\\Users\\Emi\\Downloads\\client_server_PPD-second_iteration\\Client\\src\\main\\resources\\outputs\\sequentialParticipantLeaderboard.txt"
            );

            FileUtility.writeMapToFile(countryScoreList, "C:\\Users\\Emi\\Downloads\\client_server_PPD-second_iteration\\Client\\src\\main\\resources\\outputs\\parallelCountryLeaderboard.txt");
            boolean countryLeaderboardsEqual = FileUtility.checkEq(
                    "C:\\Users\\Emi\\Downloads\\client_server_PPD-second_iteration\\Client\\src\\main\\resources\\outputs\\parallelCountryLeaderboard.txt",
                    "C:\\Users\\Emi\\Downloads\\client_server_PPD-second_iteration\\Client\\src\\main\\resources\\outputs\\sequentialCountryLeaderboard.txt"
            );

//            if (participantLeaderboardsEqual && countryLeaderboardsEqual){
//                System.out.println("CORRECT RESULT");
//            }
//            else{
//                System.out.println("INCORRECT RESULT");
//            }

            for (int i = 0; i < connectedClientsInputs.size(); i++){
                ObjectOutputStream output = connectedClientsOutputs.get(i);
                ObjectInputStream input = connectedClientsInputs.get(i);
                Socket client = connectedClientsSockets.get(i);
                synchronized (output){
                    output.writeObject(new FinalContentResponse(countryScoreList, lst.getJavaLinkedList()));
                    output.flush();

                    input.close();
                    output.close();
                    client.close();
                }
            }
            exec.shutdown();
            for(int i = 0; i < pW; i++){
                consumers[i].join();
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        finally {
            stop();
            System.out.println("STOPPED SERVER");
        }
    }

    private void handleRequests(ObjectOutputStream output, ObjectInputStream input) {
        try {
            while (true) {
                Object request = input.readObject();
                Object response = handleRequest((Request) request);
                if (response != null) {
                    if (response instanceof FinalContentResponse) {
                        break;
                    }
                    synchronized (output){
                        output.writeObject(response);
                        output.flush();
                    }
                }
            }
            System.out.println("Done processing");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response handleRequest(Request request) {
        if (request instanceof UpdateRequest updateRequest) {
            System.out.println("UPDATE REQUEST");
            updateRequest
                    .getEntries()
                    .forEach(x -> x.setCountry(updateRequest.getCountryId()));
            exec.execute(new Producer(queue, updateRequest.getEntries()));
            return null;
        }

        if (request instanceof LeaderboardRequest) {
            // TODO:
            System.out.println("LEADERBOARD REQUEST");
            synchronized (countryScoreList){
                System.out.println("RETURNING COUNTRY SCORE");
                return new LeaderboardResponse(countryScoreList);
            }
        }

        if (request instanceof FinalRequest) {
            // TODO:
            System.out.println("FINAL REQUEST");
            counter.decrementAndGet();
        }
        return null;
    }

    public void stop(){
        try {
            server.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public class CountryRanking implements Runnable{
        @Override
        public void run(){
            synchronized (countryScoreList) {
                countryScoreList.clear();
                synchronized (lst) {
                    for (Participant p : lst.getJavaLinkedList()) {
                        Integer country = p.getCountry();
                        Integer prev_score = countryScoreList.get(country);
                        if (prev_score != null) {
                            countryScoreList.put(country, prev_score + p.getScore());
                        } else {
                            countryScoreList.put(country, p.getScore());
                        }

                    }
                }
                List<Map.Entry<Integer, Integer>> sorted_entries = new ArrayList<>(countryScoreList.entrySet());
                sorted_entries.sort((e1, e2) -> {
                    Integer k1 = e1.getKey();
                    Integer v1 = e1.getValue();

                    Integer k2 = e2.getKey();
                    Integer v2 = e2.getValue();

                    int res = signum(v2.compareTo(v1));
                    if (res == 0)
                        return signum(k1.compareTo(k2));

                    return res;
                });
                HashMap<Integer, Integer> temp = new LinkedHashMap<>();
                for (Map.Entry<Integer, Integer> aa : sorted_entries) {
                    temp.put(aa.getKey(), aa.getValue());
                }
                countryScoreList = temp;
                //System.out.println(countryScoreList);
            }
        }
    }

}
