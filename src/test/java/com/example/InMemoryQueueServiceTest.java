package com.example;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;

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
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
        InMemoryQueue inMemoryQueue = memoryQueueService.getQueueByName("MyQueue1");
        Assert.assertNotNull(inMemoryQueue);
        Assert.assertEquals(3, inMemoryQueue.size());
    }

    @Test
    public void testPushMessagesToThreeQueues() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);

        result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue2", "apple", "banana", "orange");
        Assert.assertTrue(result);

        result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue3", "car", "bus", "train");
        Assert.assertTrue(result);

        InMemoryQueue inMemoryQueue1 = memoryQueueService.getQueueByName("MyQueue1");
        InMemoryQueue inMemoryQueue2 = memoryQueueService.getQueueByName("MyQueue2");
        InMemoryQueue inMemoryQueue3 = memoryQueueService.getQueueByName("MyQueue3");
        
        Assert.assertNotNull(inMemoryQueue1);
        Assert.assertNotNull(inMemoryQueue2);
        Assert.assertNotNull(inMemoryQueue3);
        
        Assert.assertEquals(3, inMemoryQueue1.size());
        Assert.assertEquals(3, inMemoryQueue2.size());
        Assert.assertEquals(3, inMemoryQueue3.size());
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
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
        Message message1FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("hello", message1FromQueue.getData());
        Message message2FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("world", message2FromQueue.getData());
        Message message3FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("java", message3FromQueue.getData());
        Message message4FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertNull(message4FromQueue);
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
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
        Assert.assertFalse(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", null));
    }

    @Test
    public void testDeleteWhenMessageIdIsNotExist() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
        Assert.assertFalse(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "123"));
    }

    @Test
    public void testDeleteMessageInVisibleTimeout() {
        boolean result = memoryQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);

        Message message1FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("hello", message1FromQueue.getData());

        Assert.assertTrue(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", message1FromQueue.getReceiptHandle()));

        Message message2FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("world", message2FromQueue.getData());

        Message message3FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("java", message3FromQueue.getData());

        Assert.assertTrue(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", message3FromQueue.getReceiptHandle()));

        /** Mock the message invisible time is timeout */
        message2FromQueue.setVisibleDate(new Date(System.currentTimeMillis() - 1));

        /** Mock timer triggers the run method */
        InMemoryQueue queue = memoryQueueService.getQueueByName("MyQueue1");
        queue.updateMsgIntoVisibleState();

        Assert.assertFalse(memoryQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", message2FromQueue.getReceiptHandle()));

        Assert.assertEquals(1, memoryQueueService.getQueueByName("MyQueue1").size());

        Message message4FromQueue = memoryQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals(message2FromQueue, message4FromQueue);
    }
}
