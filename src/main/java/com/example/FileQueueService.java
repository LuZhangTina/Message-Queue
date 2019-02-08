package com.example;

/**
 * Created by tina on 2019/2/7.
 */
public class FileQueueService implements QueueService {
    public FileQueueService() {
        // do nothing
    }

    @Override
    public boolean push(String queueUrl, String... messages) {
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        /** If there is nothing to be pushed, push success */
        if (messages == null || messages.length == 0) {
            return true;
        }

        /**  add messages one by one in the end of the queue */
        FileQueue fileQueue = new FileQueue(queueName);
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

        FileQueue fileQueue = new FileQueue(queueName);

        /** If consumer set the legal visibilityTimeout, use the set visibilityTimeout
         *  otherwise use default visibility timeout */
        int msgVisibilityTimeout = QueueProperties.getDefaultVisibilityTimeout();
        if (visibilityTimeout.length == 1) {
            if (visibilityTimeout[0] >= QueueProperties.getMinVisibilityTimeout()
                    && visibilityTimeout[0] <= QueueProperties.getMaxVisibilityTimeout()) {
                msgVisibilityTimeout = visibilityTimeout[0];
            }
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

        /** Input receiptHandle is illegal, return false */
        if (receiptHandle == null || receiptHandle.length() == 0) {
            return false;
        }

        FileQueue fileQueue = new FileQueue(queueName);
        return fileQueue.delete(receiptHandle);
    }

    public FileQueue getInFileQueue(String queueName) {
        return new FileQueue(queueName);
    }
}
