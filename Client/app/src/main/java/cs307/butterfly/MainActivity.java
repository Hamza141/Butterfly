package cs307.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //10.0.2.2
    static String ip = "10.186.97.197";
    static int port = 3300;
    static boolean server = true;
    static String firstName = "";
    static String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}