package cs307.butterfly;

import android.app.Dialog;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class EventViewerActivity extends AppCompatActivity {
    public static String name;
    public static CommunityEvent event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setImageResource(R.drawable.edit);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editEvent();
            }
        });

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
                    getSupportActionBar().setTitle(CalendarActivity.community.getCommunityEvents().get(i).getName());
                    event = CalendarActivity.community.getCommunityEvents().get(i);
                }
            }
        }
    }

    public void editEvent(){
        final Dialog dialog = new Dialog(EventViewerActivity.this);
        dialog.setContentView(R.layout.dialog2);
        dialog.setTitle("Title");

        Button b = (Button) dialog.findViewById(R.id.ok1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEdit=(EditText)dialog.findViewById(R.id.editTextDialogUserInput);
                String name=nameEdit.getText().toString();
                EditText timeEdit=(EditText)dialog.findViewById(R.id.editTextDialogUserInputTime);
                String time=timeEdit.getText().toString();
                EditText placeEdit=(EditText)dialog.findViewById(R.id.editTextDialogUserInput11);
                String place=placeEdit.getText().toString();
                EditText descriptionEdit=(EditText)dialog.findViewById(R.id.editTextDialogUserInput222);
                String description=descriptionEdit.getText().toString();
                EventViewerActivity.event.editInfo(name, place, description, time);
                CharSequence eventSequence = event.toString().subSequence(0, event.toString().length());
                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(eventSequence);
                getSupportActionBar().setTitle(event.getName());
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
