package com.moez.QKSMS.common.notificationcenter;

public interface INotificationObserver {
    void onReceive(String s, CommonBundle bundle);
}