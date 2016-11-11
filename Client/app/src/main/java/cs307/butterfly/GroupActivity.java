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

public class GroupActivity extends AppCompatActivity {
    final Context context = this;
public static String theguy;
    Button profile4,profile3,profile2,profile1,profile;

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



