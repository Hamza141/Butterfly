package cs307.butterfly;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

public class EventViewerActivity extends AppCompatActivity {
    public static String name;
    public static CommunityEvent event;
    public static EventsActivity previousActivity;
    public static String checkedIn;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);
        checkedIn = "0";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(CalendarActivity.date);
        for (int i = 0; i < CalendarActivity.community.getCommunityEvents().size(); i++) {
            if (CalendarActivity.community.getCommunityEvents().get(i).getDate().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
                    CalendarActivity.community.getCommunityEvents().get(i).getDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                if (name.equals(CalendarActivity.community.getCommunityEvents().get(i).getName())) {

                    String eventString = CalendarActivity.community.getCommunityEvents().get(i).getDescription();
                    eventString = eventString.concat(CalendarActivity.community.getCommunityEvents().get(i).getStartTime());
                    eventString = eventString.concat(CalendarActivity.community.getCommunityEvents().get(i).getPlace());

                    CharSequence eventSequence = eventString.subSequence(0, eventString.length());
                    TextView textView = (TextView) findViewById(R.id.eventDetails);
                    textView.setText(eventSequence);
                    getSupportActionBar().setTitle(CalendarActivity.community.getCommunityEvents().get(i).getName());
                    event = CalendarActivity.community.getCommunityEvents().get(i);
                }
            }
        }

        Button rsvpButton = (Button) findViewById(R.id.rsvpButton);
        rsvpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Socket[] socket = new Socket[1];
                final OutputStream[] outputStream = new OutputStream[1];
                final InputStream[] inputStream = new InputStream[1];
                final DataInputStream[] dataInputStream = new DataInputStream[1];
                final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                final JSONObject object1 = new JSONObject();
                final JSONObject object2 = new JSONObject();
                final JSONObject object3 = new JSONObject();

                if (MainActivity.server) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket[0] = new Socket(MainActivity.ip, 3300);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                object1.put("function", "rsvpCheck");
                                object1.put("communityName", CalendarActivity.community.getName());
                                object1.put("eventName", name);
                                object1.put("googleID", MainActivity.googleID);
                                dataOutputStream[0].writeUTF(object1.toString());

                                inputStream[0] = socket[0].getInputStream();
                                dataInputStream[0] = new DataInputStream(inputStream[0]);

                                checkedIn = dataInputStream[0].readUTF();

                                socket[0].close();
                                outputStream[0].close();
                                dataOutputStream[0].close();
                                inputStream[0].close();
                                dataInputStream[0].close();

                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                android.os.SystemClock.sleep(500);

                if (checkedIn.equals("0")) {
                    if (MainActivity.server) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    socket[0] = new Socket(MainActivity.ip, 3300);
                                    outputStream[0] = socket[0].getOutputStream();
                                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                    object2.put("function", "rsvpEvent");
                                    object2.put("eventName", name);
                                    object2.put("communityName", CalendarActivity.community.getName());
                                    object2.put("googleID", MainActivity.googleID);

                                    dataOutputStream[0].writeUTF(object2.toString());
                                    socket[0].close();
                                    outputStream[0].close();
                                    dataOutputStream[0].close();

                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                else if (checkedIn.equals("1")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
                    builder.setMessage("You are already RSVP'd to this event. Would you like to remove your RSVP?");
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (MainActivity.server) {
                                try {
                                    object3.put("function", "rsvpEventRemove");
                                    object3.put("eventName", name);
                                    object3.put("communityName", CalendarActivity.community.getName());
                                    object3.put("googleID", MainActivity.googleID);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                MainActivity.connectionSend(object3);
                            }
                        }
                    });
                    AlertDialog cancelRsvp = builder.create();
                    cancelRsvp.show();
                }
            }
        });
    }

    public void editEvent() {
        final Dialog dialog = new Dialog(EventViewerActivity.this);
        dialog.setContentView(R.layout.dialog2);
        dialog.setTitle("Title");

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            lp.copyFrom(dialog.getWindow().getAttributes());
        }
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
                //noinspection deprecation
                int hour = timePicker.getCurrentHour();
                //noinspection deprecation
                int minute = timePicker.getCurrentMinute();
                String time = String.valueOf(hour);
                time = time.concat(":");
                time = time.concat(String.valueOf(minute));
                final String finalTime = time;
                Log.d("EventTime", finalTime);

                EventViewerActivity.event.editInfo(name, place, description, time);
                final CommunityEvent finalEvent = event;
                dialog.dismiss();
                CalendarActivity.ecv.setEvents(CalendarActivity.community.getCommunityEvents());

                if (MainActivity.server) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket[0] = new Socket(MainActivity.ip, 3300);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                object.put("function", "editEvent");
                                object.put("communityName", CalendarActivity.community.getName());
                                object.put("eventName", name);
                                object.put("time", finalEvent.getTimeString());
                                object.put("date", finalEvent.getDateForServer());
                                object.put("description", description);
                                object.put("locationName", place);
                                dataOutputStream[0].writeUTF(object.toString());

                                socket[0].close();
                                outputStream[0].close();
                                dataOutputStream[0].close();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_community, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                CharSequence[] items = new CharSequence[2];
                items[0] = "Edit this event";
                items[1] = "Delete this event";
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            //edit clicked
                            editEvent();
                        }
                        else if (i == 1) {
                            //delete clicked
                            final AlertDialog.Builder builder1 = new AlertDialog.Builder(EventViewerActivity.this);
                            builder1.setMessage("Are you sure you want to delete this event?");
                            builder1.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("BUTTON", "delete");
                                    final Socket[] socket = new Socket[1];
                                    final OutputStream[] outputStream = new OutputStream[1];
                                    final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                                    final JSONObject object = new JSONObject();

                                    if (MainActivity.server) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    socket[0] = new Socket(MainActivity.ip, 3300);
                                                    outputStream[0] = socket[0].getOutputStream();
                                                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                                    object.put("function", "deleteEvent");
                                                    object.put("communityName", CalendarActivity.community.getName());
                                                    object.put("eventName", event.getName());
                                                    dataOutputStream[0].writeUTF(object.toString());
                                                } catch (JSONException | IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    }
                                    for (int j = 0; j < CalendarActivity.community.getCommunityEvents().size(); j++) {
                                        if (CalendarActivity.community.getCommunityEvents().get(j).getName().equals(event.getName())) {
                                            CalendarActivity.community.getCommunityEvents().remove(j);
                                        }
                                    }
                                    EventViewerActivity.super.finish();
                                    previousActivity.finish();
                                }
                            });
                            builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.d("BUTTON", "cancel");
                                }
                            });
                            AlertDialog deleteCheck = builder1.create();
                            deleteCheck.show();
                        }
                    }
                });
                AlertDialog eventOptions = builder.create();
                eventOptions.show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}

/*
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
             //   EditText timeEdit=(EditText)dialog.findViewById(R.id.editTextDialogUserInputTime);
             //   String time=timeEdit.getText().toString();
                EditText placeEdit=(EditText)dialog.findViewById(R.id.editTextDialogUserInput11);
                String place=placeEdit.getText().toString();
                EditText descriptionEdit=(EditText)dialog.findViewById(R.id.editTextDialogUserInput222);
                String description=descriptionEdit.getText().toString();
                EventViewerActivity.event.editInfo(name, place, description, "10:30 am");
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
*/

