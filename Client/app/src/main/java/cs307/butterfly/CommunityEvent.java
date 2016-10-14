package cs307.butterfly;

import android.util.Log;

import java.util.Calendar;
import java.util.jar.Pack200;

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

    public CommunityEvent(Calendar date, String name, String time, String place, String description) {
        this.date = date;
        this.name = name;
        this.place = place;
        this.description = description;
        parseTime(time, this);
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
        String eventString = "Name: " + this.name + "\n";

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
            eventString = eventString.concat("Description: ");
            eventString = eventString.concat(description);
        }

        eventString = eventString.concat("\n\n");

        return eventString;
    }

    public static void parseTime(String time, CommunityEvent event) {
        String[] split = time.split(":");
        if (split[0].equals("12")) {
            split[0] = "0";
        }
        event.date.set(Calendar.HOUR, Integer.parseInt(split[0]));
        String[] split2 = split[1].split(" ");
        event.date.set(Calendar.MINUTE, Integer.parseInt(split2[0]));
        event.date.set(Calendar.SECOND, 0);
        event.date.set(Calendar.MILLISECOND, 0);
        if (split2[1].equals("am")) {
            event.date.set(Calendar.AM_PM, Calendar.AM);
        }
        else {
            event.date.set(Calendar.AM_PM, Calendar.PM);
        }
    }

    public void editInfo(String name, String place, String description, String time) {
        if (!name.equals("")) {
            this.name = name;
        }
        if (!place.equals("")) {
            this.place = place;
        }
        if (!description.equals("")) {
            this.description = description;
        }
        if (!time.equals("")) {
            parseTime(time, this);
        }
    }

}
