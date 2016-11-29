package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class GroupActivity extends AppCompatActivity {
    final Context context = this;
    @SuppressWarnings("SpellCheckingInspection")
    public static String theguy;
    Button profile4, profile3, profile2, profile1, profile;
    //Join and Leave
    Button join;
    Button leave;

    @Override
    protected void onResume() {
        super.onResume();

        //Check whether the user is already in the community or not
        //If the user is in the community already, only display the leave button and vice versa
        join.setVisibility(View.VISIBLE);
        leave.setVisibility(View.GONE);

        for (int i = 0; i < MainActivity.myCommunities.size(); i++) {
            if (MainActivity.myCommunities.get(i).getName().equals(CalendarActivity.community.getName())) {
                leave.setVisibility(View.VISIBLE);
                join.setVisibility(View.GONE);
                break;
            }
        }

        if (join.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < MainActivity.buffer.size(); i++) {
                if (MainActivity.buffer.get(i).getName().equals(CalendarActivity.community.getName())) {
                    leave.setVisibility(View.VISIBLE);
                    join.setVisibility(View.GONE);
                    break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(CalendarActivity.community.getName());
        }

        join = (Button) findViewById(R.id.join);
        leave = (Button) findViewById(R.id.leave);

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

        ImageButton button2 = (ImageButton) findViewById(R.id.view_all);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(GroupActivity.this);

                dialog.setContentView(R.layout.dialog3);
                dialog.setTitle("Title");

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                if (dialog.getWindow() != null) {
                    lp.copyFrom(dialog.getWindow().getAttributes());
                }
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                dialog.getWindow().setAttributes(lp);
                profile = (Button) dialog.findViewById(R.id.button3);

                profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupActivity.theguy = profile.getText().toString();
                        Intent intent = new Intent(GroupActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                });
                profile1 = (Button) dialog.findViewById(R.id.button4);

                profile1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupActivity.theguy = profile1.getText().toString();
                        Intent intent = new Intent(GroupActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                });

                profile2 = (Button) dialog.findViewById(R.id.button5);

                profile2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupActivity.theguy = profile2.getText().toString();
                        Intent intent = new Intent(GroupActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                });


                profile3 = (Button) dialog.findViewById(R.id.button6);

                profile3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupActivity.theguy = profile3.getText().toString();
                        Intent intent = new Intent(GroupActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                });

                profile4 = (Button) dialog.findViewById(R.id.button8);

                profile4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GroupActivity.theguy = profile4.getText().toString();
                        Intent intent = new Intent(GroupActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                });
                dialog.show();
            }
        });

        //Check whether the user is already in the community or not
        //If the user is in the community already, only display the leave button and vice versa
        join.setVisibility(View.VISIBLE);
        leave.setVisibility(View.GONE);

        for (int i = 0; i < MainActivity.myCommunities.size(); i++) {
            if (MainActivity.myCommunities.get(i).getName().equals(CalendarActivity.community.getName())) {
                leave.setVisibility(View.VISIBLE);
                join.setVisibility(View.GONE);
                break;
            }
        }

        if (join.getVisibility() == View.VISIBLE) {
            for (int i = 0; i < MainActivity.buffer.size(); i++) {
                if (MainActivity.buffer.get(i).getName().equals(CalendarActivity.community.getName())) {
                    leave.setVisibility(View.VISIBLE);
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
            }
        });

        Button uglyAssButton = (Button) findViewById(R.id.uglyAssButton);
        final Context context = this;
        uglyAssButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BoardActivity.class);
                startActivity(intent);
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
}