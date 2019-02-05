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
        Map<String, InMemoryQueue> myQueues = getQueues();
        if (myQueues.containsKey(queueName)) {
            return myQueues.get(queueName);
        } else {
            return null;
        }
    }

    public synchronized InMemoryQueue createQueueByName(String queueName) {
        Map<String, InMemoryQueue> myQueues = getQueues();
        if (myQueues.containsKey(queueName)) {
            return myQueues.get(queueName);
        } else {
            InMemoryQueue queue = new InMemoryQueue();
            myQueues.put(queueName, queue);
            return queue;
        }
    }

    private String getQueueNameByUrl(String queueUrl) {
        if (queueUrl == null) {
            return null;
        }

        int index = queueUrl.lastIndexOf('/');
        if (index == -1) {
            return null;
        }

        String queueName = queueUrl.substring(index + 1);
        return queueName;
    }

    @Override
    public boolean push(String queueUrl, Integer visibilityTimeout, String... messages) {
        /** Get queue name from URL, if there is no valid queueName, push fails */
        String queueName = getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        /** Find the queue named as queueName.
         *  If there is no specified queue, create a new queue with the queueName */
        InMemoryQueue myQueue = getQueueByName(queueName);
        if (myQueue == null) {
            myQueue = createQueueByName(queueName);
        }

        int msgVisibleTimeout;
        if (visibilityTimeout == null) {
            /** If the parameter of delay is null, use the default timeout which is 30 seconds */
            msgVisibleTimeout = QueueVisibilityTimeout.getDefaultVisibilityTimeout();
        } else {
            /** If the visibility timeout if out of the valid range, push fails */
            msgVisibleTimeout = visibilityTimeout.intValue();
            if (msgVisibleTimeout < QueueVisibilityTimeout.getMinVisibilityTimeout() || msgVisibleTimeout > QueueVisibilityTimeout.getMaxVisibilityTimeout()) {
                return false;
            }
        }

        /** If there is nothing to be push, push success */
        if (messages == null || messages.length == 0) {
            return true;
        }

        /**  add messages one by one in the end of the queue */
        for (String data : messages) {
            Message messageNode = new Message(data, msgVisibleTimeout);
            myQueue.push(messageNode);
        }

        return true;
    }

    @Override
    public Message pull(String queueUrl) {
        /** Get queue name from URL.
         *  If there is no valid queueName, pull fails, return null */
        String queueName = getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return null;
        }

        /** Find the queue named as queueName.
         *  If there is no specified queue, pull fails, return null */
        InMemoryQueue myQueue = getQueueByName(queueName);
        if (myQueue == null) {
            return null;
        }

        return myQueue.pull();
    }

    @Override
    public boolean delete(String queueUrl, String messageId) {
        /** Get queue name from URL.
         *  If there is no valid queueName, delete fails, return false */
        String queueName = getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        /** Find the queue named as queueName.
         *  If there is no specified queue, delete fails, return false */
        InMemoryQueue myQueue = getQueueByName(queueName);
        if (myQueue == null) {
            return false;
        }

        /** Input messageId is illegal, return false */
        if (messageId == null || messageId.length() == 0) {
            return false;
        }

        return myQueue.delete(messageId);
    }
}
