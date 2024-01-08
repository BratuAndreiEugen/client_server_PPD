package org.example;

import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer extends Thread {
    private ThreadSafeQueue<Participant> queue;
    private AtomicInteger counter;

    private AtomicBoolean finishedList;
    private ThreadSafeListManager listManager;

    private CyclicBarrier barrier;

    public Consumer(ThreadSafeQueue<Participant> queue, ThreadSafeLinkedList<Participant> lst, Set<Participant> synchronizedBlackListSet, AtomicInteger counter, CyclicBarrier barrier, AtomicBoolean finishedList) {
        this.queue = queue;
        listManager = new ThreadSafeListManager(
                lst,
                synchronizedBlackListSet
        );
        this.counter = counter;
        this.barrier = barrier;
        this.finishedList = finishedList;
    }

    @Override
    public void run() {
        while (!queue.isEmpty() || counter.get() > 0) {
//            System.out.println("COADA " + queue.isEmpty());
//            System.out.println("COUNTER CONS" + counter.get());
            try {
                listManager.processParticipant(queue.pop());
            } catch (Exception e) {
//                System.out.println("THREW EXCEPTION IN CONSUMER");
//                System.out.println(e.getMessage());
            }
        }
        System.out.println("Consumer Finished");

        try {
            barrier.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        finishedList.set(true);

    }
}
