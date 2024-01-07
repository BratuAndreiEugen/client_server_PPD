package org.example;

import org.example.objectprotocol.requests.Request;
import org.example.objectprotocol.requests.UpdateRequest;
import org.example.objectprotocol.responses.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Producer implements Runnable {
    private ThreadSafeQueue<Participant> queue;

    private Iterable<Participant> entries;

    public Producer(ThreadSafeQueue<Participant> queue, Iterable<Participant> entries) {
        this.queue = queue;
        this.entries = entries;
    }

    @Override
    public void run() {
        for (Participant p : entries) {
            try {
                queue.push(p);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


//    while (theres more){
//        Socket server = connect(host, port);
//        server.send();
//        wait(deltaX);
//        server.close();
//    }

}
