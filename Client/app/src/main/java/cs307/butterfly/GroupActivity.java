package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Calendar;

public class GroupActivity extends AppCompatActivity {
    final Context context = this;
    @SuppressWarnings("SpellCheckingInspection")
    public static String theguy;
    public static ArrayList<String> userNames;
    public static ArrayList<String> googleIDs;
    public static boolean isPrivate;
    Random randomno = new Random();
    Dialog dialog;
    //Join and Leave
    Button join;
    Button leave;
    Button crews;

    public static ArrayList<String> messages;

    @Override
    protected void onResume() {
        super.onResume();

        //Check whether the user is already in the community or not
        //If the user is in the community already, only display the leave button and vice versa
        join.setVisibility(View.VISIBLE);
        leave.setVisibility(View.GONE);
        crews.setVisibility(View.GONE);


        for (int i = 0; i < MainActivity.myCommunities.size(); i++) {
            if (MainActivity.myCommunities.get(i).getName().equals(CalendarActivity.community.getName())) {
                crews.setVisibility(View.VISIBLE);
                leave.setVisibility(View.VISIBLE);
                join.setVisibility(View.GONE);
                break;
            }
        }

        if (join.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < MainActivity.buffer.size(); i++) {
                if (MainActivity.buffer.get(i).getName().equals(CalendarActivity.community.getName())) {
                    crews.setVisibility(View.VISIBLE);
                    leave.setVisibility(View.VISIBLE);
                    join.setVisibility(View.GONE);
                    break;
                }
            }
        }

        refreshMessages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userNames = new ArrayList<>();
        googleIDs = new ArrayList<>();

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
                    object.put("function", "isPrivate");
                    object.put("communityName", CalendarActivity.community.getName());
                    dataOutputStream.writeUTF(object.toString());

                    android.os.SystemClock.sleep(100);

                    String privateCommunity = dataInputStream.readUTF();
                    isPrivate = Boolean.parseBoolean(privateCommunity);

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

        android.os.SystemClock.sleep(300);

        setContentView(R.layout.activity_group);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(CalendarActivity.community.getName());
        }

        join = (Button) findViewById(R.id.join);
        leave = (Button) findViewById(R.id.leave);
        crews = (Button) findViewById(R.id.crewsButton);


        //  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        ImageButton button12 = (ImageButton) findViewById(R.id.tocalender);
        // fab.setImageResource(R.drawable.calendar);

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CalendarActivity.class);
                startActivity(intent);
            }
        });

        ImageButton notifyGroup = (ImageButton) findViewById(R.id.notify);
        // fab.setImageResource(R.drawable.calendar);

        notifyGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                CharSequence text = "Group Notified!";
                int duration = Toast.LENGTH_SHORT;

                JSONObject object = new JSONObject();
                try {
                    object.put("function", "groupNotification");
                    object.put("communityName", CalendarActivity.community.getName());
                    object.put("name", MainActivity.fullName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                MainActivity.connectionSend(object);

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        Button hangoutsButton = (Button) findViewById(R.id.hangoutsButton);

        hangoutsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, HangoutViewerActivity.class);
                startActivity(intent);
            }
        });

        if (!isPrivate) {
            notifyGroup.setVisibility(View.GONE);
            hangoutsButton.setVisibility(View.VISIBLE);
        } else {
            hangoutsButton.setVisibility(View.GONE);
            notifyGroup.setVisibility(View.VISIBLE);
        }

        ImageButton button2 = (ImageButton) findViewById(R.id.view_all);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = new Dialog(GroupActivity.this);
                dialog.setContentView(R.layout.dialog3);
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
                            Socket socket = new Socket(MainActivity.ip, MainActivity.port);
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = socket.getInputStream();
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                            DataInputStream dataInputStream = new DataInputStream(inputStream);

                            JSONObject object = new JSONObject();
                            object.put("function", "getCommunityUsersWithID");
                            object.put("communityName", CalendarActivity.community.getName());
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
                    addButton(userNames.get(i));
                }


                dialog.show();
                /* if (MainActivity.server) {
                    final Socket[] socket = new Socket[1];
                    final OutputStream[] outputStream = new OutputStream[1];
                    final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                    final InputStream[] inputStream = new InputStream[1];
                    final DataInputStream[] dataInputStream = new DataInputStream[1];
                    final JSONObject object = new JSONObject();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                object.put("function", "getCommunityUsers");
                                object.put("communityName", CalendarActivity.community.getName());

                                dataOutputStream[0].writeUTF(object.toString());

                                inputStream[0] = socket[0].getInputStream();
                                dataInputStream[0] = new DataInputStream(inputStream[0]);

                                Integer numMembers = Integer.parseInt(dataInputStream[0].readUTF());

                                for (int i = 0; i < numMembers; i++) {
                                    JSONObject jsonUser = new JSONObject(dataInputStream[0].readUTF());
                                    String firstName = jsonUser.getString("firstName");
                                    String lastName = jsonUser.getString("lastName");
                                    String userName = firstName + " " + lastName;

                                    userNames.add(userName);

                                }

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
                } */


                ImageButton check = (ImageButton) dialog.findViewById(R.id.check);
                // fab.setImageResource(R.drawable.calendar);

                check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                ImageButton invite = (ImageButton) dialog.findViewById(R.id.invite);
                // fab.setImageResource(R.drawable.calendar);

                invite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendInvite();

                    }
                });

            }
        });


        //Check whether the user is already in the community or not
        //If the user is in the community already, only display the leave button and vice versa
        join.setVisibility(View.VISIBLE);
        leave.setVisibility(View.GONE);
        crews.setVisibility(View.VISIBLE);

        for (int i = 0; i < MainActivity.myCommunities.size(); i++) {
            if (MainActivity.myCommunities.get(i).getName().equals(CalendarActivity.community.getName())) {
                leave.setVisibility(View.VISIBLE);
                crews.setVisibility(View.VISIBLE);
                join.setVisibility(View.GONE);
                break;
            }
        }

        if (join.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < MainActivity.buffer.size(); i++) {
                if (MainActivity.buffer.get(i).getName().equals(CalendarActivity.community.getName())) {
                    leave.setVisibility(View.VISIBLE);
                    crews.setVisibility(View.VISIBLE);
                    join.setVisibility(View.GONE);
                    break;
                }
            }
        }

        //Join
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join();

                //Change visibility of Join and Gone buttons
                join.setVisibility(View.GONE);
                leave.setVisibility(View.VISIBLE);
                crews.setVisibility(View.VISIBLE);
            }
        });

        //Leave
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leave();

                //Change visibility of Join and Gone buttons
                leave.setVisibility(View.GONE);
                join.setVisibility(View.VISIBLE);
                crews.setVisibility(View.GONE);
            }
        });

        crews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, CrewListActivity.class);
                startActivity(intent);
            }
        });

        messages = new ArrayList<>();

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
                        object.put("function", "getMessages");
                        object.put("communityName", CalendarActivity.community.getName());
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

            android.os.SystemClock.sleep(500);

        }

        for (int i = 0; i < messages.size(); i++) {
            addMessage(messages.get(i) + "\n");
        }

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
                                    object.put("communityName", CalendarActivity.community.getName());
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

    void join() {
        //Add user to community
        JSONObject object = new JSONObject();
        if (MainActivity.server) {
            try {
                object.put("function", "addCommunityUser");
                object.put("communityName", CalendarActivity.community.getName());
                object.put("isLeader", "0");
                object.put("googleID", MainActivity.googleID);
                MainActivity.connectionSend(object);

                //Subscribe to topic
                String topic = CalendarActivity.community.getName();
                topic = topic.replaceAll("\\s", "_");
                FirebaseMessaging.getInstance().subscribeToTopic(topic);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Save community in buffer
        MainActivity.buffer.add(CalendarActivity.community);

        //Save community in myCommunities file
        try {
            String result = CalendarActivity.community.getName() + '\n';
            FileOutputStream fileOutputStream = openFileOutput("myCommunities", MODE_APPEND);
            fileOutputStream.write(result.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //android.os.SystemClock.sleep(500);
    }

    void leave() {
        //Remove user from community
        JSONObject object = new JSONObject();
        int index = -1;
        if (MainActivity.server) {
            try {
                object.put("function", "leaveCommunityUser");
                object.put("communityName", CalendarActivity.community.getName());
                object.put("googleID", MainActivity.googleID);
                MainActivity.connectionSend(object);

                //Unsubscribe from topic
                String topic = CalendarActivity.community.getName();
                topic = topic.replaceAll("\\s", "_");
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Get index of Community in myCommunities
        ArrayList<Community> communities = MainActivity.myCommunities;

        if (MainActivity.buffer.size() != 0) {
            communities = MainActivity.buffer;
        }

        for (int i = 0; i < communities.size(); i++) {
            if (communities.get(i).getName().equals(CalendarActivity.community.getName())) {
                index = i;
                break;
            }
        }

        //Remove community from myCommunities or buffer arraylist
        communities.remove(index);

        //Save new community list in myCommunities file
        deleteFile("myCommunities");
        try {
            for (int i = 0; i < MainActivity.myCommunities.size(); i++) {
                String name = MainActivity.myCommunities.get(i).getName();
                String result = name + '\n';
                FileOutputStream fileOutputStream;
                fileOutputStream = openFileOutput("myCommunities", MODE_APPEND);
                fileOutputStream.write(result.getBytes());
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //android.os.SystemClock.sleep(300);

        //Remove community from iModerator arraylist
        MainActivity.iModerator.remove(CalendarActivity.community.getName());

        //Save new community list in iModerator file
        deleteFile("iModerator");
        try {
            for (int i = 0; i < MainActivity.iModerator.size(); i++) {
                String name = MainActivity.iModerator.get(i);
                String result = name + '\n';
                FileOutputStream fileOutputStream;
                fileOutputStream = openFileOutput("iModerator", MODE_APPEND);
                fileOutputStream.write(result.getBytes());
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (communities == MainActivity.myCommunities) {
            //Remove button from buttons arraylist
            CommunityActivity.buttons.get(index).setVisibility(View.GONE);
            CommunityActivity.buttons.remove(index);
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
        //android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need

        ll.addView(b1);
        final Intent intent = new Intent(this, GroupActivity.class);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupActivity.theguy = namex;
                Intent intent = new Intent(GroupActivity.this, UserProfile.class);
                int index = userNames.indexOf(namex);
                intent.putExtra("googleID", googleIDs.get(index));
                startActivity(intent);
            }
        });
    }

    private void addMessage(String message) {
        Log.d("GET MESSAGES", "working");
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.boardLayout2);
        TextView textView = new TextView(this);
        textView.setText(message);
        android.widget.LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        linearLayout.addView(textView);
    }

    private void refreshMessages() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.boardLayout2);
        if (linearLayout.getChildCount() > 0) {
            linearLayout.removeAllViews();
        }
        messages = new ArrayList<>();

        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
                        object.put("function", "getMessages");
                        object.put("communityName", CalendarActivity.community.getName());
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

            android.os.SystemClock.sleep(500);

        }

        for (int i = 0; i < messages.size(); i++) {
            addMessage(messages.get(i) + "\n");
        }
    }


    public void sendInvite() {
        final Dialog dialog = new Dialog(GroupActivity.this);
        dialog.setContentView(R.layout.dialog_send_invite);
        dialog.setTitle("Title");


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            lp.copyFrom(dialog.getWindow().getAttributes());
        }
        final Button sendButton = (Button) dialog.findViewById(R.id.sendButton);
        final EditText email = (EditText) dialog.findViewById(R.id.email);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                CharSequence text = "Invite Sent!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                if (MainActivity.server) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("function", "inviteNotification");
                        object.put("name", MainActivity.fullName);
                        object.put("communityName", CalendarActivity.community.getName());
                        object.put("googleID", email.getText());
                        MainActivity.connectionSend(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }
}