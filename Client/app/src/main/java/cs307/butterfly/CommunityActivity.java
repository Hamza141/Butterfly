package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class CommunityActivity extends AppCompatActivity {
    final Context context = this;
    @SuppressWarnings("SpellCheckingInspection")
    Random randomno = new Random();
    private String result;
    Button b;
    static ArrayList<Button> buttons;

    @Override
    protected void onResume() {
        super.onResume();

        //Display the communities in the buffer
        int i = 0;
        while (i != MainActivity.buffer.size()) {
            Community community = new Community(MainActivity.buffer.get(i).getName());
            addButton(community);
            MainActivity.myCommunities.add(community);
            MainActivity.buffer.remove(MainActivity.buffer.get(i));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Check whether the user has a googleID stored locally
        File file = new File(context.getFilesDir(), "googleID");
        FileInputStream fileInputStream = null;
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        try {
            fileInputStream = new FileInputStream(file);
            //noinspection ResultOfMethodCallIgnored
            fileInputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String content = new String(bytes);
        String[] contents = content.split("\n");

        if (contents[0].isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            MainActivity.googleID = contents[0];
        }

        setContentView(R.layout.activity_community);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Groups");
        }

        ImageButton view_all = (ImageButton) findViewById(R.id.view_all);
        view_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommunityActivity.this, CommunityListActivity.class);
                startActivity(intent);
            }
        });

        MainActivity.myCommunities = new ArrayList<>();
        MainActivity.iModerator = new ArrayList<>();
        buttons = new ArrayList<>();

        if (!MainActivity.server) {
            //Read a file to see which communities the user is already a part of
            file = new File(context.getFilesDir(), "myCommunities");
            fileInputStream = null;
            length = (int) file.length();
            bytes = new byte[length];

            try {
                fileInputStream = new FileInputStream(file);
                //noinspection ResultOfMethodCallIgnored
                fileInputStream.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileInputStream != null)
                        fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            content = new String(bytes);
            contents = content.split("\n");
            for (String cont : contents) {
                result = cont;
                if (!result.equals("")) {
                    Community community = new Community(result);
                    MainActivity.myCommunities.add(community);
                    addButton(community);
                }
            }

            //Read a file to see which communities the user is a moderator of
            file = new File(context.getFilesDir(), "iModerator");
            fileInputStream = null;
            length = (int) file.length();
            bytes = new byte[length];

            try {
                fileInputStream = new FileInputStream(file);
                //noinspection ResultOfMethodCallIgnored
                fileInputStream.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileInputStream != null)
                        fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            content = new String(bytes);
            contents = content.split("\n");
            for (String cont : contents) {
                result = cont;
                if (!result.equals("")) {
                    MainActivity.iModerator.add(result);
                }
            }
        }

        //Check with server to see which communities is the user a part of
        if (MainActivity.server) {
            final String[] name = new String[2];
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Create stuff for the client to connect to the app
                        Socket socket;
                        OutputStream outputStream;
                        InputStream inputStream;
                        DataOutputStream dataOutputStream;
                        DataInputStream dataInputStream;
                        JSONObject object = new JSONObject();
                        JSONObject object2 = new JSONObject();

                        //Connect to server
                        socket = new Socket(MainActivity.ip, 3300);
                        outputStream = socket.getOutputStream();
                        dataOutputStream = new DataOutputStream(outputStream);

                        //Send function and google ID to server
                        object.put("function", "getUserCommunities");
                        object.put("googleID", MainActivity.googleID);
                        dataOutputStream.writeUTF(object.toString());

                        //Receive all the names of the communities from the server
                        inputStream = socket.getInputStream();
                        dataInputStream = new DataInputStream(inputStream);

                        //Save the input stream from the server
                        name[0] = dataInputStream.readUTF();

                        //Close everything
                        dataInputStream.close();
                        inputStream.close();
                        dataOutputStream.close();
                        outputStream.close();
                        socket.close();

                        //Connect to server again
                        socket = new Socket(MainActivity.ip, MainActivity.port);
                        outputStream = socket.getOutputStream();
                        dataOutputStream = new DataOutputStream(outputStream);

                        //Send function and google ID to server
                        object2.put("function", "getUserModerator");
                        object2.put("googleID", MainActivity.googleID);
                        dataOutputStream.writeUTF(object2.toString());

                        //Receive all the names of the communities that the user is a moderator of from the server
                        inputStream = socket.getInputStream();
                        dataInputStream = new DataInputStream(inputStream);

                        //Save the input stream from the server
                        name[1] = dataInputStream.readUTF();

                        //close everything
                        dataInputStream.close();
                        inputStream.close();
                        outputStream.close();
                        dataOutputStream.close();
                        socket.close();
                    } catch (IOException | JSONException e) {
                        errorToast();
                        Log.d("Exception", "Error: Server might be offline");
                        e.printStackTrace();
                    }
                }
            }).start();

            android.os.SystemClock.sleep(500);
            if (name[0] != null) {
                String[] names = name[0].split(", ");
                MainActivity.myCommunities.clear();
                deleteFile("myCommunities");
                //Create buttons for each Community
                for (String aName : names) {
                    if (Objects.equals(aName, "")) {
                        continue;
                    }
                    Community community = new Community(aName);
                    MainActivity.myCommunities.add(community);
                    addButton(community);
                    try {
                        String result = aName + '\n';
                        FileOutputStream fileOutputStream = openFileOutput("myCommunities", MODE_APPEND);
                        fileOutputStream.write(result.getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (name[1] != null) {
                    names = name[1].split(", ");
                    MainActivity.iModerator.clear();
                    deleteFile("iModerator");
                    for (String aName : names) {
                        MainActivity.iModerator.add(aName);
                        try {
                            String result = aName + '\n';
                            FileOutputStream fileOutputStream = openFileOutput("iModerator", MODE_APPEND);
                            fileOutputStream.write(result.getBytes());
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Groups");
        }

        final ImageButton fab = (ImageButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGroup();
            }
        });

        //noinspection SpellCheckingInspection
        ImageButton viewall1 = (ImageButton) findViewById(R.id.view_all1);
        viewall1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PersonalCalendarActivity.class);
                startActivity(intent);
            }
        });
    }

    //Create new community
    public void addGroup() {
        final Dialog dialog = new Dialog(CommunityActivity.this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Title");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            lp.copyFrom(dialog.getWindow().getAttributes());
        }
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        //dialog.show();
        dialog.getWindow().setAttributes(lp);

        b = (Button) dialog.findViewById(R.id.ok);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create stuff for the client to connect to the app
                final Socket[] socket = new Socket[1];
                final OutputStream[] outputStream = new OutputStream[1];
                final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                final JSONObject object = new JSONObject();
                final JSONObject object2 = new JSONObject();

                EditText edit = (EditText) dialog.findViewById(R.id.editTextDialogUserInput);
                CheckBox checkPrivate = (CheckBox) dialog.findViewById(R.id.checkPrivate);

                final String text = edit.getText().toString();

                dialog.dismiss();
                result = text;
                Community community = new Community(text);
                //Add the community to the myCommunities list
                MainActivity.myCommunities.add(community);
                addButton(community);
                final String [] ckPrivate = new String[1];
                ckPrivate[0] = "0";

                if (checkPrivate.isChecked()) {
                    ckPrivate[0] = "1";
                }

                //Add the community to the iModerator list
                MainActivity.iModerator.add(result);

                try {
                    result = text + '\n';

                    //Add new community to myCommunities file
                    FileOutputStream fileOutputStream = openFileOutput("myCommunities", MODE_APPEND);
                    fileOutputStream.write(result.getBytes());
                    fileOutputStream.close();

                    //Add new community to iModerator file
                    fileOutputStream = openFileOutput("iModerator", MODE_APPEND);
                    fileOutputStream.write(result.getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //If app is running in Server mode send data to server
                if (MainActivity.server) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //Connect to server
                                socket[0] = new Socket(MainActivity.ip, 3300);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                //Send community name to server
                                object.put("function", "addCommunity");
                                object.put("name", text);
                                object.put("private", ckPrivate[0]);
                                dataOutputStream[0].writeUTF(object.toString());

                                //Close everything
                                dataOutputStream[0].close();
                                outputStream[0].close();
                                socket[0].close();

                                //Connect to server again
                                socket[0] = new Socket(MainActivity.ip, MainActivity.port);
                                outputStream[0] = socket[0].getOutputStream();
                                dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                //Add user to community as moderator
                                object2.put("function", "addCommunityUser");
                                object2.put("communityName", text);
                                object2.put("isLeader", "1");
                                object2.put("googleID", MainActivity.googleID);
                                dataOutputStream[0].writeUTF(object2.toString());

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
            }
        });
        dialog.show();
    }

    public void errorToast() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(context, "Error: Server might be offline", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public void addButton(final Community community) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linear);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT, 300);
        params.setMargins(0, 0, 0, 8);

        final Button b1 = new Button(this);
        buttons.add(b1);
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

    //Send Invite dialog
    public void sendInvite() {
        final Dialog dialog = new Dialog(CommunityActivity.this);
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
                if (MainActivity.server) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("function", "sendInvite");
                        object.put("googleID", MainActivity.googleID);
                        object.put("email", email.getText());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_community, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //item.setIcon(getResources().getDrawable(R.drawable.cast_ic_notification_play));
        switch (item.getItemId()) {
            /*case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            */
            case R.id.option_send_invite:
                sendInvite();
                return true;
            case R.id.option_sign_in_again:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.option_my_profile:
                Intent intent2 = new Intent(this, UserProfile.class);
                startActivity(intent2);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
