package cs307.butterfly;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class HangoutViewerActivity extends AppCompatActivity {

    public static ArrayList<Hangout> hangouts;
    public static int currentHangout;
    public static boolean hangoutJoined;
    public static int index;

    public static String hangoutName;
    public static int minUsers;
    public static int maxUsers;
    public static int startHour;
    public static int startMinute;
    public static int endHour;
    public static int endMinute;
    public static ImageButton hangoutsRight;
    public static ImageButton hangoutsLeft;
    public static TextView hangoutText;
    public static Button joinHangout;
    public static Button leaveHangout;
    public static FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangout_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(CalendarActivity.community.getName() + " Hangouts");
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        joinHangout = (Button) findViewById(R.id.joinHangout);
        hangoutsRight = (ImageButton) findViewById(R.id.hangoutsRight);
        hangoutsLeft = (ImageButton) findViewById(R.id.hangoutsLeft);
        hangoutText = (TextView) findViewById(R.id.hangoutText);
        leaveHangout = (Button) findViewById(R.id.leaveHangout);
        leaveHangout.setVisibility(View.GONE);

        hangouts = new ArrayList<>();
        hangoutJoined = false;
        index = -1;

        for (int m = 0; m < MainActivity.hangoutsJoined.size(); m++) {
            Log.d("HANGOUT JOINED", MainActivity.hangoutsJoined.get(m));
        }

        for (int k = 0; k < MainActivity.hangoutsJoined.size(); k++) {
            String[] hangoutSplit = MainActivity.hangoutsJoined.get(k).split("_");
            if (hangoutSplit[0].equals(CalendarActivity.community.getName())) {
                index = k;
                hangoutJoined = true;
            }
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageBitmap(textAsBitmap("+", 40, Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HangoutViewerActivity.this);
                builder.setTitle("Name your hangout");
                final EditText editHangoutName = new EditText(HangoutViewerActivity.this);
                editHangoutName.setHint("Hangout name");
                builder.setView(editHangoutName);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hangoutName = editHangoutName.getText().toString();
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(HangoutViewerActivity.this);
                        builder1.setTitle("Set minimum number of members");
                        final NumberPicker minUsersPicker = new NumberPicker(HangoutViewerActivity.this);
                        minUsersPicker.setMinValue(2);
                        minUsersPicker.setMaxValue(100);
                        minUsersPicker.setValue(2);
                        builder1.setView(minUsersPicker);
                        builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                minUsers = minUsersPicker.getValue();
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(HangoutViewerActivity.this);
                                builder2.setTitle("Set maximum number of members");
                                final NumberPicker maxUsersPicker = new NumberPicker(HangoutViewerActivity.this);
                                maxUsersPicker.setMinValue(minUsers);
                                maxUsersPicker.setMaxValue(100);
                                maxUsersPicker.setValue(minUsers);
                                builder2.setView(maxUsersPicker);
                                builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        maxUsers = maxUsersPicker.getValue();
                                        final Calendar currentDate = Calendar.getInstance();
                                        TimePickerDialog startTimePicker = new TimePickerDialog(HangoutViewerActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                                startHour = hourOfDay;
                                                startMinute = minute;
                                                TimePickerDialog endTimePicker = new TimePickerDialog(HangoutViewerActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                                    @Override
                                                    public void onTimeSet(TimePicker timePicker, int hourOfDay2, int minute2) {
                                                        if ((hourOfDay2 - startHour == 1 && minute2 - startMinute < 0) || hourOfDay2 - startHour < 1) {
                                                            AlertDialog.Builder builder3 = new AlertDialog.Builder(HangoutViewerActivity.this);
                                                            builder3.setMessage("End time must be at least an hour after start time");
                                                            builder3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                }
                                                            });
                                                            AlertDialog warning = builder3.create();
                                                            warning.show();
                                                        } else {
                                                            endHour = hourOfDay2;
                                                            endMinute = minute2;
                                                            addHangout();
                                                        }
                                                    }
                                                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
                                                endTimePicker.setTitle("Set end time");
                                                endTimePicker.show();
                                            }
                                        }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
                                        startTimePicker.setTitle("Set start time");
                                        startTimePicker.show();
                                    }
                                });
                                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                AlertDialog maxUsersDialog = builder2.create();
                                maxUsersDialog.show();
                            }
                        });
                        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog minUsersDialog = builder1.create();
                        minUsersDialog.show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog nameDialog = builder.create();
                nameDialog.show();

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(MainActivity.ip, MainActivity.port);
                    OutputStream outputStream = socket.getOutputStream();
                    InputStream inputStream = socket.getInputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    JSONObject object = new JSONObject();
                    object.put("function", "getHangouts");
                    object.put("communityName", CalendarActivity.community.getName());
                    dataOutputStream.writeUTF(object.toString());

                    Integer numHangouts = Integer.parseInt(dataInputStream.readUTF());

                    for (int i = 0; i < numHangouts; i++) {
                        JSONObject jsonHangout = new JSONObject(dataInputStream.readUTF());
                        String hangoutName = jsonHangout.getString("hangoutName");
                        String startTime = jsonHangout.getString("startTime");
                        String endTime = jsonHangout.getString("endTime");
                        String googleIDs = jsonHangout.getString("listIds");
                        String userNames = jsonHangout.getString("listNames");
                        String creator = jsonHangout.getString("creator");
                        int minUsers = jsonHangout.getInt("minUsers");
                        int maxUsers = jsonHangout.getInt("maxUsers");

                        String[] googleIDsArray = googleIDs.split(", ");
                        String[] userNamesArray = userNames.split(", ");
                        ArrayList<String> googleIdsArrayList = new ArrayList<>();
                        ArrayList<String> userNamesArrayList = new ArrayList<>();
                        for (int j = 0; j < googleIDsArray.length; j++) {
                            googleIdsArrayList.add(googleIDsArray[j]);
                            userNamesArrayList.add(userNamesArray[j]);
                        }
                        Hangout hangout = new Hangout(minUsers, maxUsers, hangoutName, startTime, endTime, creator, googleIdsArrayList, userNamesArrayList);
                        hangouts.add(hangout);

                    }

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

        hangoutsLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentHangout == 0) {
                    currentHangout = hangouts.size() - 1;
                } else {
                    currentHangout--;
                }
                hangoutText.setText(hangouts.get(currentHangout).toString());
                if (hangoutJoined) {
                    joinHangout.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                    String[] textSplit = hangoutText.getText().toString().split("\n");
                    String hangoutName = textSplit[0];
                    if (index != -1) {
                        String[] mainSplit = MainActivity.hangoutsJoined.get(index).split("_");
                        String mainName = mainSplit[1];
                        if (hangoutName.equals(mainName)) {
                            leaveHangout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });


        hangoutsRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentHangout == hangouts.size() - 1) {
                    currentHangout = 0;
                } else {
                    currentHangout++;
                }
                hangoutText.setText(hangouts.get(currentHangout).toString());
                if (hangoutJoined) {
                    joinHangout.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                    String[] textSplit = hangoutText.getText().toString().split("\n");
                    String hangoutName = textSplit[0];
                    if (index != -1) {
                        String[] mainSplit = MainActivity.hangoutsJoined.get(index).split("_");
                        String mainName = mainSplit[1];
                        if (hangoutName.equals(mainName)) {
                            leaveHangout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        joinHangout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket(MainActivity.ip, MainActivity.port);
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = socket.getInputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                            DataInputStream dataInputStream = new DataInputStream(inputStream);

                            JSONObject object = new JSONObject();
                            object.put("function", "addHangoutUser");
                            object.put("communityName", CalendarActivity.community.getName());
                            String[] textSplit = hangoutText.getText().toString().split("\n");
                            String hangoutName = textSplit[0];
                            object.put("hangoutName", hangoutName);
                            object.put("googleID", MainActivity.googleID);
                            dataOutputStream.writeUTF(object.toString());

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

                android.os.SystemClock.sleep(200);
                String newText = hangoutText.getText().toString();
                newText = newText.concat(", " + MainActivity.fullName);
                hangoutText.setText(newText);
                String[] newTextArray = newText.split("\n");
                String hangoutName = newTextArray[0];
                String hangoutJoinedString = CalendarActivity.community.getName();
                hangoutJoinedString = hangoutJoinedString.concat("_" + hangoutName);
                MainActivity.hangoutsJoined.add(hangoutJoinedString);
                index = MainActivity.hangoutsJoined.size() - 1;
                hangoutJoined = true;
                onResume();
            }
        });

        leaveHangout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket(MainActivity.ip, MainActivity.port);
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = socket.getInputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                            DataInputStream dataInputStream = new DataInputStream(inputStream);

                            JSONObject object = new JSONObject();
                            object.put("function", "leaveHangoutUser");
                            object.put("communityName", CalendarActivity.community.getName());
                            String[] textSplit = hangoutText.getText().toString().split("\n");
                            String hangoutName = textSplit[0];
                            object.put("hangoutName", hangoutName);
                            object.put("googleID", MainActivity.googleID);
                            dataOutputStream.writeUTF(object.toString());

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

                android.os.SystemClock.sleep(200);

                hangouts.clear();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket(MainActivity.ip, MainActivity.port);
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = socket.getInputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                            DataInputStream dataInputStream = new DataInputStream(inputStream);

                            JSONObject object = new JSONObject();
                            object.put("function", "getHangouts");
                            object.put("communityName", CalendarActivity.community.getName());
                            dataOutputStream.writeUTF(object.toString());

                            Integer numHangouts = Integer.parseInt(dataInputStream.readUTF());

                            for (int i = 0; i < numHangouts; i++) {
                                JSONObject jsonHangout = new JSONObject(dataInputStream.readUTF());
                                String hangoutName = jsonHangout.getString("hangoutName");
                                String startTime = jsonHangout.getString("startTime");
                                String endTime = jsonHangout.getString("endTime");
                                String googleIDs = jsonHangout.getString("listIds");
                                String userNames = jsonHangout.getString("listNames");
                                String creator = jsonHangout.getString("creator");
                                int minUsers = jsonHangout.getInt("minUsers");
                                int maxUsers = jsonHangout.getInt("maxUsers");

                                String[] googleIDsArray = googleIDs.split(", ");
                                String[] userNamesArray = userNames.split(", ");
                                ArrayList<String> googleIdsArrayList = new ArrayList<>();
                                ArrayList<String> userNamesArrayList = new ArrayList<>();
                                for (int j = 0; j < googleIDsArray.length; j++) {
                                    googleIdsArrayList.add(googleIDsArray[j]);
                                    userNamesArrayList.add(userNamesArray[j]);
                                }
                                Hangout hangout = new Hangout(minUsers, maxUsers, hangoutName, startTime, endTime, creator, googleIdsArrayList, userNamesArrayList);
                                hangouts.add(hangout);

                            }

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

                hangoutJoined = false;
                index = -1;
                String hangoutsJoinedString = CalendarActivity.community.getName();
                hangoutsJoinedString = hangoutsJoinedString.concat("_");
                String[] textSplit = hangoutText.getText().toString().split("\n");
                String hangoutName = textSplit[0];
                hangoutsJoinedString = hangoutsJoinedString.concat(hangoutName);
                for (int n = 0; n < MainActivity.hangoutsJoined.size(); n++) {
                    if (MainActivity.hangoutsJoined.get(n).equals(hangoutsJoinedString)) {
                        MainActivity.hangoutsJoined.remove(n);
                        break;
                    }
                }
                onResume();
            }
        });

        if (!hangouts.isEmpty()) {
            hangoutText.setText(hangouts.get(currentHangout).toString());
            if (hangoutJoined) {
                joinHangout.setVisibility(View.GONE);
                String[] textSplit = hangoutText.getText().toString().split("\n");
                String hangoutName = textSplit[0];
                if (index != -1) {
                    String[] mainSplit = MainActivity.hangoutsJoined.get(index).split("_");
                    String mainName = mainSplit[1];
                    if (hangoutName.equals(mainName)) {
                        leaveHangout.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            hangoutText.setText("No hangouts available");
            hangoutsRight.setVisibility(View.GONE);
            hangoutsLeft.setVisibility(View.GONE);
            joinHangout.setVisibility(View.GONE);
        }

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

    public void addHangout() {
        final Hangout hangout = new Hangout(minUsers, maxUsers, hangoutName, startHour, startMinute, endHour, endMinute, MainActivity.fullName, MainActivity.googleID);
        hangouts.add(hangout);
        Log.d("HANGOUT", hangout.toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(MainActivity.ip, MainActivity.port);
                    OutputStream outputStream = socket.getOutputStream();
                    InputStream inputStream = socket.getInputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    JSONObject object = new JSONObject();
                    object.put("function", "addHangout");
                    object.put("communityName", CalendarActivity.community.getName());
                    object.put("hangoutName", hangout.getName());
                    object.put("startTime", hangout.getStartTime());
                    object.put("endTime", hangout.getEndTime());
                    object.put("creator", hangout.getCreator());
                    object.put("date", hangout.getDate());
                    object.put("minUsers", hangout.getMinUsers());
                    object.put("maxUsers", hangout.getMaxUsers());
                    object.put("listAttendees", MainActivity.googleID);
                    dataOutputStream.writeUTF(object.toString());

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

        String hangoutJoinedString = CalendarActivity.community.getName();
        hangoutJoinedString = hangoutJoinedString.concat("_");
        hangoutJoinedString = hangoutJoinedString.concat(hangout.getName());
        MainActivity.hangoutsJoined.add(hangoutJoinedString);
        index = MainActivity.hangoutsJoined.size() - 1;
        hangoutJoined = true;
        onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hangouts.isEmpty()) {
            hangoutText.setText(hangouts.get(currentHangout).toString());
            hangoutsLeft.setVisibility(View.VISIBLE);
            hangoutsRight.setVisibility(View.VISIBLE);
            if (hangoutJoined) {
                joinHangout.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                String[] textSplit = hangoutText.getText().toString().split("\n");
                String hangoutName = textSplit[0];
                if (index != -1) {
                    String[] mainSplit = MainActivity.hangoutsJoined.get(index).split("_");
                    String mainName = mainSplit[1];
                    if (hangoutName.equals(mainName)) {
                        leaveHangout.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            hangoutText.setText("No hangouts available");
            fab.setVisibility(View.VISIBLE);
            leaveHangout.setVisibility(View.GONE);
            hangoutsRight.setVisibility(View.GONE);
            hangoutsLeft.setVisibility(View.GONE);
            joinHangout.setVisibility(View.GONE);
        }
    }
}
