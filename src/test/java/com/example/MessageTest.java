package com.example;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by tina on 2019/2/4.
 */
public class MessageTest {
    @Test
    public void testGetMessageId() {
        Message msg = new Message("hello", 2);
        String str = msg.getMessageId();
        Assert.assertNotNull(str);
    }

    @Test
    public void testGetMessageData() {
        Message msg = new Message("hello", 2);
        String str = msg.getData();
        Assert.assertEquals("hello", str);
    }

    @Test
    public void testGetMessageVariableVisibilityTime() {
        Message msg = new Message("hello", 2);
        int visibleTimeout = msg.getVisibleTimeout();
        Assert.assertEquals(2, visibleTimeout);
    }

    @Test
    public void testGetMessageDefaultVisibilityTime() {
        Message msg = new Message("hello");
        int visibleTimeout = msg.getVisibleTimeout();
        Assert.assertEquals(30, visibleTimeout);
    }

    @Test
    public void testVisibleTimeoutSetAndGet() {
        Date date = new Date();
        Message msg = new Message("hello");
        msg.setVisibleDate(date);
        Assert.assertEquals(date, msg.getVisibleDate());
    }
}
