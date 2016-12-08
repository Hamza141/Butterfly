package cs307.butterfly;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class CrewListActivity extends AppCompatActivity {

    public static ArrayList<Crew> crewsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My crews in " + CalendarActivity.community.getName());
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageBitmap(textAsBitmap("+", 40, Color.WHITE));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO create crew
            }
        });

        crewsList = new ArrayList<>();

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
                        Socket socket = new Socket(MainActivity.ip, 3300);
                        OutputStream outputStream = socket.getOutputStream();
                        InputStream inputStream = socket.getInputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                        DataInputStream dataInputStream = new DataInputStream(inputStream);

                        JSONObject object = new JSONObject();
                        object.put("function", "getUserCrews");
                        object.put("googleID", MainActivity.googleID);
                        object.put("communityName", CalendarActivity.community.getName());
                        dataOutputStream.writeUTF(object.toString());

                        Integer numCrews = Integer.parseInt(dataInputStream.readUTF());

                        for (int i = 0; i < numCrews; i++) {
                            JSONObject jsonCrew = new JSONObject(dataInputStream.readUTF());
                            String crewName = jsonCrew.getString("crewName");
                            int idNumber = jsonCrew.getInt("idCrew");
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

            android.os.SystemClock.sleep(500);

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
