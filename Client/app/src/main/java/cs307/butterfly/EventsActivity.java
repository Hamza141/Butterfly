package cs307.butterfly;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = CalendarActivity.EXTRA_TITLE;
        CharSequence titleSequence = title.subSequence(0, title.length());
        CollapsingToolbarLayout header = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        header.setTitle(titleSequence);

        LinearLayout linear = (LinearLayout) findViewById(R.id.linear);
        int i;
        for (i = 0; i < CalendarActivity.eventsButtons.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            Button btn = new Button(this);
            btn.setId(i);
            final int id_ = btn.getId();
            SpannableString eventButton = CalendarActivity.eventsButtons.get(i);
            CharSequence eventButtonSequence = eventButton.subSequence(0, eventButton.length());
            btn.setText(eventButtonSequence);
            linear.addView(btn, params);
            Button btn1 = ((Button) findViewById(id_));
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(),
                            "Button clicked index = " + id_, Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
        CalendarActivity.eventsButtons.clear();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        Button btn = new Button(this);
        btn.setId(i);
        final int id_ = btn.getId();
        String addString = "Add new event";
        CharSequence addSequence = addString.subSequence(0, addString.length());
        btn.setText(addSequence);
        linear.addView(btn, params);
        Button btn1 = ((Button) findViewById(id_));
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),
                        "Button clicked index = " + id_, Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }
}
