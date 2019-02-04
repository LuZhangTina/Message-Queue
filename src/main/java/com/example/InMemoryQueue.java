package com.example;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tina on 2019/2/4.
 */
public class InMemoryQueue {
    private final ConcurrentLinkedQueue<Message> queue;

    public InMemoryQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public ConcurrentLinkedQueue<Message> getQueue() {
        return this.queue;
    }

    public int getQueueSize() {
        ConcurrentLinkedQueue<Message> myQueue = getQueue();
        return myQueue.size();
    }

    /** Push the message in queue */
    public synchronized void push(Message message) {
        ConcurrentLinkedQueue<Message> myQueue = getQueue();
        myQueue.offer(message);
    }
}
