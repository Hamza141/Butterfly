package cs307.butterfly;

import android.util.Log;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;

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
        date.set(HOUR_OF_DAY, startHour);
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

    public String getName() {
        return this.name;
    }

    public String getPlace() {
        return this.place;
    }

    public String getDescription() {
        return this.description;
    }

}
