package cs307.butterfly;

import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;

public class EventViewerActivity extends AppCompatActivity {
    public static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(CalendarActivity.date);
        for (int i = 0; i < CalendarActivity.community.getCommunityEvents().size(); i++) {
            if (CalendarActivity.community.getCommunityEvents().get(i).getDate().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
                    CalendarActivity.community.getCommunityEvents().get(i).getDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                if (name.equals(CalendarActivity.community.getCommunityEvents().get(i).getName())) {
                    String eventString = CalendarActivity.community.getCommunityEvents().get(i).toString();
                    CharSequence eventSequence = eventString.subSequence(0, eventString.length());
                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText(eventSequence);
                }
            }
        }
    }
}
