package com.example;

import java.util.Date;
import java.util.UUID;

/**
 * Created by tina on 2019/2/4.
 */
public class Message {
    /** the message's content */
    private String data;

    private String receiptHandle;

    private Date visibleDate;

    Message(String data) {
        this.data = data;
        this.receiptHandle = UUID.randomUUID().toString();
        this.visibleDate = null;
    }

    public String getReceiptHandle() {
        return this.receiptHandle;
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

    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }

    @Override
    public String toString() {
        String str = "Message receiptHandle: " + getReceiptHandle();
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
