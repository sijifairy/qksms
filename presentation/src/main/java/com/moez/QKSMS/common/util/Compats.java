package com.moez.QKSMS.common.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Compats {

    public static final boolean IS_GOOGLE_DEVICE = "Google".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_SAMSUNG_DEVICE = "Samsung".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_HUAWEI_DEVICE = "Huawei".equalsIgnoreCase(Build.BRAND)
            || (!IS_GOOGLE_DEVICE && "Huawei".equalsIgnoreCase(Build.MANUFACTURER));

    public static final boolean IS_HTC_DEVICE = "HTC".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_LGE_DEVICE = "LGE".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_SONY_DEVICE = "Sony".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_MOTOROLA_DEVICE = "Motorola".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_LENOVO_DEVICE = "Lenovo".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_ZTE_DEVICE = "Zte".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_MEIZU_DEVICE = "Meizu".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_XIAOMI_DEVICE = "Xiaomi".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_TCL_DEVICE = "TCL".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_VIVO_DEVICE = "Vivo".equalsIgnoreCase(Build.BRAND);

    public static final boolean IS_SMARTISAN_DEVICE = "SMARTISAN".equalsIgnoreCase(Build.BRAND);

    public static final String SAMSUNG_SM_G9500 = "SM-G9500";
    private static final String GOOGLE_NEXUS_5 = "Nexus 5";
    private static final String MOTOROLA_MOTOE2 = "MotoE2(4G-LTE)";

    /**
     * Huawei EMUI devices.
     */
    public static class EmuiBuild {
        private static final String BUILD_PROP_NAME = "ro.build.hw_emui_api_level";

        public static class VERSION_CODES {
            /**
             * EMUI 4.0 (API 23).
             */
            public static final int EMUI_4_0 = 9;

            /**
             * EMUI 4.1 (API 23).
             */
            public static final int EMUI_4_1 = 10;

            /**
             * EMUI 5.0 (API 24).
             */
            public static final int EMUI_5_0 = 11;
        }
    }

    /**
     * Only works for EMUI 4.0 or above.
     */
    public static int getHuaweiEmuiVersionCode() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            String versionStr = prop.getProperty(EmuiBuild.BUILD_PROP_NAME);
            return Integer.valueOf(versionStr);
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    @SuppressLint("PrivateApi")
    public static String getHuaweiEmuiVersionName() {
        Class<?> classType;
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            return (String) getMethod.invoke(classType, "ro.build.version.emui");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static class BuildProperties {
        private final Properties properties;

        @SuppressWarnings("TryFinallyCanBeTryWithResources")
        private BuildProperties() throws IOException {
            properties = new Properties();
            InputStream is = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            try {
                properties.load(is);
            } finally {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }

        String getProperty(final String name) {
            return properties.getProperty(name, null);
        }

        static BuildProperties newInstance() throws IOException {
            return new BuildProperties();
        }
    }

    public static boolean isUpNavigationBarHeight() {
        return !(IS_SAMSUNG_DEVICE && Build.MODEL.equals(SAMSUNG_SM_G9500));
    }

    public static boolean isCouldImmerse() {
        return !((IS_GOOGLE_DEVICE && Build.MODEL.equals(GOOGLE_NEXUS_5))
                || IS_MOTOROLA_DEVICE && Build.MODEL.equals(MOTOROLA_MOTOE2));
    }
}
