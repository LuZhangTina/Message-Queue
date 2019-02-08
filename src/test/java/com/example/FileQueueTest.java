package com.example;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by tina on 2019/2/6.
 */
public class FileQueueTest {
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
    public void testInFileQueueLockFilePath() {
        FileQueue fileQueue = new FileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/.lock/", fileQueue.getLockFilePath());
    }

    @Test
    public void testInFileQueueMessageFilePath() {
        FileQueue fileQueue = new FileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/message", fileQueue.getMessageFilePath());
    }

    @Test
    public void testInFileQueueBackupMessageFilePath() {
        FileQueue fileQueue = new FileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/backupMessage", fileQueue.getBackupMessageFilePath());
    }

    @Test
    public void testInFileQueuePush() {
        Message message1 = new Message("hello");
        Message message2 = new Message("world");
        FileQueue fileQueue = new FileQueue("myQueue1");
        Assert.assertTrue(fileQueue.push(message1));
        Assert.assertTrue(fileQueue.push(message2));
    }

    @Test
    public void testInFileQueuePull() {
        FileQueue fileQueue = new FileQueue("myQueue1");
        Message message1 = new Message("hel$lo$");
        Message message2 = new Message("world");
        Assert.assertTrue(fileQueue.push(message1));
        Assert.assertTrue(fileQueue.push(message2));

        Message message1FromQueue = fileQueue.pull(2);
        Message message2FromQueue = fileQueue.pull(3);
        Message message3FromQueue = fileQueue.pull(3);
        Assert.assertEquals("hel$lo$", message1FromQueue.getData());
        Assert.assertEquals("world", message2FromQueue.getData());
        Assert.assertNull(message3FromQueue);
    }

    @Test
    public void testInFileQueueDelete() {
        FileQueue fileQueue = new FileQueue("myQueue1");
        Message message1 = new Message("hel$lo$");
        Message message2 = new Message("world");
        Assert.assertTrue(fileQueue.push(message1));
        Assert.assertTrue(fileQueue.push(message2));

        Message message1FromQueue = fileQueue.pull(2);
        Message message2FromQueue = fileQueue.pull(3);
        Assert.assertEquals("hel$lo$", message1FromQueue.getData());
        Assert.assertEquals("world", message2FromQueue.getData());

        Message message3 = new Message("java");
        Assert.assertTrue(fileQueue.push(message3));

        Assert.assertTrue(fileQueue.delete(message2FromQueue.getReceiptHandle()));
    }

    @Test
    public void testInFileQueueVisibleTimeout() {
        FileQueue fileQueue = new FileQueue("myQueue1");
        Message message1 = new Message("hel$lo$");
        Message message2 = new Message("world");
        Assert.assertTrue(fileQueue.push(message1));
        Assert.assertTrue(fileQueue.push(message2));

        Message message1FromQueue = fileQueue.pull(0);
        Message message2FromQueue = fileQueue.pull(0);
        Assert.assertEquals("hel$lo$", message1FromQueue.getData());
        Assert.assertEquals("world", message2FromQueue.getData());

        /** Mock timer triggers message update */
        fileQueue.updateMessageIntoVisibleState();

        Message message3FromQueue = fileQueue.pull(0);
        Message message4FromQueue = fileQueue.pull(0);
        Assert.assertEquals("hel$lo$", message3FromQueue.getData());
        Assert.assertEquals("world", message4FromQueue.getData());
    }
}
