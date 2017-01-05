package cs307.butterfly;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import java.util.Objects;
import java.util.Random;

public class CommunityListActivity extends AppCompatActivity {
    Random randomno = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Communities");
        }

        getHangouts();

        addGroup();
    }

    public void addGroup() {
        //connect to server and save all the communities to a file
        final Socket[] socket = new Socket[1];
        final OutputStream[] outputStream = new OutputStream[1];
        final InputStream[] inputStream = new InputStream[1];
        final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
        final DataInputStream[] dataInputStream = new DataInputStream[1];
        final JSONObject object = new JSONObject();
        final String[] names = new String[1];

        //If running in Server mode, list communities from server
        if (MainActivity.server) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                        outputStream[0] = socket[0].getOutputStream();
                        dataOutputStream[0] = new DataOutputStream(outputStream[0]);
                        object.put("function", "getCommunities");
                        dataOutputStream[0].writeUTF(object.toString());

                        android.os.SystemClock.sleep(200);

                        //Receive all the names of the communities from the server
                        inputStream[0] = socket[0].getInputStream();
                        dataInputStream[0] = new DataInputStream(inputStream[0]);

                        //Save the input stream from the server to the communities arraylist
                        names[0] = dataInputStream[0].readUTF();

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

            android.os.SystemClock.sleep(700);

            String[] name;
            if (names[0] != null) {
                name = names[0].split(", ");
                //Create buttons for each Community
                for (String aName : name) {
                    if (Objects.equals(aName, "")) {
                        continue;
                    }
                    Community community = new Community(aName);
                    addButton(community);
                }
            }
        } else {
            //If running app in Offline mode, list myCommunities
            for (Community com : MainActivity.myCommunities) {
                Community community = new Community(com.getName());
                addButton(community);
            }
        }
    }

    public void addButton(final Community community) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linear_list);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT, 300);
        params.setMargins(0, 0, 0, 8);

        final Button b1 = new Button(this);
     //   buttons.add(b1);
        b1.setLayoutParams(params);
        b1.setBackgroundColor(Color.rgb(255 - randomno.nextInt(50), 255 - randomno.nextInt(30), 255));
        b1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.home1, 0, 0, 0);
        b1.setPadding(150, 0, 0, 0);
        b1.setText(community.getName());
        b1.setTextSize(18);
        b1.setTextColor(Color.rgb(0, 0, 0));
        //android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need

        ll.addView(b1);
        final Intent intent = new Intent(this, GroupActivity.class);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarActivity.community = community;
                startActivity(intent);
            }
        });
    }

    public void getHangouts() {
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
                    object.put("function", "getUserHangouts");
                    object.put("googleID", MainActivity.googleID);
                    dataOutputStream.writeUTF(object.toString());

                    String hangoutsString = dataInputStream.readUTF();
                    String[] hangoutsArray = hangoutsString.split(", ");
                    for (int i = 0; i < hangoutsArray.length; i++) {
                        MainActivity.hangoutsJoined.add(hangoutsArray[i]);
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
    }
}