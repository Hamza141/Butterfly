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