package com.example;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by tina on 2019/2/4.
 */
public class MessageTest {
    @Test
    public void testGetMessageReceiptHandle() {
        Message msg = new Message("hello");
        String str = msg.getReceiptHandle();
        Assert.assertNotNull(str);
    }

    @Test
    public void testGetMessageData() {
        Message msg = new Message("hello");
        String str = msg.getData();
        Assert.assertEquals("hello", str);
    }

    @Test
    public void testVisibleTimeoutSetAndGet() {
        Date date = new Date();
        Message msg = new Message("hello");
        msg.setVisibleDate(date);
        Assert.assertEquals(date, msg.getVisibleDate());
    }
}
