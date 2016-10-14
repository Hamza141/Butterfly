package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;


public class CommunityActivity extends AppCompatActivity {
    final Context context = this;
    static ArrayList<Community> communities;

    private String result;
    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        communities = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Groups");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageBitmap(textAsBitmap("+", 40, Color.WHITE));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                addGroup();
            }
        });
    }

public void addGroup(){
    final Dialog dialog = new Dialog(CommunityActivity.this);
    dialog.setContentView(R.layout.dialog);
    dialog.setTitle("Title");

    b = (Button) dialog.findViewById(R.id.ok);
    b.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText edit=(EditText)dialog.findViewById(R.id.editTextDialogUserInput);
            String text=edit.getText().toString();
            dialog.dismiss();
            result=text;
            Community community = new Community(text);
            communities.add(community);
            addButton();

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

    public void addButton()
    {
        LinearLayout ll = (LinearLayout)findViewById(R.id.linear);
        final Button b1 = new Button(this);
        b1.setText(new String(result));
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,320); // 60 is height you can set it as u need
        b1.setLayoutParams(lp);
        ll.addView(b1);
       final Intent intent = new Intent(this, GroupActivity.class);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < communities.size(); i++) {
                    if (communities.get(i).getName().equals(b1.getText().toString())) {
                        CalendarActivity.community = communities.get(i);
                    }
                }
                startActivity(intent);
            }
        });

    }

}
