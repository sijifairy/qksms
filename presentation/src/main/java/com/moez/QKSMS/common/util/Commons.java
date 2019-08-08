package com.moez.QKSMS.common.util;

import android.os.DeadObjectException;
import android.os.TransactionTooLargeException;

/**
 * General utilities that cannot be categorized into any other utility class.
 */
public class Commons {

    public static boolean isBinderSizeError(Throwable e) {
        return e.getCause() instanceof TransactionTooLargeException
                || e.getCause() instanceof DeadObjectException;
    }
}
