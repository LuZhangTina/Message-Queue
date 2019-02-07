package com.example;

/**
 * Created by tina on 2019/2/7.
 */
public class InFileQueueService implements QueueService {
    public InFileQueueService() {
        // do nothing
    }

    @Override
    public boolean push(String queueUrl, Integer visibilityTimeout, String... messages) {
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        int msgVisibleTimeout;
        if (visibilityTimeout == null) {
            /** If the parameter of visibleTimeout is null, use the default timeout which is 30 seconds */
            msgVisibleTimeout = QueueProperties.getDefaultVisibilityTimeout();
        } else {
            /** If the visibility timeout if out of the valid range, push fails */
            msgVisibleTimeout = visibilityTimeout.intValue();
            if (msgVisibleTimeout < QueueProperties.getMinVisibilityTimeout() || msgVisibleTimeout > QueueProperties.getMaxVisibilityTimeout()) {
                return false;
            }
        }

        /** If there is nothing to be pushed, push success */
        if (messages == null || messages.length == 0) {
            return true;
        }

        /**  add messages one by one in the end of the queue */
        InFileQueue fileQueue = new InFileQueue(queueName);
        for (String data : messages) {
            Message messageNode = new Message(data, msgVisibleTimeout);
            fileQueue.push(messageNode);
        }

        return true;
    }

    @Override
    public Message pull(String queueUrl) {
        /** Get queue name from URL.
         *  If there is no valid queueName, pull fails, return null */
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return null;
        }

        InFileQueue fileQueue = new InFileQueue(queueName);

        return fileQueue.pull();
    }

    @Override
    public boolean delete(String queueUrl, String messageId) {
        /** Get queue name from URL.
         *  If there is no valid queueName, delete fails, return false */
        String queueName = QueueProperties.getQueueNameByUrl(queueUrl);
        if (queueName == null) {
            return false;
        }

        /** Input messageId is illegal, return false */
        if (messageId == null || messageId.length() == 0) {
            return false;
        }

        InFileQueue fileQueue = new InFileQueue(queueName);
        return fileQueue.delete(messageId);
    }

    public InFileQueue getInFileQueue(String queueName) {
        return new InFileQueue(queueName);
    }
}
