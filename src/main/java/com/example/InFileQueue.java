package com.example;

import java.io.*;
import java.util.Date;
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

    public boolean push(Message message) {
        File lockFile = getLockFile();
        File messageFile = getMessageFile();

        lockQueue(lockFile);
        writeMessageIntoMessageFile(messageFile, message);
        unlockQueue(lockFile);

        return true;
    }

    private void writeMessageIntoMessageFile(File messageFile, Message message) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(messageFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            /** Write message into file */
            bufferedWriter.write(createMessageString(message));

            bufferedWriter.close();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Message string structure is as following:
     *  "messageId$visibleDate$visibleTimeout$messageContent"
     *  e.g.
     *  "123456789012$0$3$hello"
     *  In this case: messageId = "123456789012"
     *                visibleDate = always visible
     *                visibleTimeout = 3
     *                messageContent = "hello"
     *
     *  "123456789012$1549423359452$3$world"
     *  In this case: messageId = "123456789012"
     *                visibleDate = the 1549423359452 milliseconds since January 1, 1970, 00:00:00 GMT
     *                visibleTimeout = 3
     *                messageContent = "world"
     */
    private String createMessageString(Message message) {
        String msgStr = message.getMessageId();
        msgStr = msgStr + "$";
        msgStr = msgStr + 0;
        msgStr = msgStr + "$";
        msgStr = msgStr + message.getVisibleTimeout();
        msgStr = msgStr + "$";
        msgStr = msgStr + message.getData();
        msgStr = msgStr + System.lineSeparator();

        return msgStr;
    }

    private void lockQueue(File lockFile) {
        while(!lockFile.mkdirs()) {
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
