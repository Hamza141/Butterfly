package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

public class EventsActivity extends AppCompatActivity {
    final Context context = this;

    private String result;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        String dateString = "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(CalendarActivity.date);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        dateString = dateString.concat(String.valueOf(month + 1));
        dateString = dateString.concat("/");
        dateString = dateString.concat(String.valueOf(dayOfMonth));
        dateString = dateString.concat("/");
        dateString = dateString.concat(String.valueOf(year));

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(CalendarActivity.date);
        if (CalendarActivity.isUser) {
            for (int i = 0; i < PersonalCalendarActivity.userEvents.size(); i++) {
                if (PersonalCalendarActivity.userEvents.get(i).getDate().get(Calendar.DAY_OF_YEAR) == calendar1.get(Calendar.DAY_OF_YEAR) &&
                        PersonalCalendarActivity.userEvents.get(i).getDate().get(Calendar.YEAR) == calendar1.get(Calendar.YEAR)) {
                    addButton(PersonalCalendarActivity.userEvents.get(i).getName());
                }
            }
            PersonalCalendarActivity.ecv.setEvents(PersonalCalendarActivity.userEvents);
        }
        else {
            for (int i = 0; i < CalendarActivity.community.getCommunityEvents().size(); i++) {
                if (CalendarActivity.community.getCommunityEvents().get(i).getDate().get(Calendar.DAY_OF_YEAR) == calendar1.get(Calendar.DAY_OF_YEAR) &&
                        CalendarActivity.community.getCommunityEvents().get(i).getDate().get(Calendar.YEAR) == calendar1.get(Calendar.YEAR)) {
                    addButton(CalendarActivity.community.getCommunityEvents().get(i).getName());
                    Log.d("addButton", CalendarActivity.community.getCommunityEvents().get(i).getName());
                }
            }
            CalendarActivity.ecv.setEvents(CalendarActivity.community.getCommunityEvents());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(dateString);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab1);
        fab.setImageBitmap(textAsBitmap("+", 40, Color.WHITE));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEvent();
            }
        });

        if (CalendarActivity.isUser) {
            fab.hide();
        }
    }

    public void addEvent() {
        final Dialog dialog = new Dialog(EventsActivity.this);
        dialog.setContentView(R.layout.dialog2);
        dialog.setTitle("Title");

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        b = (Button) dialog.findViewById(R.id.ok1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Socket[] socket = new Socket[1];
                final OutputStream[] outputStream = new OutputStream[1];
                final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                final JSONObject object = new JSONObject();

                EditText nameEdit = (EditText) dialog.findViewById(R.id.editTextDialogUserInput);
                final String name = nameEdit.getText().toString();

                EditText placeEdit = (EditText) dialog.findViewById(R.id.editTextDialogUserInput11);
                final String place = placeEdit.getText().toString();

                EditText descriptionEdit = (EditText) dialog.findViewById(R.id.editTextDialogUserInput222);
                final String description = descriptionEdit.getText().toString();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(CalendarActivity.date);

                TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePicker6);
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                String time = String.valueOf(hour);
                time = time.concat(":");
                time = time.concat(String.valueOf(minute));
                final String finalTime = time;
                Log.d("EventTime", finalTime);

                CommunityEvent event = new CommunityEvent(calendar, name, time, place, description);
                final CommunityEvent finalEvent = event;
                CalendarActivity.community.addEvent(event);
                dialog.dismiss();
                result = name;
                addButton();
                CalendarActivity.ecv.setEvents(CalendarActivity.community.getCommunityEvents());

                if (MainActivity.server) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket[0] = new Socket(MainActivity.ip, 3300);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                object.put("function", "addEvent");
                                object.put("communityName", CalendarActivity.community.getName());
                                object.put("eventName", name);
                                object.put("eventTime", finalTime);
                                object.put("date", finalEvent.getDateForServer());
                                object.put("description", description);
                                object.put("locationName", place);
                                dataOutputStream[0].writeUTF(object.toString());
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
        dialog.show();
    }

    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public void addButton() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll2);
        final Button b = new Button(this);
        b.setText(result);
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need
        b.setLayoutParams(lp);
        ll.addView(b);
        final Intent intent = new Intent(this, GroupActivity.class);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventViewerActivity.name = b.getText().toString();
                Intent intent1 = new Intent(context, EventViewerActivity.class);
                startActivity(intent1);
            }
        });

    }

    public void addButton(String name) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll2);
        final Button b = new Button(this);
        b.setText(name);
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need
        b.setLayoutParams(lp);
        ll.addView(b);
        final Intent intent = new Intent(this, GroupActivity.class);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventViewerActivity.name = b.getText().toString();
                Intent intent1 = new Intent(context, EventViewerActivity.class);
                startActivity(intent1);
            }
        });

    }

}
