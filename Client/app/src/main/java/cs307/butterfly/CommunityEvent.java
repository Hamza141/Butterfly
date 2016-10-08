package cs307.butterfly;

import android.util.Log;

import java.util.Calendar;

/**
 * Created by Travis on 10/6/2016.
 */

public class CommunityEvent {
    private Calendar date;
    private String name;
    private String place;
    private String description;

    public CommunityEvent(Calendar date, int startHour, int startMinute, String name) {
        this(date, startHour, startMinute, name, null, null);
    }

    public CommunityEvent(Calendar date, int startHour, int startMinute, String name, String place, String description) {
        date.set(Calendar.HOUR_OF_DAY, startHour);
        date.set(Calendar.MINUTE, startMinute);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        this.date = date;
        this.name = name;
        this.place = place;
        this.description = description;
    }

    public Calendar getDate() {
        return this.date;
    }

    public int getStartHour() {
        return this.date.get(Calendar.HOUR_OF_DAY);
    }

    public int getStartMinute() {
        return this.date.get(Calendar.MINUTE);
    }

    public String getStartTime() {
        String startTime = "\n";
        startTime = startTime.concat("Start Time: ");
        if (date.get(Calendar.HOUR) == 0) {
            startTime = startTime.concat("12");
        }
        else {
            startTime = startTime.concat(String.valueOf(date.get(Calendar.HOUR)));
        }
        startTime = startTime.concat(":");
        if (date.get(Calendar.MINUTE) / 10 == 0) {
            startTime = startTime.concat("0");
        }
        startTime = startTime.concat(String.valueOf(date.get(Calendar.MINUTE)));
        int ampm = date.get(Calendar.AM_PM);
        switch (ampm) {
            case Calendar.AM:
                startTime = startTime.concat(" AM");
                break;
            case Calendar.PM:
                startTime = startTime.concat(" PM");
                break;
        }
        return startTime;
    }

    public String getName() {
        return this.name;
    }

    public String getPlace() {
        return "\nPlace: " + this.place;
    }

    public String getDescription() {
        return "\n" + this.description + "\n\n";
    }

    public String toString() {
        String eventString = this.name + "\n";
        /*int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                eventString = eventString.concat("Sunday ");
                break;
            case Calendar.MONDAY:
                eventString = eventString.concat("Monday ");
                break;
            case Calendar.TUESDAY:
                eventString = eventString.concat("Tuesday ");
                break;
            case Calendar.WEDNESDAY:
                eventString = eventString.concat("Wednesday ");
                break;
            case Calendar.THURSDAY:
                eventString = eventString.concat("Thursday ");
                break;
            case Calendar.FRIDAY:
                eventString = eventString.concat("Friday ");
                break;
            case Calendar.SATURDAY:
                eventString = eventString.concat("Saturday ");
                break;
        }

        int month = date.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
                eventString = eventString.concat("January ");
                break;
            case Calendar.FEBRUARY:
                eventString = eventString.concat("February ");
                break;
            case Calendar.MARCH:
                eventString = eventString.concat("March ");
                break;
            case Calendar.APRIL:
                eventString = eventString.concat("April ");
                break;
            case Calendar.MAY:
                eventString = eventString.concat("May ");
                break;
            case Calendar.JUNE:
                eventString = eventString.concat("June ");
                break;
            case Calendar.JULY:
                eventString = eventString.concat("July ");
                break;
            case Calendar.AUGUST:
                eventString = eventString.concat("August ");
                break;
            case Calendar.SEPTEMBER:
                eventString = eventString.concat("September ");
                break;
            case Calendar.OCTOBER:
                eventString = eventString.concat("October ");
                break;
            case Calendar.NOVEMBER:
                eventString = eventString.concat("November ");
                break;
            case Calendar.DECEMBER:
                eventString = eventString.concat("December ");
                break;
        }
        eventString = eventString.concat(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));*/

        eventString = eventString.concat("Start Time: ");
        if (date.get(Calendar.HOUR) == 0) {
            eventString = eventString.concat("12");
        }
        else {
            eventString = eventString.concat(String.valueOf(date.get(Calendar.HOUR)));
        }
        eventString = eventString.concat(":");
        if (date.get(Calendar.MINUTE) / 10 == 0) {
            eventString = eventString.concat("0");
        }
        eventString = eventString.concat(String.valueOf(date.get(Calendar.MINUTE)));

        int ampm = date.get(Calendar.AM_PM);
        switch (ampm) {
            case Calendar.AM:
                eventString = eventString.concat(" AM");
                break;
            case Calendar.PM:
                eventString = eventString.concat(" PM");
                break;
        }

        if (place != null) {
            eventString = eventString.concat("\nPlace: ");
            eventString = eventString.concat(place);
        }

        if (description != null) {
            eventString = eventString.concat("\n");
            eventString = eventString.concat(description);
        }

        eventString = eventString.concat("\n\n");

        return eventString;
    }

}
