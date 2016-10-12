package cs307.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CommunityActivity extends AppCompatActivity {
    public static String EXTRA_TITLE = "com.cs307.butterfly.TITLE";
    public static SpannableString EXTRA_EVENTS = new SpannableString("com.cs307.butterfly.EVENTS");
    public static int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
    public static Date date;
    public static ArrayList<SpannableString> eventsButtons = new ArrayList<>();
    private Community community;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        this.community = new Community();
        Calendar date = Calendar.getInstance();
        for (int i = 0; i < 24; i++) {
            Calendar calendarClone = (Calendar) date.clone();
            CommunityEvent event = new CommunityEvent(calendarClone, i, 15, String.valueOf(i));
            community.addEvent(event);
        }

        final EventCalendarView ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
        ecv.setEvents(community.getCommunityEvents());
        ecv.setEventHandler(new EventCalendarView.EventHandler() {
            @Override
            public void onClick(Date date) {
                CommunityActivity.date = date;
                openEvent(ecv);
            }
        });
    }

    public void openEvent(View view) {
        String dateString = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
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

        for (int i = 0; i < community.getCommunityEvents().size(); i++) {
            CommunityEvent event = community.getCommunityEvents().get(i);
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
