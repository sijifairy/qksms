package com.moez.QKSMS.common.notificationcenter;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import androidx.collection.ArrayMap;

public class CommonBundle {
    private final Bundle bundle = new Bundle();
    private final Map<String, Object> objMap = new ArrayMap();

    public CommonBundle() {
    }

    public boolean getBoolean(String key) {
        return this.bundle.getBoolean(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.bundle.getBoolean(key, defaultValue);
    }

    public boolean[] getBooleanArray(String key) {
        return this.bundle.getBooleanArray(key);
    }

    public int getInt(String key) {
        return this.bundle.getInt(key);
    }

    public int getInt(String key, int defaultValue) {
        return this.bundle.getInt(key, defaultValue);
    }

    public int[] getIntArray(String key) {
        return this.bundle.getIntArray(key);
    }

    public long getLong(String key) {
        return this.bundle.getLong(key);
    }

    public long getLong(String key, long defaultValue) {
        return this.bundle.getLong(key, defaultValue);
    }

    public long[] getLongArray(String key) {
        return this.bundle.getLongArray(key);
    }

    public float getFloat(String key) {
        return this.bundle.getFloat(key);
    }

    public float getFloat(String key, float defaultValue) {
        return this.bundle.getFloat(key, defaultValue);
    }

    public float[] getFloatArray(String key) {
        return this.bundle.getFloatArray(key);
    }

    public double getDouble(String key) {
        return this.bundle.getDouble(key);
    }

    public double getDouble(String key, double defaultValue) {
        return this.bundle.getDouble(key, defaultValue);
    }

    public double[] getDoubleArray(String key) {
        return this.bundle.getDoubleArray(key);
    }

    public String getString(String key) {
        return this.bundle.getString(key);
    }

    public String getString(String key, String defaultValue) {
        String str = this.bundle.getString(key);
        return TextUtils.isEmpty(str) ? defaultValue : str;
    }

    public String[] getStringArray(String key) {
        return this.bundle.getStringArray(key);
    }

    public void putBoolean(String key, boolean value) {
        this.bundle.putBoolean(key, value);
    }

    public void putBooleanArray(String key, boolean[] value) {
        this.bundle.putBooleanArray(key, value);
    }

    public void putInt(String key, int value) {
        this.bundle.putInt(key, value);
    }

    public void putIntArray(String key, int[] value) {
        this.bundle.putIntArray(key, value);
    }

    public void putLong(String key, long value) {
        this.bundle.putLong(key, value);
    }

    public void putLongArray(String key, long[] value) {
        this.bundle.putLongArray(key, value);
    }

    public void putFloat(String key, float value) {
        this.bundle.putFloat(key, value);
    }

    public void putFloatArray(String key, float[] value) {
        this.bundle.putFloatArray(key, value);
    }

    public void putDouble(String key, double value) {
        this.bundle.putDouble(key, value);
    }

    public void putDoubleArray(String key, double[] value) {
        this.bundle.putDoubleArray(key, value);
    }

    public void putString(String key, String value) {
        this.bundle.putString(key, value);
    }

    public void putStringArray(String key, String[] value) {
        this.bundle.putStringArray(key, value);
    }

    public Object getObject(String key) {
        return this.objMap.get(key);
    }

    public void putObject(String key, Object value) {
        this.objMap.put(key, value);
    }

    public Object[] getObjectArray(String key) {
        Object o = this.objMap.get(key);
        if (o == null) {
            return null;
        } else {
            try {
                return (Object[])((Object[])o);
            } catch (ClassCastException var4) {
                return null;
            }
        }
    }

    public void putObjectArray(String key, Object[] value) {
        this.objMap.put(key, value);
    }

    public List<?> getObjectList(String key) {
        Object o = this.objMap.get(key);
        if (o == null) {
            return null;
        } else {
            try {
                return (List)o;
            } catch (ClassCastException var4) {
                return null;
            }
        }
    }

    public void putObjectList(String key, List<?> value) {
        this.objMap.put(key, value);
    }
}
