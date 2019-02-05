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

    @Test
    public void testPullWhenUrlIsNull() {
        Assert.assertNull(memoryQueueService.pull(null));
    }

    @Test
    public void testPullWhenUrlIsIllegal() {
        Assert.assertNull(memoryQueueService.pull("queueUrlIsIllegal"));
    }

    @Test
    public void testPullWhenQueueDoesNotExist() {
        Assert.assertNull(memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1"));
    }

    @Test
    public void testPullWhenQueueIsEmpty() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", null);
        Assert.assertTrue(result);
        Assert.assertNull(memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1"));
    }

    @Test
    public void testPullWhenQueueIsNotEmpty() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 2, "hello", "world", "java");
        Assert.assertTrue(result);
        Message myMessage1 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("hello", myMessage1.getData());
        Message myMessage2 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("world", myMessage2.getData());
        Message myMessage3 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("java", myMessage3.getData());
        Message myMessage4 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertNull(myMessage4);
    }

    @Test
    public void testDeleteWhenUrlIsNull() {
        Assert.assertFalse(memoryQueueService.delete(null, "123"));
    }

    @Test
    public void testDeleteWhenUrlIsNotIllegal() {
        Assert.assertFalse(memoryQueueService.delete("queueUrlIsIllegal", "123"));
    }

    @Test
    public void testDeleteWhenQueueIsNotExist() {
        Assert.assertFalse(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "123"));
    }

    @Test
    public void testDeleteWhenMessageIdIsNull() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 2, "hello", "world", "java");
        Assert.assertTrue(result);
        Assert.assertFalse(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", null));
    }

    @Test
    public void testDeleteWhenMessageIdIsNotExist() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 2, "hello", "world", "java");
        Assert.assertTrue(result);
        Assert.assertFalse(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "123"));
    }

    @Test
    public void testDeleteMessageInVisibleTimeout() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 2, "hello", "world", "java");
        Assert.assertTrue(result);

        Message myMessage1 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("hello", myMessage1.getData());

        Assert.assertTrue(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", myMessage1.getMessageId()));

        Message myMessage2 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("world", myMessage2.getData());
        Message myMessage3 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("java", myMessage3.getData());

        Assert.assertTrue(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", myMessage3.getMessageId()));

        try {
            Thread.sleep(3000);
            Assert.assertFalse(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", myMessage2.getMessageId()));
            Assert.assertEquals(1, memoryQueueService.getQueueByName("MyQueue1").getQueueSize());
            Message myMessage4 = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
            Assert.assertEquals(myMessage2, myMessage4);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
