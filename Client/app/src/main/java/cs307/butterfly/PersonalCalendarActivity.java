package cs307.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.EventLog;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PersonalCalendarActivity extends AppCompatActivity {
    public static String EXTRA_TITLE = "com.cs307.butterfly.TITLE";
    public static ArrayList<SpannableString> eventsButtons = new ArrayList<>();
    public static EventCalendarView ecv;
    public static ArrayList<CommunityEvent> userEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        CalendarActivity.isUser = true;

        //get current user's communities' events from server
        final Socket[] socket = new Socket[1];
        final OutputStream[] outputStream = new OutputStream[1];
        final InputStream[] inputStream = new InputStream[1];
        final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
        final DataInputStream[] dataInputStream = new DataInputStream[1];
        final JSONObject object = new JSONObject();

        userEvents = new ArrayList<>();
        if (MainActivity.server) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket[0] = new Socket(MainActivity.ip, 3300);
                        outputStream[0] = socket[0].getOutputStream();
                        dataOutputStream[0] = new DataOutputStream(outputStream[0]);
                        object.put("function", "getUserCommunityEvents");
                        object.put("googleID", MainActivity.googleID);
                        dataOutputStream[0].writeUTF(object.toString());

                        //now receive all the events of the community from the server
                        inputStream[0] = socket[0].getInputStream();
                        dataInputStream[0] = new DataInputStream(inputStream[0]);

                        //get number of events to read
                        int numCommunities = Integer.parseInt(dataInputStream[0].readUTF());
                        Log.d("numCommunities", String.valueOf(numCommunities));

                        //read each event and make new CommunityEvent to add to community
                        for (int i = 0; i < numCommunities; i++) {
                            String communityName = dataInputStream[0].readUTF();
                            int numEvents = Integer.parseInt(dataInputStream[0].readUTF());
                            Calendar calendar = Calendar.getInstance();
                            for (int j = 0; j < numEvents; j++) {
                                JSONObject jsonEvent = new JSONObject(dataInputStream[0].readUTF());
                                String date= jsonEvent.getString("date");
                                String[] split = date.split("-");
                                String year = split[0];
                                String month = split[1];
                                String day = split[2];
                                calendar.set(Calendar.YEAR, Integer.valueOf(year));
                                calendar.set(Calendar.MONTH, Integer.valueOf(month) - 1);
                                calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
                                String eventName = jsonEvent.getString("eventName");
                                String eventTime = jsonEvent.getString("time");
                                String place = jsonEvent.getString("locationName");
                                String description = jsonEvent.getString("description");
                                CommunityEvent event = new CommunityEvent(calendar, eventName, eventTime, place, description);
                                userEvents.add(event);
                                Log.d("event added", eventName);
                            }
                        }

                        outputStream[0].close();
                        dataOutputStream[0].close();
                        inputStream[0].close();
                        dataInputStream[0].close();
                        socket[0].close();

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            android.os.SystemClock.sleep(1000);
        }
        ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
        ecv.setEvents(userEvents);
        ecv.setEventHandler(new EventCalendarView.EventHandler() {
            @Override
            public void onClick(Date date) {
                CalendarActivity.date = date;
                openEvent(ecv);
            }
        });
    }

    public void openEvent(View view) {
        String dateString = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(CalendarActivity.date);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        dateString = dateString.concat(String.valueOf(month + 1));
        dateString = dateString.concat("/");
        dateString = dateString.concat(String.valueOf(dayOfMonth));
        dateString = dateString.concat("/");
        dateString = dateString.concat(String.valueOf(year));

        Log.d("openEvent", dateString);
        EXTRA_TITLE = dateString;
        Intent intent = new Intent(this, EventsActivity.class);

        int nameLength;
        int timeLength;

        for (int i = 0; i < userEvents.size(); i++) {
            CommunityEvent event = userEvents.get(i);
            if (event.getDate().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
                    event.getDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                String eventString = "";
                eventString = eventString.concat(event.getName());
                nameLength = event.getName().length();
                eventString = eventString.concat(event.getStartTime());
                timeLength = event.getStartTime().length();
                SpannableString eventSpannable = new SpannableString(eventString);
                eventSpannable.setSpan(new TextAppearanceSpan(this, android.R.style.TextAppearance_DeviceDefault_Large), 0, nameLength, 0);
                eventSpannable.setSpan(new TextAppearanceSpan(this, android.R.style.TextAppearance_DeviceDefault_Medium), nameLength, nameLength + timeLength, 0);
                eventsButtons.add(eventSpannable);
            }
        }
        startActivity(intent);
    }
}