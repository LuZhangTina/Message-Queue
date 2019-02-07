package com.example;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by tina on 2019/2/6.
 */
public class InFileQueue {
    private final String fileRootPath = "sqs/";
    private String queueName;
    private Timer timer;
    private TimerTask timerTask;

    public InFileQueue(String queueName) {
        this.queueName = queueName;
        this.timer = new Timer();
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                updateMessageIntoVisibleState();
            }
        };
    }

    public String getQueueName() {
        return this.queueName;
    }

    public String getFileRootPath() {
        return this.fileRootPath;
    }

    public Timer getTimer() {
        return this.timer;
    }

    public TimerTask getTimerTask() {
        return this.timerTask;
    }

    public File getLockFile() {
        String lockFilePath = getLockFilePath();
        return new File(lockFilePath);
    }

    public File getMessageFile() {
        String messageFilePath = getMessageFilePath();
        return new File(messageFilePath);
    }

    public File getBackupMessageFile() {
        String backupMessageFilePath = getBackupMessageFilePath();
        return new File(backupMessageFilePath);
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

    public String getBackupMessageFilePath() {
        String queueName = getQueueName();
        String filePath = getFileRootPath();
        String lockFilePath = filePath + queueName + "/backupMessage";
        return lockFilePath;
    }

    public boolean push(Message message) {
        File lockFile = getLockFile();
        File messageFile = getMessageFile();

        lockQueue(lockFile);

        /** If message file doesn't exist, start timer and create message file*/
        if (!messageFile.exists()) {
            Timer timer = getTimer();
            TimerTask timerTask = getTimerTask();
            timer.schedule(timerTask, 0, 1000);

            try {
                messageFile.createNewFile();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        writeMessageIntoMessageFile(messageFile, message);

        unlockQueue(lockFile);

        return true;
    }

    public Message pull() {
        File lockFile = getLockFile();
        File messageFile = getMessageFile();
        File backupMessageFile = getBackupMessageFile();

        lockQueue(lockFile);

        if (!messageFile.exists()) {
            unlockQueue(lockFile);
            return null;
        }

        Message message = getFirstVisibleMessageFromMessageFile(messageFile, backupMessageFile);
        backupMessageFile.renameTo(messageFile);
        unlockQueue(lockFile);

        return message;
    }

    public boolean delete(String messageId) {
        if (messageId == null || messageId.length() == 0) {
            return false;
        }

        File lockFile = getLockFile();
        File messageFile = getMessageFile();
        File backupMessageFile = getBackupMessageFile();

        lockQueue(lockFile);

        if (!messageFile.exists()) {
            unlockQueue(lockFile);
            return false;
        }

        boolean result = deleteMessageByMessageId(messageFile, backupMessageFile, messageId);
        backupMessageFile.renameTo(messageFile);

        /** If message file is empty, then stop the timer and delete the message file */
        if (messageFile.length() == 0) {
            Timer timer = getTimer();
            timer.cancel();
            messageFile.delete();
        }

        unlockQueue(lockFile);

        return result;
    }

    public void updateMessageIntoVisibleState() {
        File lockFile = getLockFile();
        File messageFile = getMessageFile();
        File backupMessageFile = getBackupMessageFile();

        lockQueue(lockFile);

        if (!messageFile.exists()) {
            unlockQueue(lockFile);
            return;
        }

        try {
            /** Create a read buffer from message file */
            FileInputStream fileInputStream = new FileInputStream(messageFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            /** Create a write buffer from backupMessage file */
            FileOutputStream fileOutputStream = new FileOutputStream(backupMessageFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            String line;
            String[] messageArray;
            while ((line = bufferedReader.readLine()) != null) {
                messageArray = line.split("\\$");

                /** Check if the message string is illegal */
                if (messageArray.length < 4) {
                    continue;
                }

                /** If the message's visible date is not 0,
                 *  means that message is invisible might need to set to visible */
                long visibleDate = Long.parseLong(messageArray[1]);
                if (visibleDate != 0) {
                    /** If the message's visibleDate is before the current date,
                     *  then the message need to be set to visible again */
                    if (visibleDate < System.currentTimeMillis()) {
                        Message message = createMessageByMessageString(line);
                        message.setVisibleDate(null);
                        line = createMessageString(message);
                        bufferedWriter.write(line);
                    } else {
                        bufferedWriter.write(line);
                        bufferedWriter.write(System.lineSeparator());
                    }
                } else {
                    bufferedWriter.write(line);
                    bufferedWriter.write(System.lineSeparator());
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();

            bufferedWriter.close();
            outputStreamWriter.close();
            fileOutputStream.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        backupMessageFile.renameTo(messageFile);

        unlockQueue(lockFile);
    }

    private Message getFirstVisibleMessageFromMessageFile(File messageFile, File backupMessageFile) {
        Message message = null;
        try {
            /** Create a read buffer from message file */
            FileInputStream fileInputStream = new FileInputStream(messageFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            /** Create a write buffer from backupMessage file */
            FileOutputStream fileOutputStream = new FileOutputStream(backupMessageFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            String line;
            String[] messageArray;
            while ((line = bufferedReader.readLine()) != null) {
                messageArray = line.split("\\$");

                /** Check if the message string is illegal */
                if (messageArray.length < 4) {
                    continue;
                }

                /** If the message's visible date is 0,
                 *  means that message is visible and can be pulled */
                long visibleDate = Long.parseLong(messageArray[1]);
                if (visibleDate == 0 && message == null) {
                    message = createMessageByMessageString(line);
                    line = createMessageString(message);
                    bufferedWriter.write(line);
                } else {
                    bufferedWriter.write(line);
                    bufferedWriter.write(System.lineSeparator());
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();

            bufferedWriter.close();
            outputStreamWriter.close();
            fileOutputStream.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return message;
    }

    private boolean deleteMessageByMessageId(File messageFile, File backupMessageFile, String msgIdTobeDeleted) {
        boolean result = false;
        try {
            /** Create a read buffer from message file */
            FileInputStream fileInputStream = new FileInputStream(messageFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            /** Create a write buffer from backupMessage file */
            FileOutputStream fileOutputStream = new FileOutputStream(backupMessageFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            String line;
            String[] messageArray;
            while ((line = bufferedReader.readLine()) != null) {
                messageArray = line.split("\\$");

                /** Check if the message string is illegal */
                if (messageArray.length < 4) {
                    continue;
                }

                /** Get messageId */
                String msgIdFromQueue = messageArray[0];
                if (msgIdFromQueue.equals(msgIdTobeDeleted)) {
                    /** If the message's visible date is not 0,
                     *  means that message is invisible and can be deleted */
                    long visibleDate = Long.parseLong(messageArray[1]);
                    if (visibleDate != 0) {
                        result = true;
                        continue;
                    } else {
                        bufferedWriter.write(line);
                        bufferedWriter.write(System.lineSeparator());
                    }
                } else {
                    bufferedWriter.write(line);
                    bufferedWriter.write(System.lineSeparator());
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();

            bufferedWriter.close();
            outputStreamWriter.close();
            fileOutputStream.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    private Message createMessageByMessageString(String messageString) {
        String[] messageArray = messageString.split("\\$");

        int visibleTimeout = Integer.parseInt(messageArray[2]);

        int msgContentStartIdx = searchMessageContentStartIdx(messageString);
        String messageContent = messageString.substring(msgContentStartIdx);
        Message message = new Message(messageContent, visibleTimeout);

        /** Keep the messageId as old messageId */
        String messageId = messageArray[0];
        message.setMessageId(messageId);

        /** Set message visible date */
        message.setVisibleDate(QueueProperties.createVisibleDate(visibleTimeout));

        return message;
    }

    private int searchMessageContentStartIdx(String messageString) {
        int msgIdEndIdx = messageString.indexOf('$');
        int visibleDateEndIdx = messageString.indexOf('$', msgIdEndIdx + 1);
        int visibleTimeoutIdx = messageString.indexOf('$', visibleDateEndIdx + 1);
        return visibleTimeoutIdx + 1;
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
            System.out.println(e.getMessage());
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

        if (message.getVisibleDate() == null) {
            /** Message is in visible state */
            msgStr = msgStr + 0;
        } else {
            /** Message is in invisible state */
            msgStr = msgStr + message.getVisibleDate().getTime();
        }

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
