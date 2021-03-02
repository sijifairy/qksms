package com.moez.QKSMS.common.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ADD_VOICEMAIL;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_MMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.RECEIVE_WAP_PUSH;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.USE_SIP;
import static android.Manifest.permission.WRITE_CALENDAR;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.LOCATION;
import static android.Manifest.permission_group.STORAGE;

/**
 * Helper class for checking and requesting permissions on behalf of application targeting
 * Marshmallow (API 23) or above.
 *
 * This class aims at handling complications involving permission groups.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RuntimePermissions {

    /** Permission is granted. See also {@link PackageManager#PERMISSION_GRANTED}. */
    public static final int PERMISSION_GRANTED = 0;

    /**
     * Another permission in the same permission group is granted. This permission is then granted
     * by default but application may need to call {@link #requestPermissions(Activity, String[], int)}
     * explicitly before actually performing any operation that requires the permission. This call
     * will not bring up any user-visible dialog. {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * will be called back immediately with result {@link PackageManager#PERMISSION_GRANTED}.
     */
    public static final int PERMISSION_GRANTED_BUT_NEEDS_REQUEST = 1;

    /** Permission is not granted. */
    public static final int PERMISSION_NOT_GRANTED = -1;

    /** Permission is once denied with "never ask again" chosen. */
    public static final int PERMISSION_PERMANENTLY_DENIED = -2;

    private static final String PREFS_NAME = "superapps_permissions";
    private static final String PREFS_KEY_PERMANENTLY_DENIED_GROUPS = "permanently_denied_groups";

    private static final Map<String, String> sPermissionToGroup = new HashMap<>(64);
    static {
        sPermissionToGroup.put(READ_CALENDAR, "android.permission-group.CALENDAR");
        sPermissionToGroup.put(WRITE_CALENDAR, "android.permission-group.CALENDAR");

        sPermissionToGroup.put("android.permission.READ_CALL_LOG", "android.permission-group.CALL_LOG");
        sPermissionToGroup.put("android.permission.WRITE_CALL_LOG", "android.permission-group.CALL_LOG");
        sPermissionToGroup.put(PROCESS_OUTGOING_CALLS, "android.permission-group.CALL_LOG");

        sPermissionToGroup.put(Manifest.permission.CAMERA, "android.permission-group.CAMERA");

        sPermissionToGroup.put(READ_CONTACTS, "android.permission-group.CONTACTS");
        sPermissionToGroup.put(WRITE_CONTACTS, "android.permission-group.CONTACTS");
        sPermissionToGroup.put(GET_ACCOUNTS, "android.permission-group.CONTACTS");

        sPermissionToGroup.put(ACCESS_FINE_LOCATION, LOCATION);
        sPermissionToGroup.put(ACCESS_COARSE_LOCATION, LOCATION);

        sPermissionToGroup.put(RECORD_AUDIO, "android.permission-group.MICROPHONE");

        sPermissionToGroup.put(READ_PHONE_STATE, "android.permission-group.PHONE");
        sPermissionToGroup.put("android.permission.READ_PHONE_NUMBERS", "android.permission-group.PHONE");
        sPermissionToGroup.put(CALL_PHONE, "android.permission-group.PHONE");
        sPermissionToGroup.put("android.permission.ANSWER_PHONE_CALLS", "android.permission-group.PHONE");
        sPermissionToGroup.put(ADD_VOICEMAIL, "android.permission-group.PHONE");
        sPermissionToGroup.put(USE_SIP, "android.permission-group.PHONE");

        sPermissionToGroup.put("android.permission.BODY_SENSORS", "android.permission-group.SENSORS");

        sPermissionToGroup.put(SEND_SMS, "android.permission-group.SMS");
        sPermissionToGroup.put(RECEIVE_SMS, "android.permission-group.SMS");
        sPermissionToGroup.put(READ_SMS, "android.permission-group.SMS");
        sPermissionToGroup.put(RECEIVE_WAP_PUSH, "android.permission-group.SMS");
        sPermissionToGroup.put(RECEIVE_MMS, "android.permission-group.SMS");

        sPermissionToGroup.put("android.permission.READ_EXTERNAL_STORAGE", STORAGE);
        sPermissionToGroup.put(WRITE_EXTERNAL_STORAGE, STORAGE);
    }

    private static List<String> sPermissionsWithRationaleFlag = new ArrayList<>();

    public static int checkSelfPermission(@NonNull Context context, @NonNull String permission) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> permanentlyDeniedGroups = prefs.getStringSet(
                PREFS_KEY_PERMANENTLY_DENIED_GROUPS, new HashSet<String>());
        String group = getPermissionGroup(permission);
        if (check(context, permission)) {
            Set<String> permanentlyDeniedCopy = new HashSet<>(permanentlyDeniedGroups);
            if (permanentlyDeniedCopy.remove(group)) {
                // User may have manually enabled this permission group again
                prefs.edit().putStringSet(PREFS_KEY_PERMANENTLY_DENIED_GROUPS,
                        permanentlyDeniedCopy).apply();
            }
            return PERMISSION_GRANTED;
        } else {
            if (permanentlyDeniedGroups.contains(group)) {
                return PERMISSION_PERMANENTLY_DENIED;
            }
            List<String> otherPermissionsInGroup = getOtherPermissionsInTheSameGroup(permission);
            for (String anotherPermission : otherPermissionsInGroup) {
                if (check(context, anotherPermission)) {
                    return PERMISSION_GRANTED_BUT_NEEDS_REQUEST;
                }
            }
            return PERMISSION_NOT_GRANTED;
        }
    }

    public static void requestPermissions(@NonNull final Activity activity,
                                          @NonNull final String[] permissions,
                                          @IntRange(from = 0L) final int requestCode) {
        sPermissionsWithRationaleFlag.clear();
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                sPermissionsWithRationaleFlag.add(permission);
            }
        }
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * Activity calling {@link #requestPermissions(Activity, String[], int)} must also forward
     * all callbacks to {@link Activity#onRequestPermissionsResult(int, String[], int[])} to this
     * method to maintain proper internal states.
     */
    public static void onRequestPermissionsResult(Activity activity,
                                                  int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> permanentlyDeniedGroups = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int grantResult = grantResults[i];
            // Signs when we know the user has permanently denied us:
                 // (1) shouldShowRequestPermissionRationale() were true when we issued the request.
            if (sPermissionsWithRationaleFlag.contains(permission)
                 // (2) the request was denied.
                    && grantResult == PackageManager.PERMISSION_DENIED
                 // (3) ... and shouldShowRequestPermissionRationale() has turned false.
                    && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                permanentlyDeniedGroups.add(getPermissionGroup(permission));
            }
        }
        sPermissionsWithRationaleFlag.clear();
        if (!permanentlyDeniedGroups.isEmpty()) {
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> existing = prefs.getStringSet(
                    PREFS_KEY_PERMANENTLY_DENIED_GROUPS, new HashSet<String>());
            Set<String> merged = new HashSet<>(existing);
            merged.addAll(permanentlyDeniedGroups);
            prefs.edit().putStringSet(PREFS_KEY_PERMANENTLY_DENIED_GROUPS, merged).apply();
        }
    }

    private static boolean check(@NonNull Context context, @NonNull String permission) {
        if (Compats.IS_LENOVO_DEVICE && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            return true;
        }

        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static String getPermissionGroup(@NonNull String permission) {
        String group = sPermissionToGroup.get(permission);
        return group == null ? "" : group;
    }

    private static List<String> getOtherPermissionsInTheSameGroup(@NonNull String permission) {
        String group = getPermissionGroup(permission);
        if (group == null) {
            return new ArrayList<>();
        }
        List<String> otherPermissions = new ArrayList<>();
        for (String anotherPermission : sPermissionToGroup.keySet()) {
            if (!permission.equals(anotherPermission)
                    && group.equals(sPermissionToGroup.get(anotherPermission))) {
                otherPermissions.add(anotherPermission);
            }
        }
        return otherPermissions;
    }
}
