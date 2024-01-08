package org.example;

import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadSafeQueue<T> {
    private Queue<T> queue;
    private final Integer capacity;
    final Lock lock = new ReentrantLock();
    final Condition notEmpty = lock.newCondition();

    private AtomicInteger counter;

    public ThreadSafeQueue(Queue<T> queue, Integer capacity, AtomicInteger counter) {
        this.queue = queue;
        this.capacity = capacity;
        this.counter = counter;
    }

    boolean isEmpty() {
        int len;
        lock.lock();
        len = queue.size();
        lock.unlock();
        return len == 0;
    }

    void push(T element) throws InterruptedException {
        lock.lock();
        try {
            queue.add(element);
            notEmpty.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    T pop() throws Exception {
        lock.lock();
//        System.out.println("CAUGHT LOCK");
        try {
            while (isEmpty()) {
                long timeout = 2000;
                if(!notEmpty.await(timeout, TimeUnit.MILLISECONDS))
                {
                    throw new Exception("TIMEOUT");
                }
            }
            return queue.poll();
        }
        catch (Exception e){
            throw new Exception(e);
        }
        finally {
            lock.unlock();
//            System.out.println("FREED LOCK");
        }
    }
}

