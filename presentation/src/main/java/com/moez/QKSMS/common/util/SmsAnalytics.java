package com.moez.QKSMS.common.util;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.flurry.android.FlurryAgent;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.moez.QKSMS.BuildConfig;
import com.moez.QKSMS.common.QKApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For v1.4.5 (78) release: to cut down number of events reported to Flurry, we log most events only to Fabric.
 * Give {@code true} for {@code alsoLogToFlurry} to log an event also to Flurry.
 */
public class SmsAnalytics {

    private static final String TAG = SmsAnalytics.class.getSimpleName();

    private static FirebaseAnalytics sFirebaseAnalytics;

    static {
        sFirebaseAnalytics = FirebaseAnalytics.getInstance(QKApplication.context);
    }

    private static HashMap<String, List<String>> sDebugEventMap = null;

    public static void logEvent(String eventID) {
        logEvent(eventID, false);
    }

    public static void logEvent(String eventID, boolean alsoLogToFlurry) {
        logEvent(eventID, alsoLogToFlurry, (HashMap<String, String>) null);
    }

    public static void logEvent(String eventID, String... vars) {
        logEvent(eventID, false, vars);
    }

    public static void logEvent(String eventID, boolean alsoLogToFlurry, String... vars) {
        HashMap<String, String> eventValue = new HashMap<>();
        if (null != vars) {
            int length = vars.length;
            if (length % 2 != 0) {
                --length;
            }

            String key;
            String value;
            int i = 0;

            while (i < length) {
                key = vars[i++];
                value = vars[i++];
                eventValue.put(key, value);
            }
        }

        logEvent(eventID, alsoLogToFlurry, eventValue);
    }

    public static void logEvent(final String eventID, final Map<String, String> eventValues) {
        logEvent(eventID, false, eventValues);
    }

    public static void logEvent(final String eventID, boolean alsoLogToFlurry, Map<String, String> eventValues) {
        try {
            CustomEvent event = new CustomEvent(eventID);

            if (eventValues != null) {
                for (Map.Entry<String, String> entry : eventValues.entrySet()) {
                    event.putCustomAttribute(entry.getKey(), entry.getValue());
                }
            }

            Answers.getInstance().logCustom(event);

            if (eventValues == null) {
                eventValues = new HashMap<>(1);
            }
            FlurryAgent.logEvent(eventID, eventValues);

            Bundle params = new Bundle();
            if (eventValues != null) {
                for (Map.Entry<String, String> entry : eventValues.entrySet()) {
                    String key = entry.getKey();

                    assertEventIdOrKeyOrValueLengthNoMoreThan40(key);
                    assertEventIdOrKeyStartLegal(key);
                    assertEventKeyNoMoreThan25(eventID, key);

                    String value = entry.getValue();
                    assertEventIdOrKeyOrValueLengthNoMoreThan40(value);

                    params.putString(key, value);
                }
            }
            sFirebaseAnalytics.logEvent(eventID, params);
            onLogEvent(eventID, alsoLogToFlurry, eventValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void onLogEvent(String eventID, boolean alsoLogToFlurry, Map<String, String> eventValues) {
        String eventDescription = getEventInfoDescription(eventID, alsoLogToFlurry, eventValues);
        Log.d(TAG, eventDescription);
    }

    private static String getEventInfoDescription(String eventID, boolean alsoLogToFlurry, Map<String, String> eventValues) {
        String scope = (alsoLogToFlurry ? "F" : " ") + "|A";
        StringBuilder values = new StringBuilder();
        if (eventValues != null) {
            for (Map.Entry<String, String> valueEntry : eventValues.entrySet()) {
                values.append(valueEntry).append(", ");
            }
        }

        if (values.length() > 0) {
            values = new StringBuilder(": " + values.substring(0, values.length() - 2)); // At ": " at front and remove ", " in the end
        }
        return "(" + scope + ") " + eventID + values;
    }

    private static void assertEventIdOrKeyOrValueLengthNoMoreThan40(String eventIdOrKeyOrValue) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (TextUtils.isEmpty(eventIdOrKeyOrValue)) {
            throw new AssertionError("the eventIdOrKeyOrValue is null!!!");
        }

        if (eventIdOrKeyOrValue.length() > 40) {
            throw new AssertionError("The length of " + eventIdOrKeyOrValue + " already more than 40 which is illegal!!!!!");
        }
    }

    private static void assertEventIdOrKeyStartLegal(String eventIdOrKey) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (TextUtils.isEmpty(eventIdOrKey)) {
            throw new AssertionError("the eventIdOrKey is null!!!");
        }

        if (eventIdOrKey.startsWith("firebase_")
                || eventIdOrKey.startsWith("google_")
                || eventIdOrKey.startsWith("ga_")) {
            throw new AssertionError(eventIdOrKey + " is start with \"firebase_\" or \"google_\" or \"ga_\" which is illegal");
        }
    }

    private static void assertEventKeyNoMoreThan25(String eventId, String key) {
        if (!BuildConfig.DEBUG) {
            return;
        }

        if (sDebugEventMap == null) {
            sDebugEventMap = new HashMap<>();
        }

        List<String> keyList = sDebugEventMap.get(eventId);
        if (keyList == null) {
            keyList = new ArrayList<>();
        }
        if (!keyList.contains(key)) {
            keyList.add(key);
            if (keyList.size() > 25) {
                throw new AssertionError("The parameters of event must not exceed 25，" +
                        "but the parameters of " + eventId + " has been more than 25!!! Please check it.");
            }

            sDebugEventMap.put(eventId, keyList);
        }
    }
}
