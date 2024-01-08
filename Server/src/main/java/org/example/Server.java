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
import java.util.stream.Collectors;

public class Server {
    private int port;

    private HashMap<Integer, Integer> countryScoreList = new HashMap<>();
    private ServerSocket server = null;

    private List<Socket> connectedClients = new ArrayList<>();
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
            while (true) {
                if(nrClients.get() == maxClients){
                    break;
                }
                System.out.println("Waiting for clients ...");
                Socket client=server.accept();
                connectedClients.add(client);
                nrClients.incrementAndGet();
                System.out.println("Client connected ...");
                Thread thread = new Thread(() -> {
                    try{
                        handleRequests(client);

                    }catch (Exception e){
                        throw new RuntimeException(e);
                    }
                });
                thread.start();
            }

            while(!finishedList.get()){
                Thread.sleep(1000);
            }

            scheduledFuture.cancel(true);
            scheduledExecutorService.shutdown();



            /// TODO: Send final files
            synchronized (lst){
                lst.getJavaLinkedList().sort(null);
            }

            FileUtility.writeToFile(lst.getJavaLinkedList(),"C:\\Proiecte SSD\\PPD\\P1\\Client\\src\\main\\resources\\outputs\\parallelResult.txt");
            if(FileUtility.checkEq("C:\\Proiecte SSD\\PPD\\P1\\Client\\src\\main\\resources\\outputs\\parallelResult.txt", "C:\\Proiecte SSD\\PPD\\P1\\Client\\src\\main\\resources\\outputs\\sequentialResult.txt")){
                System.out.println("CORRECT RESULT");
            }else{
                System.out.println("INCORRECT RESULT");
            }


            for(Socket cl : connectedClients){
                ObjectOutputStream output = new ObjectOutputStream(cl.getOutputStream());
                synchronized (output){
                    output.writeObject(new FinalContentResponse(countryScoreList, lst.getJavaLinkedList()));
                    output.flush();
                    output.close();
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

    private void handleRequests(Socket client) throws IOException {
        ObjectInputStream input = new ObjectInputStream(client.getInputStream());
        ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
        try {
            try {
                while (!client.isClosed() && counter.get() > 0) {
                    Object request = input.readObject();
                    Object response = handleRequest((Request) request);
                    if (response != null) {
                        synchronized (output){
                            output.writeObject(response);
                            output.flush();
                        }
                    }
                }

            } finally {
                input.close();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println(client);
            if (!(e instanceof EOFException) && !e.getMessage().equals("Socket closed")) {
                throw new RuntimeException(e);
            }
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
//                    for (Participant p : lst.getJavaLinkedList()) {
//                        System.out.println(p);
//                    }
                    int cn = 0;
                    for (Participant p : lst.getJavaLinkedList()) {
                        Integer country = p.getCountry();
                        Integer prev_score = countryScoreList.get(country);
//                        System.out.println("[prev " + country + " ]  " + prev_score);
                        cn += 1;
                        if (prev_score != null) {
                            countryScoreList.put(country, prev_score + p.getScore());
                        } else {
                            countryScoreList.put(country, p.getScore());
                        }

                    }
//                    System.out.println(cn);
                }
                countryScoreList = countryScoreList.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, HashMap::new));
            }
        }
    }

}
