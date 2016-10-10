package cs307.butterfly;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.EditText;


public class CommunityActivity extends AppCompatActivity {
    final Context context = this;
    private String result;
    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                addGroup("HELLO");

            }
        });
    }

public void promptName(){

    final Dialog dialog = new Dialog(CommunityActivity.this);
    dialog.setContentView(R.layout.prompt);
    dialog.setTitle("Title");

    Button button = (Button) dialog.findViewById(R.id.ok);
    button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            EditText edit=(EditText)dialog.findViewById(R.id.editTextDialogUserInput);
            String text=edit.getText().toString();
            dialog.dismiss();
            result=text;
            addButton();

        }
    });
    dialog.show();
}

    public void addGroup(String name)
    {
        promptName();
    }

    public void addButton()
    {
        LinearLayout ll = (LinearLayout)findViewById(R.id.linear);
        b = new Button(this);
        b.setText(result);
        android.widget.LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,320); // 60 is height you can set it as u need
        b.setLayoutParams(lp);
        ll.addView(b);
    }

}
