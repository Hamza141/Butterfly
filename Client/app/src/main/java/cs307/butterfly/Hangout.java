package cs307.butterfly;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class Hangout {
    private int minUsers;
    private int maxUsers;
    private String name;
    private Calendar startTime;
    private Calendar endTime;
    private ArrayList<String> users;
    private String creator;
    private ArrayList<String> googleIDs;

    public Hangout(int minUsers, int maxUsers, String name, int startHour, int startMinute, int endHour, int endMinute, String creator, String creatorID) {
        this.minUsers = minUsers;
        this.maxUsers = maxUsers;
        this.name = name;

        Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.HOUR_OF_DAY, startHour);
        startDate.set(Calendar.MINUTE, startMinute);
        this.startTime = startDate;

        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR_OF_DAY, endHour);
        endDate.set(Calendar.MINUTE, endMinute);
        this.endTime = endDate;

        this.creator = creator;
        this.users = new ArrayList<>();
        users.add(creator);
        this.googleIDs = new ArrayList<>();
        googleIDs.add(creatorID);
    }

    public Hangout(int minUsers, int maxUsers, String name, String startTime, String endTime, String creator, ArrayList<String> googleIDs, ArrayList<String> users) {
        this.minUsers = minUsers;
        this.maxUsers = maxUsers;
        this.name = name;

        Calendar date = Calendar.getInstance();
        String[] startHourAndMinute = startTime.split(":");
        date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHourAndMinute[0]));
        date.set(Calendar.MINUTE, Integer.parseInt(startHourAndMinute[1]));
        this.startTime = date;

        Calendar date1 = Calendar.getInstance();
        String[] endHourAndMinute = endTime.split(":");
        date1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHourAndMinute[0]));
        date1.set(Calendar.MINUTE, Integer.parseInt(endHourAndMinute[1]));
        this.endTime = date1;
        this.creator = creator;
        this.users = users;
        this.googleIDs = googleIDs;
    }

    public String toString() {
        String hangoutString = "";
        hangoutString = hangoutString.concat(this.name + "\n");

        hangoutString = hangoutString.concat("Start Time: ");
        int startHour = startTime.get(Calendar.HOUR);
        int startMinute = startTime.get(Calendar.MINUTE);
        String startAMPM;
        if (startTime.get(Calendar.AM_PM) == Calendar.AM) {
            startAMPM = " AM";
        } else {
            startAMPM = " PM";
        }
        String startTimeString;
        if (startHour == 0) {
            startTimeString = "12";
        } else {
            startTimeString = String.valueOf(startHour);
        }
        startTimeString = startTimeString.concat(":");
        if (startMinute / 10 == 0) {
            startTimeString = startTimeString.concat("0");
        }
        startTimeString = startTimeString.concat(String.valueOf(startMinute));
        startTimeString = startTimeString.concat(startAMPM);
        hangoutString = hangoutString.concat(startTimeString + "\n");

        hangoutString = hangoutString.concat("End Time: ");
        int endHour = endTime.get(Calendar.HOUR);
        int endMinute = endTime.get(Calendar.MINUTE);
        String endAMPM;
        if (endTime.get(Calendar.AM_PM) == Calendar.AM) {
            endAMPM = " AM";
        } else {
            endAMPM = " PM";
        }
        String endTimeString;
        if (endHour == 0) {
            endTimeString = "12";
        } else {
            endTimeString = String.valueOf(endHour);
        }
        endTimeString = endTimeString.concat(":");
        if (endMinute / 10 == 0) {
            endTimeString = endTimeString.concat("0");
        }
        endTimeString = endTimeString.concat(String.valueOf(endMinute));
        endTimeString = endTimeString.concat(endAMPM);
        hangoutString = hangoutString.concat(endTimeString + "\n");

        if (minUsers != maxUsers) {
            hangoutString = hangoutString.concat("Members Required: ");
            hangoutString = hangoutString.concat(String.valueOf(minUsers) + " - " + String.valueOf(maxUsers));
            hangoutString = hangoutString.concat("\n");
        } else {
            hangoutString = hangoutString.concat("Members Required: ");
            hangoutString = hangoutString.concat(String.valueOf(minUsers));
            hangoutString = hangoutString.concat("\n");
        }

        hangoutString = hangoutString.concat("Current Members: ");
        int i;
        for (i = 0; i < this.users.size() - 1; i++) {
            hangoutString = hangoutString.concat(this.users.get(i) + ", ");
        }
        Log.d("USERS", this.users.get(i));
        hangoutString = hangoutString.concat(this.users.get(i));

        return hangoutString;
    }

    public String getCreator() {
        return this.creator;
    }

    public String getName() {
        return this.name;
    }

    public String getStartTime() {
        String startTimeString = "";
        startTimeString = startTimeString.concat(String.valueOf(startTime.get(Calendar.HOUR_OF_DAY)));
        startTimeString = startTimeString.concat(":");
        startTimeString = startTimeString.concat(String.valueOf(startTime.get(Calendar.MINUTE)));
        return startTimeString;
    }

    public String getEndTime() {
        String endTimeString = "";
        endTimeString = endTimeString.concat(String.valueOf(endTime.get(Calendar.HOUR_OF_DAY)));
        endTimeString = endTimeString.concat(":");
        endTimeString = endTimeString.concat(String.valueOf(endTime.get(Calendar.MINUTE)));
        return endTimeString;
    }

    public String getDate() {
        Calendar date = Calendar.getInstance();
        String dateString = String.valueOf(date.get(Calendar.YEAR));
        dateString = dateString.concat("-");
        dateString = dateString.concat(String.valueOf(date.get(Calendar.MONTH)));
        dateString = dateString.concat("-");
        dateString = dateString.concat(String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
        return dateString;
    }

    public int getMinUsers() {
        return this.minUsers;
    }

    public int getMaxUsers() {
        return this.maxUsers;
    }

    public String getUsers() {
        String usersString = "";
        for (int i = 0; i < this.googleIDs.size(); i++) {
            usersString = usersString.concat(this.googleIDs.get(i));
            usersString = usersString.concat(", ");
        }
        return usersString;
    }
}
