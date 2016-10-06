package cs307.butterfly;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CommunityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        EventCalendarView ecv = ((EventCalendarView) findViewById(R.id.calendar_view));
        ecv.setEventHandler(new EventCalendarView.EventHandler() {
            @Override
            public void onDayLongPress(Date date) {
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(CommunityActivity.this, df.format(date), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
