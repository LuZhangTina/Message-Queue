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
        InMemoryQueue queue = new InMemoryQueue();
        queue.push(message);
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void testPushTwoMessage() {
        Message message1 = new Message("hello", 2);
        Message message2 = new Message("world", 3);
        InMemoryQueue queue = new InMemoryQueue();
        queue.push(message1);
        queue.push(message2);
        Assert.assertEquals(2, queue.size());
    }

    @Test
    public void testPullOnceWhenQueueIsEmpty() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message = queue.pull();
        Assert.assertNull(message);
    }

    @Test
    public void testPullOnceWhenQueueHasOneMessage() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message = new Message("hello", 2);
        queue.push(message);
        Message messageAtTop = queue.pull();
        Assert.assertEquals(message, messageAtTop);
    }

    @Test
    public void testPullTwiceWhenQueueHasOneMessage() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message = new Message("hello", 2);
        queue.push(message);
        Message messageFromQueue1 = queue.pull();
        Assert.assertEquals(message, messageFromQueue1);
        Message messageFromQueue2 = queue.pull();
        Assert.assertNull(messageFromQueue2);
    }

    @Test
    public void testPullTwiceWhenQueueHasTwoMessages() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        queue.push(message1);

        Message message2 = new Message("world", 3);
        queue.push(message2);

        Message messageFromQueue1 = queue.pull();
        Assert.assertEquals(message1, messageFromQueue1);
        Message messageFromQueue2 = queue.pull();
        Assert.assertEquals(message2, messageFromQueue2);
    }

    @Test
    public void testDeleteMessageWhenQueueIsEmpty() {
        InMemoryQueue queue = new InMemoryQueue();
        Assert.assertFalse(queue.delete("123456"));
    }

    @Test
    public void testDeleteMessageWhenMessageIsNotInQueue() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        queue.push(message1);
        Message messageFromQueue = queue.pull();
        String messageId = messageFromQueue.getMessageId();
        messageId = messageId + "1";
        Assert.assertFalse(queue.delete(messageId));
    }

    @Test
    public void testDeleteMessageWhenMessageNotBePulledFirst() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        queue.push(message1);
        String messageId1 = message1.getMessageId();
        Assert.assertFalse(queue.delete(messageId1));
    }

    @Test
    public void testDeleteMessageWhenMessageIsInvisible() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        queue.push(message1);
        Message message2 = new Message("world", 3);
        queue.push(message2);

        String messageId1 = message1.getMessageId();
        String messageId2 = message2.getMessageId();
        Assert.assertFalse(queue.delete(messageId1));

        /** Pull two messages out from queue */
        queue.pull();
        queue.pull();

        Assert.assertTrue(queue.delete(messageId1));
        Assert.assertFalse(queue.delete(messageId1));

        Assert.assertTrue(queue.delete(messageId2));
        Assert.assertFalse(queue.delete(messageId2));

        /** The queue is empty after two messages are deleted */
        Assert.assertEquals(0, queue.size());
    }

    @Test
    public void testMessagePulledButNotDeleted() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        queue.push(message1);
        Assert.assertNull(message1.getVisibleDate());

        /** Pull the message */
        Message messageFromQueue1 = queue.pull();
        Assert.assertNotNull(messageFromQueue1.getVisibleDate());
        try {
            /** Wait for 1 second, the pulled out message is invisible */
            Thread.sleep(1000);
            Assert.assertNotNull(messageFromQueue1.getVisibleDate());

            /** Pull one more message, there is no new message can be pulled */
            Message messageFromQueue2 = queue.pull();
            Assert.assertNull(messageFromQueue2);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        try {
            /** Wait for 2 second, the pulled out message is visible again */
            Thread.sleep(2000);
            Assert.assertNull(messageFromQueue1.getVisibleDate());

            /** Pull a new message, the new message is the message just pulled out */
            Assert.assertEquals(messageFromQueue1, queue.pull());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testMessagePulledAndDeleted() {
        InMemoryQueue queue = new InMemoryQueue();
        Message message1 = new Message("hello", 2);
        queue.push(message1);

        Message message2 = new Message("world", 3);
        queue.push(message2);

        Message message3 = new Message("java", 3);
        queue.push(message3);

        /** Messages are visible in queue */
        Assert.assertNull(message1.getVisibleDate());
        Assert.assertNull(message2.getVisibleDate());
        Assert.assertNull(message3.getVisibleDate());
        Assert.assertEquals(3, queue.size());

        /** Pull Three messages */
        Message message1FromQueue = queue.pull();
        Assert.assertNotNull(message1FromQueue.getVisibleDate());
        Message message2FromQueue = queue.pull();
        Assert.assertNotNull(message2FromQueue.getVisibleDate());
        Message message3FromQueue = queue.pull();
        Assert.assertNotNull(message3FromQueue.getVisibleDate());
        try {
            /** Wait for 1 second, delete the second message which was pulled out */
            Thread.sleep(1000);
            Assert.assertTrue(queue.delete(message2FromQueue.getMessageId()));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        try {
            /** Wait for 3 second, pull two messages again */
            Thread.sleep(3000);
            Message message4FromQueue = queue.pull();
            Message message5FromQueue = queue.pull();

            Assert.assertEquals(message1FromQueue, message4FromQueue);
            Assert.assertEquals(message3FromQueue, message5FromQueue);

            Assert.assertEquals(2, queue.size());

            queue.delete(message4FromQueue.getMessageId());
            queue.delete(message5FromQueue.getMessageId());

            Assert.assertEquals(0, queue.size());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
