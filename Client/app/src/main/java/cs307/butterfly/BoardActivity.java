package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class BoardActivity extends AppCompatActivity {

    public static ArrayList<String> messages;
    public static ArrayList<String> userNames;
    public static ArrayList<String> googleIDs;
    public static Random randomno = new Random();
    public static Crew crew;
    public static Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        messages = new ArrayList<>();
        userNames = new ArrayList<>();
        googleIDs = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(crew.getCrewName());
        setSupportActionBar(toolbar);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Log.d("BoardActivity", "created");

        final Socket[] socket = new Socket[1];
        final OutputStream[] outputStream = new OutputStream[1];
        final InputStream[] inputStream = new InputStream[1];
        final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
        final DataInputStream[] dataInputStream = new DataInputStream[1];
        final JSONObject object = new JSONObject();

        if (MainActivity.server) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                        outputStream[0] = socket[0].getOutputStream();
                        dataOutputStream[0] = new DataOutputStream(outputStream[0]);
                        object.put("function", "getCrewMessages");
                        object.put("communityName", CalendarActivity.community.getName());
                        object.put("idCrew", crew.getIdNumber());
                        object.put("crewName", crew.getCrewName());
                        dataOutputStream[0].writeUTF(object.toString());

                        inputStream[0] = socket[0].getInputStream();
                        dataInputStream[0] = new DataInputStream(inputStream[0]);

                        int numMessages = Integer.parseInt(dataInputStream[0].readUTF());

                        for (int i = 0; i < numMessages; i++) {
                            JSONObject jsonMessage = new JSONObject(dataInputStream[0].readUTF());
                            String message = (String) jsonMessage.get("message");
                            Log.d("MESSAGE", message);
                            messages.add(message);
                        }

                        outputStream[0].close();
                        dataOutputStream[0].close();
                        inputStream[0].close();
                        dataInputStream[0].close();
                        socket[0].close();

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            android.os.SystemClock.sleep(300);

        }

        for (int i = 0; i < messages.size(); i++) {
            addMessage(messages.get(i) + "\n");
        }

        ImageButton button = (ImageButton) findViewById(R.id.crewUsers);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = new Dialog(BoardActivity.this);
                dialog.setContentView(R.layout.dialog5);
                dialog.setTitle("Crew Members");

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                if (dialog.getWindow() != null) {
                    lp.copyFrom(dialog.getWindow().getAttributes());
                }
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                dialog.getWindow().setAttributes(lp);

                LinearLayout vv = (LinearLayout) dialog.findViewById(R.id.linearfriend);
                vv.removeAllViews();
                userNames.clear();
                googleIDs.clear();

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
                            object.put("function", "getCrewUsers");
                            object.put("communityName", CalendarActivity.community.getName());
                            object.put("idCrew", crew.getIdNumber());
                            dataOutputStream.writeUTF(object.toString());

                            Integer numMembers = Integer.parseInt(dataInputStream.readUTF());

                            for (int i = 0; i < numMembers; i++) {
                                JSONObject jsonUser = new JSONObject(dataInputStream.readUTF());
                                String firstName = jsonUser.getString("firstName");
                                String lastName = jsonUser.getString("lastName");
                                String userName = firstName + " " + lastName;
                                String id = jsonUser.getString("googleID");
                                if (!userNames.contains(userName)) {
                                    userNames.add(userName);
                                    googleIDs.add(id);
                                }
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

                android.os.SystemClock.sleep(700);


                for (int i = 0; i < userNames.size(); i++) {
                    addButton(userNames.get(i));
                }

                dialog.show();

                ImageButton check = (ImageButton) dialog.findViewById(R.id.check);
                // fab.setImageResource(R.drawable.calendar);

                check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

            }
        });

        final ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editBoardPost = (EditText) findViewById(R.id.editBoardPost);
                String postString = editBoardPost.getText().toString();
                if (!postString.equals("")) {

                    String fullPost = MainActivity.fullName;
                    fullPost = fullPost.concat(": ");
                    fullPost = fullPost.concat(postString);

                    final String finalPost = fullPost;
                    final Socket[] socket = new Socket[1];
                    final OutputStream[] outputStream = new OutputStream[1];
                    final InputStream[] inputStream = new InputStream[1];
                    final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                    final DataInputStream[] dataInputStream = new DataInputStream[1];
                    final JSONObject object = new JSONObject();

                    if (MainActivity.server) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                                    outputStream[0] = socket[0].getOutputStream();
                                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                    Calendar calendar = Calendar.getInstance();
                                    String year = String.valueOf(calendar.get(Calendar.YEAR));
                                    String month = String.valueOf(calendar.get(Calendar.MONTH));
                                    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                                    String hour = String.valueOf(calendar.get(Calendar.HOUR));
                                    String minute = String.valueOf(calendar.get(Calendar.MINUTE));

                                    String date = year;
                                    date = date.concat("-");
                                    date = date.concat(month);
                                    date = date.concat("-");
                                    date = date.concat(day);

                                    String time = hour;
                                    time = time.concat(":");
                                    time = time.concat(minute);

                                    object.put("function", "addMessage");
                                    object.put("message", finalPost);
                                    object.put("date", date);
                                    object.put("time", time);
                                    String communityName = CalendarActivity.community.getName();
                                    communityName = communityName.concat("_");
                                    communityName = communityName.concat(crew.getCrewName());
                                    communityName = communityName.concat("_");
                                    communityName = communityName.concat(String.valueOf(crew.getIdNumber()));
                                    object.put("communityName", communityName);
                                    object.put("pinned", "0");

                                    dataOutputStream[0].writeUTF(object.toString());

                                    outputStream[0].close();
                                    dataOutputStream[0].close();
                                    socket[0].close();

                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        android.os.SystemClock.sleep(300);

                    }
                    fullPost = fullPost.concat("\n");
                    addMessage(fullPost);

                    //clear text from EditText
                    editBoardPost.setText("");

                    //close keyboard
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    private void addMessage(String message) {
        Log.d("GET MESSAGES", "working");
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.boardLayout);
        TextView textView = new TextView(this);
        textView.setText(message);
        android.widget.LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        linearLayout.addView(textView);
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
        //android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need

        ll.addView(b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupActivity.theguy = namex;
                Intent intent = new Intent(BoardActivity.this, UserProfile.class);
                int index = userNames.indexOf(namex);
                intent.putExtra("googleID", googleIDs.get(index));
                startActivity(intent);
            }
        });
    }

}
