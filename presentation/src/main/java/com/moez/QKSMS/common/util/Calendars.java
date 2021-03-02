package com.moez.QKSMS.common.util;

import java.util.Calendar;

public class Calendars {

    public static boolean isSameDay(long time1, long time2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

    public static int getDayDifference(long epoch1, long epoch2) {
        return getDayDifference(epoch1, epoch2, 0);
    }

    public static int getDayDifference(long epoch1, long epoch2, int dateLineHour) {
        Calendar wholeDayDate1 = getWholeDayCalendar(epoch1, dateLineHour);
        Calendar wholeDayDate2 = getWholeDayCalendar(epoch2, dateLineHour);

        // Get the represented date in milliseconds
        long wholeDayMillis1 = wholeDayDate1.getTimeInMillis();
        long wholeDayMillis2 = wholeDayDate2.getTimeInMillis();

        // Calculate difference in milliseconds
        long diff = Math.abs(wholeDayMillis1 - wholeDayMillis2);

        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    private static Calendar getWholeDayCalendar(long epoch, int dateLineHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epoch);
        if (calendar.get(Calendar.HOUR_OF_DAY) < dateLineHour) {
            calendar.add(Calendar.DATE, -1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
