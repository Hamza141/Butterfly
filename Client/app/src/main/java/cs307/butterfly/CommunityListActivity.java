package cs307.butterfly;

import android.content.Intent;
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

public class CommunityListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Communities");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

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
                        socket[0] = new Socket(MainActivity.ip, 3300);
                        outputStream[0] = socket[0].getOutputStream();
                        dataOutputStream[0] = new DataOutputStream(outputStream[0]);
                        object.put("function", "getCommunities");
                        dataOutputStream[0].writeUTF(object.toString());

                        //now receive all the names of the communities from the server
                        inputStream[0] = socket[0].getInputStream();
                        dataInputStream[0] = new DataInputStream(inputStream[0]);

                        //save the input stream from the server to the communities arraylist
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

            android.os.SystemClock.sleep(500);

            String[] name = names[0].split(", ");
            //Create buttons for each Community
            for (String aName : name) {
                if (Objects.equals(aName, "")){
                    continue;
                }
                Community community = new Community(aName);
                addButton(new Community(aName));
            }
        }
        else {
            //If running app in Offline mode, list myCommunities
            for (Community com : MainActivity.myCommunities) {
                Community community = new Community(com.getName());
                addButton(community);
            }
        }
    }

    public void addButton(final Community community) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linear_list);
        final Button b1 = new Button(this);
        b1.setText(community.getName());
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need
        b1.setLayoutParams(lp);
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
}