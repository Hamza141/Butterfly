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

        final EventCalendarView ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
        ecv.setEventHandler(new EventCalendarView.EventHandler() {
            @Override
            public void onClick(Date date) {
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(date);
                ArrayList<CommunityEvent> events = ecv.getEvents();
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i).getTime().get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
                            && events.get(i).getTime().get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR)) {
                        Toast.makeText(CommunityActivity.this, events.get(i).getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
