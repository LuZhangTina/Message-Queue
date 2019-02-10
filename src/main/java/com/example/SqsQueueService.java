package com.example;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

/**
 * Created by tina on 2019/2/8.
 */
public class SqsQueueService implements QueueService {
    private AmazonSQS sqsClient;

    public SqsQueueService(AmazonSQS sqsClient) {
        this.sqsClient = sqsClient;
    }

    public AmazonSQS getSqsClient() {
        return this.sqsClient;
    }

    @Override
    public boolean push(String queueUrl, String... messages) {
        AmazonSQS sqsClient = getSqsClient();

        for (String message : messages) {
            SendMessageRequest messageRequest = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(message)
                    .withMessageGroupId("messageGroupId1");
            sqsClient.sendMessage(messageRequest);
        }
        return true;
    }

    @Override
    public Message pull(String queueUrl, Integer... visibilityTimeout) {
        Message message = null;
        AmazonSQS sqsClient = getSqsClient();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(1);
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
        AmazonSQS sqsClient = getSqsClient();
        sqsClient.deleteMessage(queueUrl, receiptHandle);
        return true;
    }
}
