package cs307.butterfly;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class EventViewerActivity extends AppCompatActivity {
    public static String name;
    public static CommunityEvent event;
    public static EventsActivity previousActivity;
    public static String rsvpd;
    public static boolean checkInTime;
    public static ArrayList<String> userNames;
    public static ArrayList<String> googleIDs;
    public static Random randomno = new Random();
    Dialog dialog;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);
        rsvpd = "0";
        checkInTime = false;
        userNames = new ArrayList<>();
        googleIDs = new ArrayList<>();

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

        Calendar currentTime = Calendar.getInstance();
        if (event.getDate().before(currentTime)) {
            checkInTime = true;
        }

        ImageButton usersButton = (ImageButton) findViewById(R.id.view_all);
        usersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(EventViewerActivity.this);
                dialog.setContentView(R.layout.dialog5);
                dialog.setTitle("Title");

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                if (dialog.getWindow() != null) {
                    lp.copyFrom(dialog.getWindow().getAttributes());
                }
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                dialog.getWindow().setAttributes(lp);
                //  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

                LinearLayout vv = (LinearLayout) dialog.findViewById(R.id.linearfriend);
                vv.removeAllViews();
                userNames.clear();
                googleIDs.clear();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket(MainActivity.ip, 3300);
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = socket.getInputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                            DataInputStream dataInputStream = new DataInputStream(inputStream);

                            JSONObject object = new JSONObject();
                            if (!checkInTime) {
                                object.put("function", "getrsvp");
                            }
                            else {
                                object.put("function", "getCheckIns");
                            }
                            object.put("communityName", CalendarActivity.community.getName());
                            object.put("eventName", event.getName());
                            dataOutputStream.writeUTF(object.toString());

                            String input = dataInputStream.readUTF();
                            Log.d("INPUT", input);

                            String input2 = dataInputStream.readUTF();
                            Log.d("INPUT", input2);
                            if (!input.equals("") && !input2.equals("")) {
                                String[] names = input.split(", ");
                                String[] ids = input2.split(", ");
                                for (int i = 0; i < names.length; i++) {
                                    Log.d("NAME", names[i]);
                                    userNames.add(names[i]);
                                    googleIDs.add(ids[i]);
                                }
                            }

                            //addButton(userName);

                            socket.close();
                            outputStream.close();
                            dataOutputStream.close();
                            inputStream.close();
                            dataInputStream.close();

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();

                android.os.SystemClock.sleep(500);


                for (int i = 0; i < userNames.size(); i++) {
                    Log.d("ADD BUTTON", userNames.get(i));
                    addButton(userNames.get(i));
                }

                dialog.show();

                ImageButton check = (ImageButton) dialog.findViewById(R.id.check);

                check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        Button rsvpButton = (Button) findViewById(R.id.rsvpButton);
        if (checkInTime) {
            rsvpButton.setText("Check In");
            rsvpButton.setTextSize(18);
        }
        else {
            rsvpButton.setText("RSVP");
            rsvpButton.setTextSize(24);
        }
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
                                inputStream[0] = socket[0].getInputStream();
                                dataInputStream[0] = new DataInputStream(inputStream[0]);

                                if (!checkInTime) {
                                    object1.put("function", "rsvpCheck");
                                }
                                else {
                                    object1.put("function", "checkInCheck");
                                }
                                object1.put("communityName", CalendarActivity.community.getName());
                                object1.put("eventName", name);
                                object1.put("googleID", MainActivity.googleID);
                                dataOutputStream[0].writeUTF(object1.toString());

                                android.os.SystemClock.sleep(100);
                                rsvpd = dataInputStream[0].readUTF();

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

                if (rsvpd.equals("0")) {
                    if (MainActivity.server) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    socket[0] = new Socket(MainActivity.ip, 3300);
                                    outputStream[0] = socket[0].getOutputStream();
                                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                    if (!checkInTime) {
                                        object2.put("function", "rsvpEvent");
                                    } else {
                                        object2.put("function", "checkIn");
                                    }
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
                else if (rsvpd.equals("1")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EventViewerActivity.this);
                    if (!checkInTime) {
                        builder.setMessage("You are already RSVP'd to this event. Would you like to remove your RSVP?");
                    } else {
                        builder.setMessage("You have already checked into this event. Would you like to check out?");
                    }
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
                                    if (!checkInTime) {
                                        object3.put("function", "rsvpEventRemove");
                                    } else {
                                        object3.put("function", "removeCheckIn");
                                    }
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
        if (MainActivity.iModerator.contains(CalendarActivity.community.getName())) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_event_viewer, menu);
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_delete:
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
                return true;

            case R.id.option_edit:
                editEvent();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void addButton(final String namex) {
        LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.linearfriend);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT, 220);
        params.setMargins(0, 0, 0, 8);

        Button b1 = new Button(this);
        // buttons.add(b1);
        b1.setLayoutParams(params);
        b1.setBackgroundColor(Color.rgb(255 - randomno.nextInt(50), 255 - randomno.nextInt(30), 255));
        b1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person, 0, 0, 0);
        b1.setPadding(150, 0, 0, 0);
        b1.setText(namex);
        b1.setTextSize(18);
        b1.setTextColor(Color.rgb(0, 0, 0));

        ll.addView(b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupActivity.theguy = namex;
                Intent intent = new Intent(EventViewerActivity.this, UserProfile.class);
                int index = userNames.indexOf(namex);
                intent.putExtra("googleID", googleIDs.get(index));
                startActivity(intent);
            }
        });
    }
}


