package org.vaadin.addon.daterangefield;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil implements Serializable {

    public static boolean isAfter(Date isThisDateAfter, Date thisDate) {
        DateTime d1 = new DateTime(isThisDateAfter);
        DateTime d2 = new DateTime(thisDate);
        return (d1.withTimeAtStartOfDay().isAfter(d2.withTimeAtStartOfDay()));
    }

    public static boolean isBefore(Date isThisDateBefore, Date thisDate) {
        return isBefore(new DateTime(isThisDateBefore), new DateTime(thisDate));
    }

    public static boolean isOnOrBefore(Date isThisDateOnOrBefore, Date thisDate) {
        return isOnOrBefore(new DateTime(isThisDateOnOrBefore), new DateTime(thisDate));
    }

    public static boolean isOnOrAfter(Date isThisDateOnOrAfter, Date thisDate) {
        return isOnOrAfter(new DateTime(isThisDateOnOrAfter), new DateTime(thisDate));
    }

    public static boolean isBetweenInclusive(Date date, Date startDate, Date endDate) {
        return isOnOrAfter(date, startDate) && isOnOrBefore(date, endDate);
    }

    public static boolean isBetweenNotInclusive(Date date, Date startDate, Date endDate) {
        return isAfter(date, startDate) && isBefore(date, endDate);
    }

    public static boolean isOnOrAfter(final DateTime isThisDateOnOrAfter, final DateTime thisDate) {
        return !((null == isThisDateOnOrAfter) || (null == thisDate)) && ((isThisDateOnOrAfter.withTimeAtStartOfDay()
                .isEqual(thisDate.withTimeAtStartOfDay())) || (isThisDateOnOrAfter.withTimeAtStartOfDay().isAfter
                (thisDate.withTimeAtStartOfDay())));
    }

    public static boolean isOnOrBefore(final DateTime isThisDateOnOrBefore, final DateTime thisDate) {
        return !((null == isThisDateOnOrBefore) || (null == thisDate)) && ((isThisDateOnOrBefore.withTimeAtStartOfDay
                ().isEqual(thisDate.withTimeAtStartOfDay())) || (isThisDateOnOrBefore.withTimeAtStartOfDay().isBefore
                (thisDate.withTimeAtStartOfDay())));
    }

    public static boolean isBefore(final DateTime isThisDateBefore, final DateTime thisDate) {
        return !isOnOrAfter(isThisDateBefore, thisDate);
    }

    public static int currentYear() {
        return DateTime.now().getYear();
    }

    public static Date now() {
        return DateTime.now().withTimeAtStartOfDay().toDate();
    }

    public static boolean isInYear(final Date d, final int year) {
        DateTime dt = new DateTime(d);
        return dt.getYear() == year;
    }

    public static Date firstDayOfYear(final int year) {
        DateTime dt = new DateTime(year, 1, 1, 0, 0);
        return dt.withTimeAtStartOfDay().toDate();
    }

    public static Date lastDayOfYear(final int year) {
        DateTime dt = new DateTime(year, 12, 31, 0, 0);
        return dt.withTimeAtStartOfDay().toDate();
    }

    public static Date sameDayDifferentYear(final Date d, final int newYear) {
        DateTime dt = new DateTime(d);
        DateTime newDate = new DateTime(newYear, dt.getMonthOfYear(), dt.getDayOfMonth(), 0, 0);
        return newDate.withTimeAtStartOfDay().toDate();
    }

    public static Date stringToDate(final String dateString, String pattern, final Locale locale) {
        if (null == pattern) {
            pattern = "dd-MMM-yyyy";
        }
        DateFormat fmt = new SimpleDateFormat(pattern, locale);
        Date d = null;
        try {
            return fmt.parse(dateString);
        } catch (ParseException e) {
            return null;
            //throw new Converter.ConversionException(e);
        }
    }

}