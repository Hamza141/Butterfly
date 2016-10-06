package cs307.butterfly;

import android.util.Log;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;

/**
 * Created by Travis on 10/6/2016.
 */

public class CommunityEvent {
    private Calendar date;
    private String startTime;
    private String name;
    private String place;
    private String description;

    public CommunityEvent(Calendar date, String startTime, String name) {
        this(date, startTime, name, null, null);
    }

    public CommunityEvent(Calendar date, String startTime, String name, String place, String description) {
        date.set(HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        Log.d("CommunityEvent", String.valueOf(date.get(HOUR_OF_DAY)));
        this.date = date;
        this.startTime = startTime;
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

    public String getStartTime() {
        return  this.startTime;
    }
}
