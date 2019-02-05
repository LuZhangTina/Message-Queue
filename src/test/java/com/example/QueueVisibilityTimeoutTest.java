package com.example;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by tina on 2019/2/4.
 */
public class QueueVisibilityTimeoutTest {
    @Test
    public void testGetMaxVisibilityTimeout() {
        Assert.assertEquals(43200, QueueVisibilityTimeout.getMaxVisibilityTimeout());
    }

    @Test
    public void testGetMinVisibilityTimeout() {
        Assert.assertEquals(0, QueueVisibilityTimeout.getMinVisibilityTimeout());
    }

    @Test
    public void testGetDefaultVisibilityTimeout() {
        Assert.assertEquals(30, QueueVisibilityTimeout.getDefaultVisibilityTimeout());
    }

    @Test
    public void testCreateVisibleDate() {
        Date date = QueueVisibilityTimeout.createVisibleDate(30);
        Assert.assertNotNull(date);
    }
}
