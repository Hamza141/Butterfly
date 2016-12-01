package cs307.butterfly;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class UserProfile extends AppCompatActivity {

    Uri userPic;
    String fullName;
    String gID;
    ArrayList <String> userCommunities;
    ArrayList <String> userModerator;
    boolean fail = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        if (getIntent().getStringExtra("googleID") == null) {
            user(MainActivity.googleID);
            return;
        }

        user(getIntent().getStringExtra("googleID"));
    }

    void user(final String googleID) {
        final JSONObject[] userInfo = new JSONObject[1];

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(MainActivity.ip, 3300);
                    OutputStream outputStream = socket.getOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    JSONObject object = new JSONObject();
                    object.put("function", "getUserProfile");
                    object.put("googleID", googleID);
                    dataOutputStream.writeUTF(object.toString());

                    InputStream inputStream = socket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    String jsonObject = dataInputStream.readUTF();
                    Log.d("Input from server: ", jsonObject);

                    userInfo[0] = new JSONObject(jsonObject);

                    fail = false;

                    //Close everything
                    dataInputStream.close();
                    inputStream.close();
                    dataOutputStream.close();
                    outputStream.close();
                    socket.close();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //android.os.SystemClock.sleep(1000);

        while (fail) {

        }

        try {
            userPic = Uri.parse(userInfo[0].getString("pictureURL"));
            fullName = userInfo[0].getString("firstName");
            fullName = fullName.concat(" ");
            fullName = fullName.concat(userInfo[0].getString("lastName"));
            gID = userInfo[0].getString("googleID");
            userCommunities = new ArrayList<>(Arrays.asList(userInfo[0].getString("communitiesList").split(", ")));
            userModerator = new ArrayList<>(Arrays.asList(userInfo[0].getString("moderatorOf").split(", ")));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        Picasso.with(this).load(userPic).into((ImageView) findViewById(R.id.userPic));

        TextView uName = (TextView) findViewById(R.id.userName);
        TextView email = (TextView) findViewById(R.id.userEmail);

        uName.setText(fullName);
        email.setText(gID);

        boolean found;

        for (String community : userCommunities) {
            found = false;
            for (String moderator : userModerator) {
                if (moderator.contains(community)) {
                    String output = community + " - MOD";
                    addCommunityToList(output);
                    found = true;
                    break;
                }
            }
            if (!found) {
                addCommunityToList(community);
            }
        }
    }

    void addCommunityToList(String community){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.userCommunityListLinearLayout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Toolbar.LayoutParams.MATCH_PARENT, 100);
        //params.setMargins(0, 0, 0, 8);

        final TextView textView = new TextView(this);
        //final Button b1 = new Button(this);
        textView.setLayoutParams(params);
        //textView.setBackgroundColor(Color.rgb(255 - randomno.nextInt(50), 255 - randomno.nextInt(30), 255));
        //textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.home1, 0, 0, 0);
        //textView.setPadding(0, 0, 0, 0);
        textView.setText(community);
        //textView.setTextSize(18);
        //textView.setTextColor(Color.rgb(0, 0, 0));
        linearLayout.addView(textView);
    }

}
