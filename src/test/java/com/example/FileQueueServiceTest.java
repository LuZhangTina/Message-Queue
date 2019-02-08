package com.example;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by tina on 2019/2/7.
 */
public class FileQueueServiceTest {
    private FileQueueService fileQueueService;
    @Before
    public void runPreCondition() {
        this.fileQueueService = new FileQueueService();
    }

    @After
    public void removeFilesUnderFileRootPath() {
        FileQueue fileQueue = new FileQueue("myQueue1");
        String fileRootPath = fileQueue.getFileRootPath();
        File rootFolder = new File(fileRootPath);
        deleteFiles(rootFolder);
    }

    public void deleteFiles(File rootFolder) {
        File[] files = rootFolder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            } else {
                deleteFiles(file);
            }
        }
    }

    @Test
    public void testPushMessageWhenUrlIsNull() {
        boolean result = fileQueueService.push(null, null, "hello");
        Assert.assertFalse(result);
    }

    @Test
    public void testPushMessageWhenUrlIsIllegal() {
        boolean result = fileQueueService.push("queueUrlIllegal", null, "hello");
        Assert.assertFalse(result);
    }

    @Test
    public void testPushMessageWhenMessageIsNull() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", null, null);
        Assert.assertTrue(result);
    }

    @Test
    public void testPushThreeMessages() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
    }

    @Test
    public void testPushMessagesToThreeQueues() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);

        result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue2", "apple", "banana", "orange");
        Assert.assertTrue(result);

        result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue3", "car", "bus", "train");
        Assert.assertTrue(result);
    }

    @Test
    public void testPullWhenUrlIsNull() {
        Assert.assertNull(fileQueueService.pull(null));
    }

    @Test
    public void testPullWhenUrlIsIllegal() {
        Assert.assertNull(fileQueueService.pull("queueUrlIsIllegal"));
    }

    @Test
    public void testPullWhenQueueDoesNotExist() {
        Assert.assertNull(fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1"));
    }

    @Test
    public void testPullWhenQueueIsEmpty() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", null);
        Assert.assertTrue(result);
        Assert.assertNull(fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1"));
    }

    @Test
    public void testPullWhenQueueIsNotEmpty() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
        Message message1FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("hello", message1FromQueue.getData());
        Message message2FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("world", message2FromQueue.getData());
        Message message3FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("java", message3FromQueue.getData());
        Message message4FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertNull(message4FromQueue);
    }

    @Test
    public void testDeleteWhenUrlIsNull() {
        Assert.assertFalse(fileQueueService.delete(null, "123"));
    }

    @Test
    public void testDeleteWhenUrlIsNotIllegal() {
        Assert.assertFalse(fileQueueService.delete("queueUrlIsIllegal", "123"));
    }

    @Test
    public void testDeleteWhenQueueIsNotExist() {
        Assert.assertFalse(fileQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "123"));
    }

    @Test
    public void testDeleteWhenMessageIdIsNull() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
        Assert.assertFalse(fileQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", null));
    }

    @Test
    public void testDeleteWhenMessageIdIsNotExist() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);
        Assert.assertFalse(fileQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "123"));
    }

    @Test
    public void testDeleteMessageInVisibleTimeout() {
        boolean result = fileQueueService.push("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", "hello", "world", "java");
        Assert.assertTrue(result);

        Message message1FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("hello", message1FromQueue.getData());

        Assert.assertTrue(fileQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", message1FromQueue.getReceiptHandle()));

        Message message2FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 0);
        Assert.assertEquals("world", message2FromQueue.getData());

        Message message3FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", 0);
        Assert.assertEquals("java", message3FromQueue.getData());

        Assert.assertTrue(fileQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", message3FromQueue.getReceiptHandle()));

        /** Mock timer triggers the run method */
        FileQueue queue = fileQueueService.getInFileQueue("MyQueue1");
        queue.updateMessageIntoVisibleState();

        Assert.assertFalse(fileQueueService.delete("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1", message2FromQueue.getReceiptHandle()));

        Message message4FromQueue = fileQueueService.pull("https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue1");
        Assert.assertEquals("world", message4FromQueue.getData());
    }
}
