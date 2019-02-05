package com.example;

import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by tina on 2019/2/4.
 */
public class InMemoryQueue {
    private final ConcurrentLinkedQueue<Message> queue;

    /** The timer's period is 1 second
     *  The timer aims to find out messages which are not be deleted during visibleTimeout,
     *  Then push those messages in queue top again.
     *  Those messages will be allowed to be pulled by consumers again. */
    private final Timer timer;
    private final TimerTask timerTask;

    public InMemoryQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.timer = new Timer();
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                ConcurrentLinkedQueue<Message> queue = getQueue();

                /** Find the invisible messages in the queue.
                 *  if the message's invisible timeout,
                 *  make the message visible again by setting the visibleDate to null */
                Iterator<Message> iterator = queue.iterator();
                while (iterator.hasNext()) {
                    Message messageFromQueue = iterator.next();
                    Date visibleDate = messageFromQueue.getVisibleDate();
                    if (visibleDate != null) {
                        if (visibleDate.before(new Date())) {
                            messageFromQueue.setVisibleDate(null);
                        }
                    }
                }
            }
        };
    }

    public ConcurrentLinkedQueue<Message> getQueue() {
        return this.queue;
    }

    public int size() {
        ConcurrentLinkedQueue<Message> queue = getQueue();
        return queue.size();
    }

    /** Push the message in queue */
    public synchronized void push(Message message) {
        ConcurrentLinkedQueue<Message> queue = getQueue();

        /** If the queue is Empty, start the 1 second timer.
         *  The timer's aim is to scan the queue,
         *  then make the messages which are not be deleted during the visibleTimeout visibility again */
        if (queue.isEmpty()) {
            Timer timer = getTimer();
            TimerTask timerTask = getTimerTask();
            timer.schedule(timerTask, 0, 1000);
        }

        queue.offer(message);
    }

    /** Get First Message from queue */
    public synchronized Message pull() {
        ConcurrentLinkedQueue<Message> queue = getQueue();

        /** Find the first visible message from queue */
        Message messageFromQueue = null;
        Iterator<Message> iterator = queue.iterator();
        while (iterator.hasNext()) {
            messageFromQueue = iterator.next();
            if (messageFromQueue.getVisibleDate() == null) {
                break;
            }
        }

        /** If the queue is empty, or if there is no visible message, return null */
        if (messageFromQueue == null || messageFromQueue.getVisibleDate() != null) {
            return null;
        }

        /** Make the message invisible by setting the visible date of the message */
        Date date = QueueVisibilityTimeout.createVisibleDate(messageFromQueue.getVisibleTimeout());
        messageFromQueue.setVisibleDate(date);
        return messageFromQueue;
    }

    /** If message with the specified messageId is in the queue and invisible,
     *  delete the message, return true. Otherwise, return false */
    public synchronized boolean delete(String messageId) {
        boolean result = false;
        ConcurrentLinkedQueue<Message> queue = getQueue();

        /** Find the invisible message which has the specified messageId from queue */
        Message messageFromQueue = null;
        Iterator<Message> iterator = queue.iterator();
        while (iterator.hasNext()) {
            messageFromQueue = iterator.next();
            if (messageFromQueue.getVisibleDate() != null) {
                if (messageFromQueue.getMessageId().equals(messageId)) {
                    queue.remove(messageFromQueue);
                    result = true;
                    break;
                }
            }
        }

        /** If the queue is empty, stop the timer */
        if (queue.isEmpty()) {
            Timer timer = getTimer();
            timer.cancel();
        }

        return result;
    }

    private Timer getTimer() {
        return this.timer;
    }

    private TimerTask getTimerTask() {
        return this.timerTask;
    }
}
