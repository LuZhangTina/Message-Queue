package com.example;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Created by tina on 2019/2/4.
 */
public class QueuePropertiesTest {
    @Test
    public void testGetMaxVisibilityTimeout() {
        Assert.assertEquals(43200, QueueProperties.getMaxVisibilityTimeout());
    }

    @Test
    public void testGetMinVisibilityTimeout() {
        Assert.assertEquals(0, QueueProperties.getMinVisibilityTimeout());
    }

    @Test
    public void testGetDefaultVisibilityTimeout() {
        Assert.assertEquals(30, QueueProperties.getDefaultVisibilityTimeout());
    }

    @Test
    public void testCreateVisibleDate() {
        Date date = QueueProperties.createVisibleDate(30);
        Assert.assertNotNull(date);
    }
}
