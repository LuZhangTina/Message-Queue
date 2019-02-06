package com.example;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by tina on 2019/2/6.
 */
public class InFileQueueTest {
    @Test
    public void testInFileQueueLockFilePath() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/.lock/", inFileQueue.getLockFilePath());
    }

    @Test
    public void testInFileQueueMessageFilePath() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/message", inFileQueue.getMessageFilePath());
    }

    @Test
    public void testInFileQueuePush() {
        Message message1 = new Message("hello", 2);
        Message message2 = new Message("world", 3);
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Assert.assertTrue(inFileQueue.push(message1));
        Assert.assertTrue(inFileQueue.push(message2));
    }
}
