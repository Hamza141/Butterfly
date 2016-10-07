package cs307.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CommunityActivity extends AppCompatActivity {
    public final static String EXTRA_DATE = "com.cs307.butterfly.DATE";
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

        EventCalendarView ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
        ecv.setEvents(community.getCommunityEvents());
        ecv.setEventHandler(new EventCalendarView.EventHandler() {
            @Override
            public void onClick(Date date) {
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(date);
                for (int i = 0; i < community.getCommunityEvents().size(); i++) {
                    if (community.getCommunityEvents().get(i).getDate().get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR) &&
                            community.getCommunityEvents().get(i).getDate().get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR)) {
                        Toast.makeText(CommunityActivity.this, community.getCommunityEvents().get(i).toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void openEvent(View view) {

        EventCalendarView ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
        Calendar dateTapped = ecv.getCurrentDate();
        String dateString = "";
        int month = dateTapped.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
                dateString = "January ";
                break;
            case Calendar.FEBRUARY:
               dateString = "February ";
                break;
            case Calendar.MARCH:
                dateString = "March ";
                break;
            case Calendar.APRIL:
                dateString = "April ";
                break;
            case Calendar.MAY:
                dateString = "May ";
                break;
            case Calendar.JUNE:
                dateString = "June ";
                break;
            case Calendar.JULY:
                dateString = "July ";
                break;
            case Calendar.AUGUST:
                dateString = "August ";
                break;
            case Calendar.SEPTEMBER:
                dateString = "September ";
                break;
            case Calendar.OCTOBER:
                dateString = "October ";
                break;
            case Calendar.NOVEMBER:
                dateString = "November ";
                break;
            case Calendar.DECEMBER:
                dateString = "December ";
                break;
        }

        String dayOfMonth = ((TextView) view).getText().toString();
        dateString = dateString.concat(dayOfMonth);

        Log.d("openEvent", dateString);
        Intent intent = new Intent(this, EventsActivity.class);
        intent.putExtra(EXTRA_DATE, dateString);
        startActivity(intent);
    }
}
