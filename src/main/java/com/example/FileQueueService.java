package com.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tina on 2019/2/7.
 */
public class FileQueueService implements QueueService {
    private Map<String, FileQueue> queues;

    public FileQueueService() {
        this.queues = new ConcurrentHashMap<>();
    }

    private Map<String, FileQueue> getQueues() {
        return this.queues;
    }

    public synchronized FileQueue getQueueByName(String queueName) {
        Map<String, FileQueue> queues = getQueues();
        if (queues.containsKey(queueName)) {
            return queues.get(queueName);
        } else {
            return null;
        }
    }

    public synchronized FileQueue createQueueByName(String queueName) {
        Map<String, FileQueue> queues = getQueues();
        if (queues.containsKey(queueName)) {
            return queues.get(queueName);
        } else {
            FileQueue queue = new FileQueue(queueName);
            queues.put(queueName, queue);
            return queue;
        }
    }

    @Override
    public boolean push(String queueUrl, String... messages) {
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        /** Find the queue named as queueName.
         *  If there is no specified queue, create a new queue with the queueName */
        FileQueue fileQueue = getQueueByName(queueName);
        if (fileQueue == null) {
            fileQueue = createQueueByName(queueName);
        }

        /** If there is nothing to be pushed, push success */
        if (messages == null || messages.length == 0) {
            return true;
        }

        /**  add messages one by one in the end of the queue */
        for (String data : messages) {
            Message messageNode = new Message(data);
            fileQueue.push(messageNode);
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
         *  If there is no specified queue, create the queue */
        FileQueue fileQueue = getQueueByName(queueName);
        if (fileQueue == null) {
            fileQueue = createQueueByName(queueName);
        }

        /** If consumer set the legal visibilityTimeout, use the set visibilityTimeout
         *  otherwise use default visibility timeout */
        int msgVisibilityTimeout = QueueProperties.getDefaultVisibilityTimeout();
        if (visibilityTimeout.length == 1
                && visibilityTimeout[0] >= QueueProperties.getMinVisibilityTimeout()
                && visibilityTimeout[0] <= QueueProperties.getMaxVisibilityTimeout()) {
                msgVisibilityTimeout = visibilityTimeout[0];
        }

        return fileQueue.pull(msgVisibilityTimeout);
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
        FileQueue fileQueue = getQueueByName(queueName);
        if (fileQueue == null) {
            return false;
        }

        /** Input receiptHandle is illegal, return false */
        if (receiptHandle == null || receiptHandle.length() == 0) {
            return false;
        }

        return fileQueue.delete(receiptHandle);
    }

    public FileQueue getInFileQueue(String queueName) {
        return new FileQueue(queueName);
    }
}
