package cs307.butterfly;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.widget.TextView;

import static android.support.design.R.attr.expandedTitleGravity;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = CommunityActivity.EXTRA_TITLE;
        CharSequence titleSequence = title.subSequence(0, title.length());
        CollapsingToolbarLayout header = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        header.setTitle(titleSequence);
        SpannableString events = CommunityActivity.EXTRA_EVENTS;
        CharSequence eventsSequence = events.subSequence(0, events.length());
        TextView eventsText = (TextView) findViewById(R.id.events_text);
        eventsText.setText(eventsSequence);
    }
}
