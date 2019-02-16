package com.example;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tina on 2019/2/12.
 */
public class ConfigPropertiesTest {
    @Test
    public void testReadConfig() {
        ConfigProperties configProperties = new ConfigProperties("queue.properties");
        String queueType = configProperties.readConfig();
        Assert.assertEquals("InMemoryQueue", queueType);
    }
}
