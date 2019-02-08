package com.example;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by tina on 2019/2/4.
 */
public class InMemoryQueueTest {
    @Test
    public void testPushOneMessage() {
        Message message = new Message("hello");
        InMemoryQueue queue = new InMemoryQueue();
        queue.push(message);
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void testPushTwoMessage() {
        Message message1 = new Message("hello");
        Message message2 = new Message("world");
        InMemoryQueue queue = new InMemoryQueue();
        queue.push(message1);
        queue.push(message2);
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void testPullOnceWhenQueueIsEmpty() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message = queue.pull(2);
        Assert.assertNull(message);
    }

    @Test
    public void testPullOnceWhenQueueHasOneMessage() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message = new Message("hello");
        queue.push(message);
        Message messageAtTop = queue.pull(2);
        Assert.assertEquals(message, messageAtTop);
    }

    @Test
    public void testPullTwiceWhenQueueHasOneMessage() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message = new Message("hello");
        queue.push(message);
        Message message1FromQueue = queue.pull(2);
        Assert.assertEquals(message, message1FromQueue);
        Message message2FromQueue = queue.pull(3);
        Assert.assertNull(message2FromQueue);
    }

    @Test
    public void testPullTwiceWhenQueueHasTwoMessages() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello");
        queue.push(message1);

        Message message2 = new Message("world");
        queue.push(message2);

        Message message1FromQueue = queue.pull(2);
        Assert.assertEquals(message1, message1FromQueue);
        Message message2FromQueue = queue.pull(3);
        Assert.assertEquals(message2, message2FromQueue);
    }

    @Test
    public void testDeleteMessageWhenQueueIsEmpty() {
        InMemoryQueue queue = new InMemoryQueue();
        Assert.assertFalse(queue.delete("123456"));
    }

    @Test
    public void testDeleteMessageWhenMessageIsNotInQueue() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello");
        queue.push(message1);
        Message messageFromQueue = queue.pull(2);
        String receiptHandle = messageFromQueue.getReceiptHandle();
        receiptHandle = receiptHandle + "1";
        Assert.assertFalse(queue.delete(receiptHandle));
    }

    @Test
    public void testDeleteMessageWhenMessageNotBePulledFirst() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello");
        queue.push(message1);
        String receiptHandle = message1.getReceiptHandle();
        Assert.assertFalse(queue.delete(receiptHandle));
    }

    @Test
    public void testDeleteMessageWhenMessageIsInvisible() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello");
        queue.push(message1);
        Message message2 = new Message("world");
        queue.push(message2);

        String receiptHandle1 = message1.getReceiptHandle();
        String receiptHandle2 = message2.getReceiptHandle();
        Assert.assertFalse(queue.delete(receiptHandle1));

        /** Pull two messages out from queue */
        queue.pull(2);
        queue.pull(3);

        Assert.assertTrue(queue.delete(receiptHandle1));
        Assert.assertFalse(queue.delete(receiptHandle1));

        Assert.assertTrue(queue.delete(receiptHandle2));
        Assert.assertFalse(queue.delete(receiptHandle2));

        /** The queue is empty after two messages are deleted */
        Assert.assertEquals(0, queue.size());
    }

    @Test
    public void testMessagePulledButNotDeleted() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello");
        queue.push(message1);
        Assert.assertNull(message1.getVisibleDate());

        /** Pull the message */
        Message message1FromQueue = queue.pull(2);
        Assert.assertNotNull(message1FromQueue.getVisibleDate());
        
        /** Pull one more message, there is no new message can be pulled */
        Message message2FromQueue = queue.pull(3);
        Assert.assertNull(message2FromQueue);

        /** Mock the message invisible time is timeout */
        message1FromQueue.setVisibleDate(new Date(System.currentTimeMillis() - 1));

        /** Mock timer triggers the run method */
        queue.resetInvisibleMessageWhichIsTimeoutInQueue();

        /** message1FromQueue should be visible again */
        Assert.assertNull(message1FromQueue.getVisibleDate());

        /** Pull a new message, the new message is the message just pulled out */
        Assert.assertEquals(message1FromQueue, queue.pull(2));

    }

    @Test
    public void testMessagePulledAndDeleted() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello");
        queue.push(message1);

        Message message2 = new Message("world");
        queue.push(message2);

        Message message3 = new Message("java");
        queue.push(message3);

        /** Messages are visible in queue */
        Assert.assertNull(message1.getVisibleDate());
        Assert.assertNull(message2.getVisibleDate());
        Assert.assertNull(message3.getVisibleDate());
        Assert.assertEquals(3, queue.size());

        /** Pull Three messages */
        Message message1FromQueue = queue.pull(2);
        Assert.assertNotNull(message1FromQueue.getVisibleDate());
        Message message2FromQueue = queue.pull(3);
        Assert.assertNotNull(message2FromQueue.getVisibleDate());
        Message message3FromQueue = queue.pull(3);
        Assert.assertNotNull(message3FromQueue.getVisibleDate());

        Assert.assertTrue(queue.delete(message2FromQueue.getReceiptHandle()));

        /** Mock the message invisible time is timeout */
        message1FromQueue.setVisibleDate(new Date(System.currentTimeMillis() - 1));
        message3FromQueue.setVisibleDate(new Date(System.currentTimeMillis() - 1));

        /** Mock timer triggers the run method */
        queue.resetInvisibleMessageWhichIsTimeoutInQueue();

        Message message4FromQueue = queue.pull(2);
        Message message5FromQueue = queue.pull(3);

        Assert.assertEquals(message1FromQueue, message4FromQueue);
        Assert.assertEquals(message3FromQueue, message5FromQueue);

        Assert.assertEquals(2, queue.size());

        queue.delete(message4FromQueue.getReceiptHandle());
        queue.delete(message5FromQueue.getReceiptHandle());

        Assert.assertEquals(0, queue.size());
    }
}
