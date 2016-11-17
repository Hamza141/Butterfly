package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;


public class CommunityActivity extends AppCompatActivity {
    final Context context = this;
    static ArrayList<Community> communities;
    Random randomno = new Random();
    private String result;
    Button b;

    @Override
    protected void onResume() {
        super.onResume();
        int i = 0;
        while (i != MainActivity.buffer.size()) {
            Community community = new Community(MainActivity.buffer.get(i));
            addButton(community);
            MainActivity.buffer.remove(MainActivity.buffer.get(i));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

        communities = new ArrayList<>();

        //Read a file to see which communities the user is already a part of
        File file = new File(context.getFilesDir(), "myCommunities");
        FileInputStream fileInputStream = null;
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        try {
            fileInputStream = new FileInputStream(file);
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
        for (String cont : contents) {
            result = cont;
            if (!result.equals("")) {
                Community community = new Community(result);
                communities.add(community);
                MainActivity.myCommunities.add(result);
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Groups");
        }

        ImageButton fab = (ImageButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGroup();
            }
        });

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
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
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
                final String text = edit.getText().toString();
                dialog.dismiss();
                result = text;
                Community community = new Community(text);
                communities.add(community);
                addButton(community);

                //Add the community to the lists
                MainActivity.iModerator.add(result);
                MainActivity.myCommunities.add(result);

                try {
                    result = text + '\n';
                    FileOutputStream fileOutputStream = openFileOutput("iModerator", MODE_APPEND);
                    fileOutputStream.write(result.getBytes());
                    fileOutputStream.close();

                    fileOutputStream = openFileOutput("myCommunities", MODE_APPEND);
                    fileOutputStream.write(result.getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

    public void addButton(final Community community) {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linear);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT, 300);
        params.setMargins(0, 0, 0, 8);

        final Button b1 = new Button(this);
        b1.setLayoutParams(params);
        b1.setBackgroundColor(Color.rgb(255- randomno.nextInt(50) ,255 - randomno.nextInt(30) ,255));
        b1.setCompoundDrawablesWithIntrinsicBounds( R.drawable.cake, 0, 0, 0);
        b1.setPadding(150, 0, 0, 0);
        b1.setText(community.getName());
        b1.setTextSize(18);
        b1.setTextColor(Color.rgb(0,0,0));
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 320); // 60 is height you can set it as u need

        ll.addView(b1);
        final Intent intent = new Intent(this, GroupActivity.class);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*for (int i = 0; i < communities.size(); i++) {
                    if (communities.get(i).getName().equals(b1.getText().toString())) {
                        CalendarActivity.community = communities.get(i);
                    }
                }*/
                CalendarActivity.community = community;
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_community, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
