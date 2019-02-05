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

    @Test
    public void testMessagePulledButNotDeleted() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        myQueue.push(message1);
        Assert.assertNull(message1.getVisibleDate());

        /** Pull the message */
        Message myMessage = myQueue.pull();
        Assert.assertNotNull(myMessage.getVisibleDate());
        try {
            /** Wait for 1 second, the pulled out message is invisible */
            Thread.sleep(1000);
            Assert.assertNotNull(myMessage.getVisibleDate());

            /** Pull one more message, there is no new message can be pulled */
            Message message2 = myQueue.pull();
            Assert.assertNull(message2);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        try {
            /** Wait for 2 second, the pulled out message is visible again */
            Thread.sleep(2000);
            Assert.assertNull(myMessage.getVisibleDate());

            /** Pull a new message, the new message is the message just pulled out */
            Assert.assertEquals(myMessage, myQueue.pull());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testMessagePulledAndDeleted() {
        InMemoryQueue myQueue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        myQueue.push(message1);

        Message message2 = new Message("world", 3);
        myQueue.push(message2);

        Message message3 = new Message("java", 3);
        myQueue.push(message3);

        /** Messages are visible in queue */
        Assert.assertNull(message1.getVisibleDate());
        Assert.assertNull(message2.getVisibleDate());
        Assert.assertNull(message3.getVisibleDate());
        Assert.assertEquals(3, myQueue.getQueueSize());

        /** Pull Three messages */
        Message myMessage1 = myQueue.pull();
        Assert.assertNotNull(myMessage1.getVisibleDate());
        Message myMessage2 = myQueue.pull();
        Assert.assertNotNull(myMessage2.getVisibleDate());
        Message myMessage3 = myQueue.pull();
        Assert.assertNotNull(myMessage3.getVisibleDate());
        try {
            /** Wait for 1 second, delete the second message which was pulled out */
            Thread.sleep(1000);
            Assert.assertTrue(myQueue.delete(myMessage2.getMessageId()));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        try {
            /** Wait for 3 second, pull two messages again */
            Thread.sleep(3000);
            Message myMessage4 = myQueue.pull();
            Message myMessage5 = myQueue.pull();
            Assert.assertEquals(myMessage1, myMessage4);
            Assert.assertEquals(myMessage3, myMessage5);
            Assert.assertEquals(2, myQueue.getQueueSize());
            myQueue.delete(myMessage4.getMessageId());
            myQueue.delete(myMessage5.getMessageId());
            Assert.assertEquals(0, myQueue.getQueueSize());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
