package com.example;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by tina on 2019/2/6.
 */
public class InFileQueue {
    private final String fileRootPath = "sqs/";
    private String queueName;

    public InFileQueue(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return this.queueName;
    }

    public String getFileRootPath() {
        return this.fileRootPath;
    }

    public File getLockFile() {
        String lockFilePath = getLockFilePath();
        return new File(lockFilePath);
    }

    public File getMessageFile() {
        String messageFilePath = getMessageFilePath();
        return new File(messageFilePath);
    }

    public String getLockFilePath() {
        String queueName = getQueueName();
        String filePath = getFileRootPath();
        String lockFilePath = filePath + queueName + "/.lock/";
        return lockFilePath;
    }

    public String getMessageFilePath() {
        String queueName = getQueueName();
        String filePath = getFileRootPath();
        String lockFilePath = filePath + queueName + "/message";
        return lockFilePath;
    }

    private void lockQueue(File lockFile) {
        while(!lockFile.mkdir()) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void unlockQueue(File lockFile) {
        lockFile.delete();
    }
}
