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
}
