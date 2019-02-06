package com.example;

import org.junit.Assert;
import org.junit.Test;

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
}
