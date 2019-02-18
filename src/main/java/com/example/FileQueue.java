package com.example;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by tina on 2019/2/6.
 */
public class FileQueue {
    private final String fileRootPath = "sqs/";
    private String queueName;
    private Timer timer;
    private TimerTask timerTask;

    public FileQueue(String queueName) {
        this.queueName = queueName;
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

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public void setTimerTask(TimerTask timerTask) {
        this.timerTask = timerTask;
    }

    public boolean push(Message message) {
        File lockFile = getLockFile();
        File messageFile = getMessageFile();

        lockQueue(lockFile);

        /** If message file doesn't exist, start timer and create message file*/
        if (!messageFile.exists()) {
            startTimer();

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

    public Message pull(int visibilityTimeout) {
        File lockFile = getLockFile();
        File messageFile = getMessageFile();
        File backupMessageFile = getBackupMessageFile();

        lockQueue(lockFile);

        if (!messageFile.exists()) {
            unlockQueue(lockFile);
            return null;
        }

        Message message = getFirstVisibleMessageFromMessageFile(messageFile, backupMessageFile, visibilityTimeout);
        backupMessageFile.renameTo(messageFile);
        unlockQueue(lockFile);

        return message;
    }

    public boolean delete(String receiptHandle) {
        if (receiptHandle == null || receiptHandle.length() == 0) {
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

        boolean result = deleteMessageByReceiptHandle(messageFile, backupMessageFile, receiptHandle);
        backupMessageFile.renameTo(messageFile);

        /** If message file is empty, then stop the timer and delete the message file */
        if (messageFile.length() == 0) {
            stopTimer();
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
            updateMsgVisibleStateIntoBackupFIle(messageFile, backupMessageFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        backupMessageFile.renameTo(messageFile);

        unlockQueue(lockFile);
    }

    private void updateMsgVisibleStateIntoBackupFIle(File messageFile, File backupMessageFile) throws IOException {
        /** Create a read buffer from message file */
        FileInputStream fileInputStream = new FileInputStream(messageFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        /** Create a write buffer from backupMessage file */
        FileOutputStream fileOutputStream = new FileOutputStream(backupMessageFile, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        setMsgVisibleAndWriteIntoBackupFile(bufferedReader, bufferedWriter);

        closeInputStream(fileInputStream, inputStreamReader, bufferedReader);

        closeOutputStream(fileOutputStream, outputStreamWriter, bufferedWriter);
    }

    private void closeInputStream(FileInputStream fileInputStream, InputStreamReader inputStreamReader, BufferedReader bufferedReader) throws IOException {
        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
    }

    private void setMsgVisibleAndWriteIntoBackupFile(BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
        String line;
        String[] messageArray;
        while ((line = bufferedReader.readLine()) != null) {
            messageArray = line.split("\\$");

            /** Check if the message string is illegal */
            if (messageArray.length < 3) {
                continue;
            }

            /** If the message's visible date is not 0,
             *  means that message is invisible might need to set to visible */
            long visibleDate = Long.parseLong(messageArray[1]);
            if (visibleDate != 0) {
                /** If the message's visibleDate is before the current date,
                 *  then the message need to be set to visible again */
                if (visibleDate < System.currentTimeMillis()) {
                    Message message = createMessageByMessageString(line, 0);
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
    }

    private Message getFirstVisibleMessageFromMessageFile(File messageFile, File backupMessageFile, int visibilityTimeout) {
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

            message = getMessageAndUpdateBackupFile(visibilityTimeout, bufferedReader, bufferedWriter);

            closeInputStream(fileInputStream, inputStreamReader, bufferedReader);

            closeOutputStream(fileOutputStream, outputStreamWriter, bufferedWriter);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return message;
    }

    private Message getMessageAndUpdateBackupFile(int visibilityTimeout, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
        Message message = null;
        String line;
        String[] messageArray;
        while ((line = bufferedReader.readLine()) != null) {
            messageArray = line.split("\\$");

            /** Check if the message string is illegal */
            if (messageArray.length < 3) {
                continue;
            }

            /** If the message's visible date is 0,
             *  means that message is visible and can be pulled */
            long visibleDate = Long.parseLong(messageArray[1]);
            if (visibleDate == 0 && message == null) {
                message = createMessageByMessageString(line, visibilityTimeout);
                line = createMessageString(message);
                bufferedWriter.write(line);
            } else {
                bufferedWriter.write(line);
                bufferedWriter.write(System.lineSeparator());
            }
        }
        return message;
    }

    private boolean deleteMessageByReceiptHandle(File messageFile, File backupMessageFile, String msgReceiptHandleTobeDeleted) {
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

            result = deleteMessageAndUpdateBackupFile(msgReceiptHandleTobeDeleted, bufferedReader, bufferedWriter);

            closeInputStream(fileInputStream, inputStreamReader, bufferedReader);

            closeOutputStream(fileOutputStream, outputStreamWriter, bufferedWriter);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    private boolean deleteMessageAndUpdateBackupFile(String msgReceiptHandleTobeDeleted, BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
        boolean result = false;
        String line;
        String[] messageArray;
        while ((line = bufferedReader.readLine()) != null) {
            messageArray = line.split("\\$");

            /** Check if the message string is illegal */
            if (messageArray.length < 3) {
                continue;
            }

            /** Get receiptHandle */
            String msgReceiptHandleFromQueue = messageArray[0];
            if (msgReceiptHandleFromQueue.equals(msgReceiptHandleTobeDeleted)) {
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
        return result;
    }

    private void closeOutputStream(FileOutputStream fileOutputStream, OutputStreamWriter outputStreamWriter, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.close();
        outputStreamWriter.close();
        fileOutputStream.close();
    }

    private Message createMessageByMessageString(String messageString, int visibilityTimeout) {
        String[] messageArray = messageString.split("\\$");

        int msgContentStartIdx = searchMessageContentStartIdx(messageString);
        String messageContent = messageString.substring(msgContentStartIdx);
        Message message = new Message(messageContent);

        /** Keep the msgReceiptHandle as old msgReceiptHandle */
        String msgReceiptHandle = messageArray[0];
        message.setReceiptHandle(msgReceiptHandle);

        /** Set message visible date */
        message.setVisibleDate(QueueProperties.createVisibleDate(visibilityTimeout));

        return message;
    }

    private int searchMessageContentStartIdx(String messageString) {
        int msgReceiptHandleEndIdx = messageString.indexOf('$');
        int visibleDateEndIdx = messageString.indexOf('$', msgReceiptHandleEndIdx + 1);
        return visibleDateEndIdx + 1;
    }

    private void writeMessageIntoMessageFile(File messageFile, Message message) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(messageFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            /** Write message into file */
            bufferedWriter.write(createMessageString(message));

            closeOutputStream(fileOutputStream, outputStreamWriter, bufferedWriter);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Message string structure is as following:
     *  "receiptHandle$visibleDate$messageContent"
     *  e.g.
     *  "123456789012$0$3$hello"
     *  In this case: receiptHandle = "123456789012"
     *                visibleDate = always visible
     *                messageContent = "hello"
     *
     *  "123456789012$1549423359452$world"
     *  In this case: receiptHandle = "123456789012"
     *                visibleDate = the 1549423359452 milliseconds since January 1, 1970, 00:00:00 GMT
     *                messageContent = "world"
     */
    private String createMessageString(Message message) {
        String msgStr = message.getReceiptHandle();
        msgStr = msgStr + "$";

        if (message.getVisibleDate() == null) {
            /** Message is in visible state */
            msgStr = msgStr + 0;
        } else {
            /** Message is in invisible state */
            msgStr = msgStr + message.getVisibleDate().getTime();
        }

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

    private void startTimer() {
        Timer timer = getTimer();
        if (timer == null) {
            timer = new Timer();
            setTimer(timer);
        }

        TimerTask timerTask = getTimerTask();
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateMessageIntoVisibleState();
                }
            };
            setTimerTask(timerTask);
        }

        timer.schedule(timerTask, 0, 1000);
    }

    private void stopTimer() {
        Timer timer = getTimer();
        TimerTask timerTask = getTimerTask();
        if (timerTask != null) {
            timerTask.cancel();
            setTimerTask(null);
        }

        if (timer != null) {
            timer.cancel();
            setTimer(null);
        }
    }
}
