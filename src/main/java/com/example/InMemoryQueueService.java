package com.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tina on 2019/2/5.
 */
public class InMemoryQueueService implements QueueService {
    private Map<String, InMemoryQueue> queues;

    public InMemoryQueueService() {
        this.queues = new ConcurrentHashMap<>();
    }

    private Map<String, InMemoryQueue> getQueues() {
        return this.queues;
    }

    public synchronized InMemoryQueue getQueueByName(String queueName) {
        Map<String, InMemoryQueue> queues = getQueues();
        if (queues.containsKey(queueName)) {
            return queues.get(queueName);
        } else {
            return null;
        }
    }

    public synchronized InMemoryQueue createQueueByName(String queueName) {
        Map<String, InMemoryQueue> queues = getQueues();
        if (queues.containsKey(queueName)) {
            return queues.get(queueName);
        } else {
            InMemoryQueue queue = new InMemoryQueue();
            queues.put(queueName, queue);
            return queue;
        }
    }

    @Override
    public boolean push(String queueUrl, String... messages) {
        /** Get queue name from URL, if there is no valid queueName, push fails */
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        /** Find the queue named as queueName.
         *  If there is no specified queue, create a new queue with the queueName */
        InMemoryQueue queue = getQueueByName(queueName);
        if (queue == null) {
            queue = createQueueByName(queueName);
        }

        /** If there is nothing to be pushed, push success */
        if (messages == null || messages.length == 0) {
            return true;
        }

        /**  add messages one by one in the end of the queue */
        for (String data : messages) {
            Message messageNode = new Message(data);
            queue.push(messageNode);
        }

        return true;
    }

    @Override
    public Message pull(String queueUrl, Integer... visibilityTimeout) {
        /** Get queue name from URL.
         *  If there is no valid queueName, pull fails, return null */
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return null;
        }

        /** Find the queue named as queueName.
         *  If there is no specified queue, pull fails, return null */
        InMemoryQueue queue = getQueueByName(queueName);
        if (queue == null) {
            return null;
        }

        /** If consumer set the legal visibilityTimeout, use the set visibilityTimeout
         *  otherwise use default visibility timeout */
        int msgVisibilityTimeout = QueueProperties.getDefaultVisibilityTimeout();
        if (visibilityTimeout.length == 1
                && visibilityTimeout[0] >= QueueProperties.getMinVisibilityTimeout()
                && visibilityTimeout[0] <= QueueProperties.getMaxVisibilityTimeout()) {
                msgVisibilityTimeout = visibilityTimeout[0];
        }

        return queue.pull(msgVisibilityTimeout);
    }

    @Override
    public boolean delete(String queueUrl, String receiptHandle) {
        /** Get queue name from URL.
         *  If there is no valid queueName, delete fails, return false */
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        /** Find the queue named as queueName.
         *  If there is no specified queue, delete fails, return false */
        InMemoryQueue queue = getQueueByName(queueName);
        if (queue == null) {
            return false;
        }

        /** Input receiptHandle is illegal, return false */
        if (receiptHandle == null || receiptHandle.length() == 0) {
            return false;
        }

        return queue.delete(receiptHandle);
    }
}
