package org.example;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StartServer {
    public static void main(String[] args) {
        /// args[0] - refresh rate ( delta t )
        /// args[1] - pW
        /// args[2] - pR
        /// args[3] - max clients

        Integer maxClients = Integer.valueOf(Integer.parseInt(args[3]));
        AtomicInteger i = new AtomicInteger(maxClients);
        ThreadSafeQueue<Participant> queue = new ThreadSafeQueue<>(new LinkedList<>(), 1000, i);
        ThreadSafeLinkedList<Participant> lst = new ThreadSafeLinkedList<Participant>(new LinkedList<>());
        Set<Participant> synchronizedBlackListSet = Collections.synchronizedSet(new HashSet<>());


        Server server = new Server(55555, Integer.parseInt(args[0]), queue, lst, synchronizedBlackListSet, i, maxClients);
        try {
            server.start(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}