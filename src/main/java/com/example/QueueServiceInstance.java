package com.example;

import com.amazonaws.services.sqs.AmazonSQS;

/**
 * Created by tina on 2019/2/12.
 */
public class QueueServiceInstance {
    private QueueServiceInstance() {
        // do nothing
    }

    public static QueueService getQueueServiceInstance(String propertiesPath, AmazonSQS sqsClient) {
        ConfigProperties properties = new ConfigProperties(propertiesPath);
        String queueType = properties.readConfig();
        if ("InMemoryQueue".equalsIgnoreCase(queueType)) {
            return new InMemoryQueueService();
        } else if ("FileQueue".equalsIgnoreCase(queueType)) {
            return new FileQueueService();
        } else {
            return new SqsQueueService(sqsClient);
        }
    }
}
