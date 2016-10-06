package cs307.butterfly;

import android.util.Log;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;

/**
 * Created by Travis on 10/6/2016.
 */

public class CommunityEvent {
    private Calendar time;
    private String name;
    private String place;
    private String description;

    public CommunityEvent(Calendar time, String name) {
        this(time, name, null, null);
    }

    public CommunityEvent(Calendar date, String name, String place, String description) {
        date.set(HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        this.time = date;
        this.name = name;
        this.place = place;
        this.description = description;
    }

    public int getStartTime() {
        return this.time.get(HOUR_OF_DAY);
    }

    public int getDayOfYear() {
        return this.time.get(Calendar.DAY_OF_YEAR);
    }

    public int getYear() {
        return this.time.get(Calendar.YEAR);
    }

    public Calendar getTime() {
        return this.time;
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
