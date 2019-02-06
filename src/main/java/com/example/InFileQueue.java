package com.example;

import java.io.File;

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
}
