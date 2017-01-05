package cs307.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    public static boolean isEvent;
    public static ArrayList<String> userNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        userNames = new ArrayList<>();

        Log.d("isEVENT", String.valueOf(isEvent));
        if (isEvent) {
            String title = "Members RSVP'd to ";
            title = title.concat(EventViewerActivity.name);
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);

            if (MainActivity.server) {
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

                            object.put("function", "getrsvp");
                            object.put("communityName", CalendarActivity.community.getName());
                            object.put("eventName", EventViewerActivity.name);

                            dataOutputStream[0].writeUTF(object.toString());

                            inputStream[0] = socket[0].getInputStream();
                            dataInputStream[0] = new DataInputStream(inputStream[0]);

                            String input = dataInputStream[0].readUTF();
                            Log.d("INPUT", input);
                            String[] names = input.split(",");
                            for (int i = 0; i < names.length - 1; i++) {
                                Log.d("NAME", names[i]);
                                userNames.add(names[i]);
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
            }
        }

        else {
            String title = "Members of ";
            title = title.concat(CalendarActivity.community.getName());
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);

            if (MainActivity.server) {
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
            }
        }

        android.os.SystemClock.sleep(500);
        for (int i = 0; i < userNames.size(); i++) {
            Log.d("USERNAME", userNames.get(i));
            addUserButton(userNames.get(i));
        }

    }

    public void addUserButton(String userName) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll3);
        final Button b = new Button(this);
        b.setText(userName);
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need
        b.setLayoutParams(lp);
        ll.addView(b);
        final Intent intent = new Intent(this, ProfileActivity.class);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
    }

}
