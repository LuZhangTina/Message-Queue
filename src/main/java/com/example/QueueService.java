package com.example;

/**
 * Created by tina on 2019/2/4.
 */
public interface QueueService {
    /** Push messages in queue.
     *
     *  The argument queueUrl simulates the Amazon SQS Queue URLs :
     *  the structure is as follows:
     *  https://{REGION_ENDPOINT}/queue.|api-domain|/{YOUR_ACCOUNT_NUMBER}/{YOUR_QUEUE_NAME}
     *
     *  The argument visibilityTimeout represents Amazon SQS Visibility Timeout.
     *  The default visibility timeout for a message is 30 seconds.
     *  The minimum is 0 seconds.
     *  The maximum is 12 hours.
     */
    boolean push(String queueUrl, String... messages);

    /** Get the first message from queue.
     *
     *  The argument queueUrl simulates the Amazon SQS Queue URLs :
     *  the structure is as follows:
     *  https://{REGION_ENDPOINT}/queue.|api-domain|/{YOUR_ACCOUNT_NUMBER}/{YOUR_QUEUE_NAME}
     */
    Message pull(String queueUrl, Integer... visibilityTimeout);

    /** delete the message with specified receiptHandle from queue.
     *
     *  The argument queueUrl simulates the Amazon SQS Queue URLs :
     *  the structure is as follows:
     *  https://{REGION_ENDPOINT}/queue.|api-domain|/{YOUR_ACCOUNT_NUMBER}/{YOUR_QUEUE_NAME}
     *
     *  The argument receiptHandle can be got when does the pull action
     */
    boolean delete(String queueUrl, String receiptHandle);
}
