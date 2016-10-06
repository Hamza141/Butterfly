package cs307.butterfly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommunityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        EventCalendarView ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
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
