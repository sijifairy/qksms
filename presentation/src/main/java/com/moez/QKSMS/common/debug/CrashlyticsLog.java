package com.moez.QKSMS.common.debug;

/**
 * Exception type for logging to Crashlytics.
 */
public class CrashlyticsLog extends Exception {

    public CrashlyticsLog(String logMessage) {
        super(logMessage);
    }

    public CrashlyticsLog(String logMessage, Throwable cause) {
        super(logMessage, cause);
    }
}
