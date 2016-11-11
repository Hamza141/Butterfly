package cs307.butterfly;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

public class BoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Log.d("BoardActivity", "created");

        final Socket[] socket = new Socket[1];
        final OutputStream[] outputStream = new OutputStream[1];
        final InputStream[] inputStream = new InputStream[1];
        final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
        final DataInputStream[] dataInputStream = new DataInputStream[1];
        final JSONObject object = new JSONObject();

        if (MainActivity.server) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket[0] = new Socket(MainActivity.ip, 3300);
                        outputStream[0] = socket[0].getOutputStream();
                        dataOutputStream[0] = new DataOutputStream(outputStream[0]);
                        object.put("function", "getMessages");
                        object.put("communityName", CalendarActivity.community.getName());
                        dataOutputStream[0].writeUTF(object.toString());

                        inputStream[0] = socket[0].getInputStream();
                        dataInputStream[0] = new DataInputStream(inputStream[0]);

                        int numMessages = Integer.parseInt(dataInputStream[0].readUTF());

                        for (int i = 0; i < numMessages; i++) {
                            JSONObject jsonMessage = new JSONObject(dataInputStream[0].readUTF());
                            String message = (String) jsonMessage.get("message");
                            addMessage(message);
                        }

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

            android.os.SystemClock.sleep(300);

        }

        final ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editBoardPost = (EditText) findViewById(R.id.editBoardPost);
                String postString = editBoardPost.getText().toString();
                if (!postString.equals("")) {

                    String fullPost = MainActivity.firstName;
                    fullPost = fullPost.concat(": ");
                    fullPost = fullPost.concat(postString);

                    final String finalPost = fullPost;
                    final Socket[] socket = new Socket[1];
                    final OutputStream[] outputStream = new OutputStream[1];
                    final InputStream[] inputStream = new InputStream[1];
                    final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
                    final DataInputStream[] dataInputStream = new DataInputStream[1];
                    final JSONObject object = new JSONObject();

                    if (MainActivity.server) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    socket[0] = new Socket(MainActivity.ip, 3300);
                                    outputStream[0] = socket[0].getOutputStream();
                                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                                    Calendar calendar = Calendar.getInstance();
                                    String year = String.valueOf(calendar.get(Calendar.YEAR));
                                    String month = String.valueOf(calendar.get(Calendar.MONTH));
                                    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
                                    String hour = String.valueOf(calendar.get(Calendar.HOUR));
                                    String minute = String.valueOf(calendar.get(Calendar.MINUTE));

                                    String date = year;
                                    date.concat("-");
                                    date.concat(month);
                                    date.concat("-");
                                    date.concat(day);

                                    String time = hour;
                                    time.concat(":");
                                    time.concat(minute);

                                    object.put("message", finalPost);
                                    object.put("date", date);
                                    object.put("time", time);
                                    object.put("communityName", CalendarActivity.community.getName());
                                    object.put("pinned", "0");

                                    dataOutputStream[0].writeUTF(object.toString());

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

                        android.os.SystemClock.sleep(300);

                    }
                    addMessage(finalPost);

                    //clear text from EditText
                    editBoardPost.setText("");

                    //close keyboard
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
    }

    private void addMessage(String message) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.boardLayout);
        TextView textView = new TextView(this);
        textView.setText(message);
        android.widget.LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        linearLayout.addView(textView);
    }

}
