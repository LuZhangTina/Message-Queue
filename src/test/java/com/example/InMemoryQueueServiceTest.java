package com.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by tina on 2019/2/5.
 */
public class InMemoryQueueServiceTest {
    private InMemoryQueueService memoryQueueService;
    @Before
    public void runPreCondition() {
        this.memoryQueueService = new InMemoryQueueService();
    }

    @Test
    public void testPushMessageWhenUrlIsNull() {
        boolean result = memoryQueueService.push(null, null, "hello");
        Assert.assertFalse(result);
    }

    @Test
    public void testPushMessageWhenUrlIsIllegal() {
        boolean result = memoryQueueService.push("queueUrlIllegal", null, "hello");
        Assert.assertFalse(result);
    }

    @Test
    public void testPushMessageWhenMessageIsNull() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", null, null);
        Assert.assertTrue(result);
        InMemoryQueue inMemoryQueue = memoryQueueService.getQueueByName("MyQueue1");
        Assert.assertNotNull(inMemoryQueue);
    }

    @Test
    public void testPushThreeMessages() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 2, "hello", "world", "java");
        Assert.assertTrue(result);
        InMemoryQueue inMemoryQueue = memoryQueueService.getQueueByName("MyQueue1");
        Assert.assertNotNull(inMemoryQueue);
        Assert.assertEquals(3, inMemoryQueue.getQueueSize());
    }

    @Test
    public void testPushMessagesToThreeQueues() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 2, "hello", "world", "java");
        Assert.assertTrue(result);

        result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue2", 2, "apple", "banana", "orange");
        Assert.assertTrue(result);

        result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue3", 2, "car", "bus", "train");
        Assert.assertTrue(result);

        InMemoryQueue inMemoryQueue1 = memoryQueueService.getQueueByName("MyQueue1");
        InMemoryQueue inMemoryQueue2 = memoryQueueService.getQueueByName("MyQueue2");
        InMemoryQueue inMemoryQueue3 = memoryQueueService.getQueueByName("MyQueue3");
        Assert.assertNotNull(inMemoryQueue1);
        Assert.assertNotNull(inMemoryQueue2);
        Assert.assertNotNull(inMemoryQueue3);
        Assert.assertEquals(3, inMemoryQueue1.getQueueSize());
        Assert.assertEquals(3, inMemoryQueue2.getQueueSize());
        Assert.assertEquals(3, inMemoryQueue3.getQueueSize());
    }
}
