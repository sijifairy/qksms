package com.moez.QKSMS.feature.guide.topapp;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.moez.QKSMS.common.BaseApplication;
import com.moez.QKSMS.common.util.Packages;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 用于指定不同方法，获取当前前台运行的应用的包名
 */
public class ForegroundAppReporter {

    /**
     * 方法的枚举
     */
    public enum ReportMethod {

        /**
         * 通过 ActivityManager 的 getRunningTasks() 获取前台 Activity 名称，再由 Activity 名称获取包名
         * <p/>
         * 适用范围：API 1-20
         */
        GET_RUNNING_TASK(1),

        /**
         * 通过 ActivityManager 的 getRunningAppProcesses() 获取进程名，进程名通常即是包名
         * <p/>
         * 适用范围：API 3-21
         * 缺点：在本方法与 GET_RUNNING_TASK 方法均可用的平台上，本方法相对较费电，消耗大约 3 倍的 CPU 时间
         */
        GET_RUNNING_APP_PROCESSES(2),

        /**
         * 通过系统 UsageStats API 获取包名
         * <p/>
         * 适用范围：API 21-23
         * 缺点：需要用户在设置中手工开权限，部分主流机型（如 Samsung）上此 API 不可用
         */
        USAGE_STATS(5);

        private int value = -1;

        ReportMethod(int value) {
            this.value = value;
        }

        public String toString() {
            switch (value) {
                case 1:
                    return "GET_RUNNING_TASK";
                case 2:
                    return "GET_RUNNING_APP_PROCESSES";
                case 5:
                    return "USAGE_STATS";
            }
            return "";
        }
    }

    public static class ForegroundAppInfo {
        public String packageName;
        public int pid;
        public int oomScoreAdj;

        public ForegroundAppInfo(String packageName) {
            this.packageName = packageName;
            this.pid = -1;
            this.oomScoreAdj = Integer.MIN_VALUE;
        }

        public ForegroundAppInfo(String packageName, int pid, int oomScoreAdj) {
            this.packageName = packageName;
            this.pid = pid;
            this.oomScoreAdj = oomScoreAdj;
        }

        public String toString() {
            return "package name = " + packageName + ", pid = " + pid + ", oom score adj = " + oomScoreAdj;
        }
    }

    private static final int PROCESS_STATE_TOP = 2;
    private static final int FLAG_HAS_ACTIVITIES = 1 << 2;

    private ReportMethod reportMethod;

    private Field processStateField;
    private Field flagsField;

    private Context context;

    private String lastPackageNameOfUsageStat;

    public ForegroundAppReporter() {
        this.context = BaseApplication.getContext();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            reportMethod = ReportMethod.GET_RUNNING_TASK;
        } else {
            reportMethod = ReportMethod.USAGE_STATS;
        }
    }

    /**
     * 获取当前前台运行的应用的包名
     */
    public ForegroundAppInfo getForegroundPackageName() {

        ForegroundAppInfo foregroundAppInfo = null;

        switch (reportMethod) {

            case GET_RUNNING_TASK:
                foregroundAppInfo = new ForegroundAppInfo(getForegroundPackageNameByGetRunningTask());
                break;

            case GET_RUNNING_APP_PROCESSES:
                foregroundAppInfo = new ForegroundAppInfo(getForegroundPackageNameByGetRunningAppProcesses());
                break;

            case USAGE_STATS: {
                String packageName = getForegroundPackageNameByUsageStats();
                if (!TextUtils.isEmpty(packageName)) {
                    foregroundAppInfo = new ForegroundAppInfo(packageName);
                    lastPackageNameOfUsageStat = packageName;
                } else {
                    foregroundAppInfo = new ForegroundAppInfo(lastPackageNameOfUsageStat);
                }
                break;
            }

            default:
                break;
        }
        return foregroundAppInfo;
    }

    private String getForegroundPackageNameByGetRunningTask() {
        if (Build.VERSION.SDK_INT >= 21) {
            return "";
        }
        if (null != context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (null != activityManager) {
                @SuppressWarnings("deprecation")
                List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
                if (null != taskInfo && taskInfo.size() > 0) {
                    ActivityManager.RunningTaskInfo runningTaskInfo = taskInfo.get(0);
                    if (null != runningTaskInfo) {
                        ComponentName componentName = runningTaskInfo.topActivity;
                        if (null != componentName) {
                            return componentName.getPackageName();
                        }
                    }
                }
            }
        }
        return "";
    }

    private String getForegroundPackageNameByGetRunningAppProcesses() {
        //TODO
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
            return "";
        }

        if (null == processStateField) {
            try {
                processStateField = RunningAppProcessInfo.class.getDeclaredField("processState");
            } catch (Exception e) {
            }
        }
        if (null == flagsField) {
            try {
                flagsField = RunningAppProcessInfo.class.getDeclaredField("flags");
            } catch (Exception e) {
            }
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

        if (runningAppProcesses == null) {
            return "";
        }

        String foregroundPackageName = null;

        for (RunningAppProcessInfo appProcessInfo : runningAppProcesses) {
            // Log.i("ForegroundAppReporter", "getForegroundPackageNameByGetRunningAppProcesses(), app process name = " + appProcessInfo.processName);

            if (RunningAppProcessInfo.IMPORTANCE_FOREGROUND != appProcessInfo.importance && RunningAppProcessInfo.IMPORTANCE_BACKGROUND != appProcessInfo.importance) {
                // Log.i("ForegroundAppReporter", "getForegroundPackageNameByGetRunningAppProcesses(), app process name = "
                //        + appProcessInfo.processName + ", importance is not IMPORTANCE_FOREGROUND, importance = " + appProcessInfo.importance);
                continue;
            }
            if (0 != appProcessInfo.importanceReasonCode) {
                // Log.i("ForegroundAppReporter", "getForegroundPackageNameByGetRunningAppProcesses(), app process name = "
                //        + appProcessInfo.processName + ", importance reason code is not 0");
                continue;
            }
            if (null != flagsField) {
                Integer flags = null;
                try {
                    flags = flagsField.getInt(appProcessInfo);
                } catch (Exception ignored) {
                }
                if (null == flags) {
                    // Log.i("ForegroundAppReporter", "getForegroundPackageNameByGetRunningAppProcesses(), app process name = "
                    //        + appProcessInfo.processName + ", no app process info");
                    continue;
                }
                if (0 == (flags.intValue() & FLAG_HAS_ACTIVITIES)) {
                    // Log.i("ForegroundAppReporter", "getForegroundPackageNameByGetRunningAppProcesses(), app process name = "
                    //         + appProcessInfo.processName + ", no FLAG_HAS_ACTIVITIES");
                    continue;
                }
            }
            if (null != processStateField) {
                Integer state = null;
                try {
                    state = processStateField.getInt(appProcessInfo);
                } catch (Exception ignored) {
                }
                if (null == state || PROCESS_STATE_TOP != state) {
                    // Log.i("ForegroundAppReporter", "getForegroundPackageNameByGetRunningAppProcesses(), app process name = "
                    //        + appProcessInfo.processName + ", state is not PROCESS_STATE_TOP");
                    continue;
                }
            }

            String packageName = appProcessInfo.processName.split(":")[0].trim();
            if (!Packages.isLaunchableApp(packageName)) {
                // Log.i("ForegroundAppReporter", "getForegroundPackageNameByGetRunningAppProcesses(), app process name = "
                //        + appProcessInfo.processName + ", not Launcher app and not launchable app");
                continue;
            }

            foregroundPackageName = packageName;
            break;
        }
        return null == foregroundPackageName ? "" : foregroundPackageName;
    }

    private String getForegroundPackageNameByUsageStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return "";
        }

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents;
        UsageEvents.Event resultEvent = null;
        try {
            usageEvents = usageStatsManager.queryEvents(System.currentTimeMillis() - 10000, System.currentTimeMillis());
            while (usageEvents.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageEvents.getNextEvent(event);
                if (UsageEvents.Event.MOVE_TO_FOREGROUND == event.getEventType()) {
                    resultEvent = event;
                }
            }
        } catch (Exception e) {
//            CrashlyticsCoreCompat.getInstance().logException(e);
        }

        return null == resultEvent ? null : resultEvent.getPackageName();
    }
}
