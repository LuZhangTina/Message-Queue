package com.example;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by tina on 2019/2/6.
 */
public class InFileQueueTest {
    @After
    public void removeFilesUnderFileRootPath() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        String fileRootPath = inFileQueue.getFileRootPath();
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
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/.lock/", inFileQueue.getLockFilePath());
    }

    @Test
    public void testInFileQueueMessageFilePath() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/message", inFileQueue.getMessageFilePath());
    }

    @Test
    public void testInFileQueueBackupMessageFilePath() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Assert.assertEquals("sqs/myQueue1/backupMessage", inFileQueue.getBackupMessageFilePath());
    }

    @Test
    public void testInFileQueuePush() {
        Message message1 = new Message("hello", 2);
        Message message2 = new Message("world", 3);
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Assert.assertTrue(inFileQueue.push(message1));
        Assert.assertTrue(inFileQueue.push(message2));
    }

    @Test
    public void testInFileQueuePull() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Message message1 = new Message("hel$lo$", 2);
        Message message2 = new Message("world", 3);
        Assert.assertTrue(inFileQueue.push(message1));
        Assert.assertTrue(inFileQueue.push(message2));

        Message message1FromQueue = inFileQueue.pull();
        Message message2FromQueue = inFileQueue.pull();
        Message message3FromQueue = inFileQueue.pull();
        Assert.assertEquals("hel$lo$", message1FromQueue.getData());
        Assert.assertEquals("world", message2FromQueue.getData());
        Assert.assertNull(message3FromQueue);
    }

    @Test
    public void testInFileQueueDelete() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Message message1 = new Message("hel$lo$", 2);
        Message message2 = new Message("world", 3);
        Assert.assertTrue(inFileQueue.push(message1));
        Assert.assertTrue(inFileQueue.push(message2));

        Message message1FromQueue = inFileQueue.pull();
        Message message2FromQueue = inFileQueue.pull();
        Assert.assertEquals("hel$lo$", message1FromQueue.getData());
        Assert.assertEquals("world", message2FromQueue.getData());

        Message message3 = new Message("java", 3);
        Assert.assertTrue(inFileQueue.push(message3));

        Assert.assertTrue(inFileQueue.delete(message2FromQueue.getMessageId()));
    }

    @Test
    public void testInFileQueueVisibleTimeout() {
        InFileQueue inFileQueue = new InFileQueue("myQueue1");
        Message message1 = new Message("hel$lo$", 0);
        Message message2 = new Message("world", 0);
        Assert.assertTrue(inFileQueue.push(message1));
        Assert.assertTrue(inFileQueue.push(message2));

        Message message1FromQueue = inFileQueue.pull();
        Message message2FromQueue = inFileQueue.pull();
        Assert.assertEquals("hel$lo$", message1FromQueue.getData());
        Assert.assertEquals("world", message2FromQueue.getData());

        /** Mock timer triggers message update */
        inFileQueue.updateMessageIntoVisibleState();

        Message message3FromQueue = inFileQueue.pull();
        Message message4FromQueue = inFileQueue.pull();
        Assert.assertEquals("hel$lo$", message3FromQueue.getData());
        Assert.assertEquals("world", message4FromQueue.getData());
    }
}
