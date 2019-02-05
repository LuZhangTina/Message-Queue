package com.example;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tina on 2019/2/4.
 */
public class InMemoryQueueTest {
    @Test
    public void testPushOneMessage() {
        Message message = new Message("hello", 2);
        InMemoryQueue myQueue = new InMemoryQueue();
        myQueue.push(message);
        Assert.assertEquals(1, myQueue.getQueueSize());
    }

    @Test
    public void testPushTwoMessage() {
        Message message1 = new Message("hello", 2);
        Message message2 = new Message("world", 3);
        InMemoryQueue myQueue = new InMemoryQueue();
        myQueue.push(message1);
        myQueue.push(message2);
        Assert.assertEquals(2, myQueue.getQueueSize());
    }

    @Test
    public void testPullOnceWhenQueueIsEmpty() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message = myQueue.pull();
        Assert.assertNull(message);
    }

    @Test
    public void testPullOnceWhenQueueHasOneMessage() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message = new Message("hello", 2);
        myQueue.push(message);
        Message myMessage = myQueue.pull();
        Assert.assertEquals(message, myMessage);
    }

    @Test
    public void testPullTwiceWhenQueueHasOneMessage() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message = new Message("hello", 2);
        myQueue.push(message);
        Message myMessage = myQueue.pull();
        Assert.assertEquals(message, myMessage);
        Message myMessage2 = myQueue.pull();
        Assert.assertNull(myMessage2);
    }

    @Test
    public void testPullTwiceWhenQueueHasTwoMessages() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        myQueue.push(message1);

        Message message2 = new Message("world", 3);
        myQueue.push(message2);

        Message myMessage1 = myQueue.pull();
        Assert.assertEquals(message1, myMessage1);
        Message myMessage2 = myQueue.pull();
        Assert.assertEquals(message2, myMessage2);
    }

    @Test
    public void testDeleteMessageWhenQueueIsEmpty() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Assert.assertFalse(myQueue.delete("123456"));
    }

    @Test
    public void testDeleteMessageWhenMessageIsNotInQueue() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        myQueue.push(message1);
        Message myMessage = myQueue.pull();
        String messageId = myMessage.getMessageId();
        messageId = messageId + "1";
        Assert.assertFalse(myQueue.delete(messageId));
    }

    @Test
    public void testDeleteMessageWhenMessageNotBePulledFirst() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        myQueue.push(message1);
        String messageId1 = message1.getMessageId();
        Assert.assertFalse(myQueue.delete(messageId1));
    }

    @Test
    public void testDeleteMessageWhenMessageIsInvisible() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        myQueue.push(message1);
        Message message2 = new Message("world", 3);
        myQueue.push(message2);
        String messageId1 = message1.getMessageId();
        String messageId2 = message2.getMessageId();
        Assert.assertFalse(myQueue.delete(messageId1));
        myQueue.pull();
        myQueue.pull();
        Assert.assertTrue(myQueue.delete(messageId1));
        Assert.assertFalse(myQueue.delete(messageId1));
        Assert.assertTrue(myQueue.delete(messageId2));
        Assert.assertFalse(myQueue.delete(messageId2));
        Assert.assertEquals(0, myQueue.getQueueSize());
    }
}
