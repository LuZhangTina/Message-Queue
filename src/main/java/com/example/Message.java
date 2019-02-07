package com.example;

import java.util.Date;
import java.util.UUID;

/**
 * Created by tina on 2019/2/4.
 */
public class Message {
    /** the message's visibleTimeout in second */
    private int visibleTimeout;

    /** the message's content */
    private String data;

    private String messageId;

    private Date visibleDate;

    Message(String data) {
        this.visibleTimeout = QueueProperties.getDefaultVisibilityTimeout();
        this.data = data;
        this.messageId = UUID.randomUUID().toString();
        this.visibleDate = null;
    }

    Message(String data, int visibleTimeout) {
        this.visibleTimeout = visibleTimeout;
        this.data = data;
        this.messageId = UUID.randomUUID().toString();
        this.visibleDate = null;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public int getVisibleTimeout() {
        return this.visibleTimeout;
    }

    public String getData() {
        return this.data;
    }

    public Date getVisibleDate() {
        return this.visibleDate;
    }

    public void setVisibleDate(Date date) {
        this.visibleDate = date;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        String str = "MessageId: " + getMessageId();
        str = str + System.lineSeparator();
        str = str + "Message visibility timeout: " + getVisibleTimeout();
        str = str + System.lineSeparator();
        str = str + "Message content: " + getData();
        str = str + System.lineSeparator();

        if (getVisibleDate() != null) {
            str = str + getVisibleDate().toString();
            str = str + System.lineSeparator();
        }

        return str;
    }
}
