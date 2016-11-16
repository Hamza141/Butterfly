package cs307.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //10.0.2.2
    static String ip = "128.211.225.79";
    static int port = 3300;
    static boolean server = true;

    static String firstName = "";
    static String googleID = "";

    static ArrayList <Community> myCommunities;
    static ArrayList <String> iModerator;
    static ArrayList <Community> buffer = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}