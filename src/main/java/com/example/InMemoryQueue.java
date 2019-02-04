package com.example;

import java.util.Date;
import java.util.Iterator;
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

    /** Get First Message from queue */
    public synchronized Message pull() {
        ConcurrentLinkedQueue<Message> myQueue = getQueue();

        /** Find the first visible message from queue */
        Message myMessage = null;
        Iterator<Message> iterator = myQueue.iterator();
        while (iterator.hasNext()) {
            myMessage = iterator.next();
            if (myMessage.getVisibleDate() == null) {
                break;
            }
        }

        /** If the queue is empty, or if there is no visible message, return null */
        if (myMessage == null || myMessage.getVisibleDate() != null) {
            return null;
        }

        /** Make the message invisible by setting the visible date of the message */
        Date date = QueueVisibilityTimeout.createVisibleDate(myMessage.getVisibleTimeout());
        myMessage.setVisibleDate(date);
        return myMessage;
    }
}
