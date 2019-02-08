package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

/**
 * Created by tina on 2019/2/8.
 */
public class SqsQueueService implements QueueService {
    private AmazonSQSClient sqsClient;

    public SqsQueueService(AmazonSQSClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public AmazonSQSClient getSqsClient() {
        return this.sqsClient;
    }

    @Override
    public boolean push(String queueUrl, String... messages) {
        AmazonSQSClient sqsClient = getSqsClient();

        for (String message : messages) {
            sqsClient.sendMessage(queueUrl, message);
        }
        return true;
    }

    @Override
    public Message pull(String queueUrl, Integer... visibilityTimeout) {
        Message message = null;
        AmazonSQSClient sqsClient = getSqsClient();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest().withQueueUrl(queueUrl).withMaxNumberOfMessages(1);
        if (visibilityTimeout.length == 1) {
            receiveMessageRequest.setVisibilityTimeout(visibilityTimeout[0]);
        }

        List<com.amazonaws.services.sqs.model.Message> messageResults = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
        for (com.amazonaws.services.sqs.model.Message messageFromResult : messageResults) {
            String messageReceiptHandle = messageFromResult.getReceiptHandle();
            String messageData = messageFromResult.getBody();
            message = new Message(messageData);
            message.setReceiptHandle(messageReceiptHandle);
        }

        return message;
    }

    @Override
    public boolean delete(String queueUrl, String receiptHandle) {
        AmazonSQSClient sqsClient = getSqsClient();
        sqsClient.deleteMessage(queueUrl, receiptHandle);
        return true;
    }
}
