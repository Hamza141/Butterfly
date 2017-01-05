package cs307.butterfly;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class CrewListActivity extends AppCompatActivity {

    public static ArrayList<Crew> crewsList;
    public static ArrayList<String> userNames;
    public static ArrayList<String> googleIDs;
    public static ArrayList<Integer> selectedUsersIndexes;
    public static String selectedUsersString;
    public static int crewID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My crews in " + CalendarActivity.community.getName());
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        crewsList = new ArrayList<>();
        userNames = new ArrayList<>();
        googleIDs = new ArrayList<>();
        selectedUsersIndexes = new ArrayList<>();
        selectedUsersString = "";

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageBitmap(textAsBitmap("+", 40, Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                                if (!userNames.contains(userName) && !id.equals(MainActivity.googleID)) {
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

                final String[] usersArray = new String[userNames.size()];
                for (int i = 0; i < userNames.size(); i++) {
                    usersArray[i] = userNames.get(i);
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(CrewListActivity.this);
                builder.setTitle("Name your crew");
                final EditText crewNameEdit = new EditText(CrewListActivity.this);
                crewNameEdit.setHint("Crew name");
                builder.setView(crewNameEdit);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String crewName = crewNameEdit.getText().toString();
                        if (crewName.equals("")) {
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(CrewListActivity.this);
                            builder2.setMessage("Crew must have a name");
                            builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            AlertDialog warning = builder2.create();
                            warning.show();
                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(CrewListActivity.this);
                            builder1.setTitle("Add users");
                            builder1.setMultiChoiceItems(usersArray, null, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int index, boolean isSelected) {
                                    if (isSelected) {
                                        selectedUsersIndexes.add(index);
                                    } else if (selectedUsersIndexes.contains(index)) {
                                        selectedUsersIndexes.remove(Integer.valueOf(index));
                                    }
                                }
                            });
                            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    for (int j = 0; j < selectedUsersIndexes.size(); j++) {
                                        selectedUsersString = selectedUsersString.concat(googleIDs.get(selectedUsersIndexes.get(j)));
                                        selectedUsersString = selectedUsersString.concat(", ");
                                    }
                                    selectedUsersString = selectedUsersString.concat(MainActivity.googleID);

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
                                                object.put("function", "addCrew");
                                                object.put("communityName", CalendarActivity.community.getName());
                                                object.put("crewName", crewName);
                                                object.put("list", selectedUsersString);
                                                dataOutputStream.writeUTF(object.toString());

                                                android.os.SystemClock.sleep(100);

                                                crewID = dataInputStream.read();
                                                Log.d("CREWID", String.valueOf(crewID));

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

                                    android.os.SystemClock.sleep(1000);
                                    Log.d("CREWID2", String.valueOf(crewID));

                                    addButton(crewName, crewID);
                                    Crew crew = new Crew(crewName, crewID);
                                    crewsList.add(crew);

                                }
                            });
                            builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                            AlertDialog addUsers = builder1.create();
                            addUsers.show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog createCrew = builder.create();
                createCrew.show();
            }

        });

        if (MainActivity.server) {
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
                        object.put("function", "getUserCrews");
                        object.put("googleID", MainActivity.googleID);
                        object.put("communityName", CalendarActivity.community.getName());
                        dataOutputStream.writeUTF(object.toString());

                        while(true) {
                            JSONObject jsonCrew = new JSONObject(dataInputStream.readUTF());
                            String crewName = jsonCrew.getString("crewName");
                            int idNumber = jsonCrew.getInt("idCrew");
                            if (crewName.equals("END") && idNumber == -1) {
                                break;
                            }
                            Crew newCrew = new Crew(crewName, idNumber);
                            crewsList.add(newCrew);
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

            for (int i = 0; i < crewsList.size(); i++) {
                addButton(crewsList.get(i).getCrewName(), crewsList.get(i).getIdNumber());
            }
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

    public void addButton(final String crewName, final int idNumber) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.crewLL);
        final Button b = new Button(this);
        b.setText(crewName);
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need
        b.setLayoutParams(lp);
        ll.addView(b);
        //noinspection unused
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < crewsList.size(); i++) {
                    if (crewsList.get(i).getCrewName().equals(crewName) && crewsList.get(i).getIdNumber() == idNumber) {
                        BoardActivity.crew = crewsList.get(i);
                    }
                }
                Intent intent1 = new Intent(CrewListActivity.this, BoardActivity.class);
                startActivity(intent1);
            }
        });

    }
}
