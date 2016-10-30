package cs307.butterfly;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class CommunityListActivity extends CommunityActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        communities = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        addGroup();
    }

    @Override
    public void addGroup() {
        //connect to server and save all the communities to a file
        final Socket[] socket = new Socket[1];
        final OutputStream[] outputStream = new OutputStream[1];
        final InputStream[] inputStream = new InputStream[1];
        final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
        final DataInputStream[] dataInputStream = new DataInputStream[1];
        final JSONObject object = new JSONObject();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket[0] = new Socket(MainActivity.ip, 3300);
                    outputStream[0] = socket[0].getOutputStream();
                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);
                    object.put("function", "addAllCommunities");
                    dataOutputStream[0].writeUTF(object.toString());

                    //now receive all the names of the communities from the server
                    inputStream[0] = socket[0].getInputStream();
                    dataInputStream[0] = new DataInputStream(inputStream[0]);

                    //save the input stream from the server to the communities arraylist
                    String name = dataInputStream[0].readUTF();
                    while (!name.equals("")) {
                        Community community = new Community(name);
                        communities.add(community);

                        //add Button for each community
                        result = name;
                        addButton();

                        //read next community name
                        name = dataInputStream[0].readUTF();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}