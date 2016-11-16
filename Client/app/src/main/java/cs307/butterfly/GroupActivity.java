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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class GroupActivity extends AppCompatActivity {
    final Context context = this;
    public static String theguy;
    Button profile4, profile3, profile2, profile1, profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(CalendarActivity.community.getName());
        }


        //  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        ImageButton button12 = (ImageButton) findViewById(R.id.tocalender);
        // fab.setImageResource(R.drawable.calend);

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CalendarActivity.class);
                startActivity(intent);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("wtf");
                }

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
                lp.copyFrom(dialog.getWindow().getAttributes());
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

        //Join and Leave
        final Button join = (Button) findViewById(R.id.join);
        final Button leave = (Button) findViewById(R.id.leave);

        join.setVisibility(View.GONE);
        leave.setVisibility(View.GONE);
        if (!MainActivity.myCommunities.contains(CalendarActivity.community)) {
            join.setVisibility(View.VISIBLE);
        }
        if (MainActivity.myCommunities.contains(CalendarActivity.community)) {
            leave.setVisibility(View.VISIBLE);
        }

        //Join
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create stuff for the client to connect to the app
                final Socket[] socket = new Socket[1];
                final OutputStream[] outputStream = new OutputStream[1];
                final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                final JSONObject object = new JSONObject();

                //Add user to community
                if (MainActivity.server) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //Connect to server
                                socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                object.put("function", "addCommunityUser");
                                object.put("communityName", CalendarActivity.community.getName());
                                object.put("isLeader", "0");
                                object.put("googleID", MainActivity.googleID);
                                dataOutputStream[0].writeUTF(object.toString());

                                //close everything
                                outputStream[0].close();
                                dataOutputStream[0].close();
                                socket[0].close();

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                //Save community in buffer
                MainActivity.buffer.add(CalendarActivity.community);

                //Save community in myCommunities file
                try {
                    String result = CalendarActivity.community.getName() + '\n';
                    FileOutputStream fileOutputStream = openFileOutput("myCommunities", MODE_APPEND);
                    fileOutputStream.write(result.getBytes());
                    fileOutputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                android.os.SystemClock.sleep(500);

                //Change visibility of Join and Gone buttons
                join.setVisibility(View.GONE);
                leave.setVisibility(View.VISIBLE);
            }
        });

        //Leave
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create stuff for the client to connect to the app
                final Socket[] socket = new Socket[1];
                final OutputStream[] outputStream = new OutputStream[1];
                final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                final JSONObject object = new JSONObject();
                int index;

                //Remove user to community
                if (MainActivity.server) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //Connect to server
                                socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                object.put("function", "leaveCommunityUser");
                                object.put("communityName", CalendarActivity.community.getName());
                                object.put("googleID", MainActivity.googleID);
                                dataOutputStream[0].writeUTF(object.toString());

                                //close everything
                                outputStream[0].close();
                                dataOutputStream[0].close();
                                socket[0].close();

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                //Get index of Community in myCommunities
                index = MainActivity.myCommunities.indexOf(CalendarActivity.community);

                //Remove community from myCommunities arraylist
                MainActivity.myCommunities.remove(CalendarActivity.community);

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

                android.os.SystemClock.sleep(500);

                //Remove button from buttons arraylist
                CommunityActivity.buttons.get(index).setVisibility(View.GONE);
                CommunityActivity.buttons.remove(index);

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


}



