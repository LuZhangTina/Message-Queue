package com.example;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by tina on 2019/2/4.
 */
public class QueueVisibilityTimeout {
    private static final int MAX_TIMEOUT_SECONDS = 43200;
    private static final int MIN_TIMEOUT_SECONDS = 0;
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    public static int getMaxVisibilityTimeout() {
        return MAX_TIMEOUT_SECONDS;
    }

    public static int getMinVisibilityTimeout() {
        return MIN_TIMEOUT_SECONDS;
    }

    public static int getDefaultVisibilityTimeout() {
        return DEFAULT_TIMEOUT_SECONDS;
    }

    public static Date createVisibleDate(int visibleTimeout) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, visibleTimeout);
        return calendar.getTime();
    }
}
