package cs307.butterfly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CommunityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        Community community = new Community();
        Calendar date = Calendar.getInstance();
        for (int i = 1; i < 10; i++) {
            Calendar calendarClone = (Calendar) date.clone();
            CommunityEvent event = new CommunityEvent(calendarClone, 12, 30, String.valueOf(i));
            community.addEvent(event);
            date.add(Calendar.DATE, 5);
        }

        EventCalendarView ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
        ecv.setEvents(community.getCommunityEvents());
        ecv.setEventHandler(new EventCalendarView.EventHandler() {
            @Override
            public void onClick(Date date) {
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(date);
                Toast.makeText(CommunityActivity.this, dateCalendar.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
