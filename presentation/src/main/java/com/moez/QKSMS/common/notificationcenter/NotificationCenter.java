package com.moez.QKSMS.common.notificationcenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NotificationCenter {
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final HashMap<String, MyObservable> mapEventObservable = new HashMap();

    public NotificationCenter() {
    }

    public boolean isEmpty() {
        HashMap var1 = this.mapEventObservable;
        synchronized (this.mapEventObservable) {
            return this.mapEventObservable.isEmpty();
        }
    }

    public void addObserver(String notificationName, INotificationObserver observer) {
        if (observer != null) {
            HashMap var3 = this.mapEventObservable;
            synchronized (this.mapEventObservable) {
                NotificationCenter.MyObservable observable = (NotificationCenter.MyObservable) this.mapEventObservable.get(notificationName);
                if (observable == null) {
                    observable = new NotificationCenter.MyObservable();
                    this.mapEventObservable.put(notificationName, observable);
                }

                observable.addObserver(observer);
            }
        }
    }

    public void addObserver(final String notificationName, final INotificationObserver observer, final Handler handler) {
        this.addObserver(notificationName, new INotificationObserver() {
            public void onReceive(String notificaitonName, final CommonBundle bundle) {
                (handler == null ? NotificationCenter.mainHandler : handler).post(new Runnable() {
                    public void run() {
                        if (NotificationCenter.this.containsObserver(notificationName, observer)) {
                            observer.onReceive(notificationName, bundle);
                        }

                    }
                });
            }
        });
    }

    public void removeObserver(String notificationName, INotificationObserver observer) {
        HashMap var3 = this.mapEventObservable;
        synchronized (this.mapEventObservable) {
            NotificationCenter.MyObservable observable = (NotificationCenter.MyObservable) this.mapEventObservable.get(notificationName);
            if (observable != null) {
                observable.deleteObserver(observer);
                if (observable.isEmpty()) {
                    this.mapEventObservable.remove(notificationName);
                }
            }

        }
    }

    public void removeObserver(INotificationObserver observer) {
        HashMap var2 = this.mapEventObservable;
        synchronized (this.mapEventObservable) {
            Iterator itEvent = this.mapEventObservable.entrySet().iterator();

            while (itEvent.hasNext()) {
                Map.Entry<String, MyObservable> entry = (Map.Entry) itEvent.next();
                NotificationCenter.MyObservable observable = (NotificationCenter.MyObservable) entry.getValue();
                observable.deleteObserver(observer);
                if (observable.isEmpty()) {
                    itEvent.remove();
                }
            }

        }
    }

    private boolean containsObserver(String notificationName, INotificationObserver observer) {
        HashMap var3 = this.mapEventObservable;
        synchronized (this.mapEventObservable) {
            NotificationCenter.MyObservable observable = (NotificationCenter.MyObservable) this.mapEventObservable.get(notificationName);
            return observable != null ? observable.containsObserver(observer) : false;
        }
    }

    public void sendNotification(String notificationName) {
        this.sendNotification(notificationName, (CommonBundle) null);
    }

    public void sendNotification(String notificationName, CommonBundle bundle) {
        Log.d(NotificationCenter.class.getSimpleName(), notificationName + " " + bundle);
        HashMap var4 = this.mapEventObservable;
        NotificationCenter.MyObservable observable;
        synchronized (this.mapEventObservable) {
            observable = (NotificationCenter.MyObservable) this.mapEventObservable.get(notificationName);
        }

        if (observable != null) {
            observable.notifyObservers(notificationName, bundle);
        }

    }

    public void sendNotificationOnMainLooper(String notificationName) {
        this.sendNotificationOnMainLooper(notificationName, (CommonBundle) null);
    }

    public void sendNotificationOnMainLooper(final String notificationName, final CommonBundle bundle) {
        this.runOnUIThread(new Runnable() {
            public void run() {
                NotificationCenter.this.sendNotification(notificationName, bundle);
            }
        });
    }

    private void runOnUIThread(final Runnable action) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            action.run();
        } else {
            final Object lock = new Object();
            synchronized (lock) {
                try {
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            action.run();
                            Object var1 = lock;
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    });
                    lock.wait();
                } catch (Exception var6) {
                    var6.printStackTrace();
                }

            }
        }
    }

    private class MyObservable {
        private final List<INotificationObserver> observers;

        private MyObservable() {
            this.observers = new ArrayList();
        }

        boolean isEmpty() {
            return this.observers.isEmpty();
        }

        void addObserver(INotificationObserver observer) {
            synchronized (this) {
                if (observer != null && !this.observers.contains(observer)) {
                    this.observers.add(observer);
                }

            }
        }

        boolean deleteObserver(INotificationObserver observer) {
            synchronized (this) {
                return this.observers.remove(observer);
            }
        }

        boolean containsObserver(INotificationObserver observer) {
            synchronized (this) {
                return this.observers.contains(observer);
            }
        }

        void notifyObservers(String notificaitonName, CommonBundle bundle) {
            INotificationObserver[] arrays;
            synchronized (this) {
                arrays = new INotificationObserver[this.observers.size()];
                this.observers.toArray(arrays);
            }

            INotificationObserver[] var4 = arrays;
            int var5 = arrays.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                INotificationObserver observer = var4[var6];
                observer.onReceive(notificaitonName, bundle);
            }

        }
    }
}
