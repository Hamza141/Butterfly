package cs307.butterfly;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //10.0.2.2
    static String ip = "10.186.80.53";
    static int port = 3300;
    static boolean server = true;
    static boolean failed = true;

    static String fullName = "";
    static String googleID = "";

    static ArrayList<Community> myCommunities;
    static ArrayList<String> iModerator;
    static ArrayList<Community> buffer = new ArrayList<>();

    static ArrayList<String> hangoutsJoined = new ArrayList<>();

    @SuppressWarnings("SpellCheckingInspection")
    static Socket ssocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    static void connectionSend(final JSONObject jsonObject) {
        final Socket[] socket = new Socket[1];
        final OutputStream[] outputStream = new OutputStream[1];
        final DataOutputStream[] dataOutputStream = new DataOutputStream[1];
        failed = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Connect to server
                    socket[0] = new Socket(ip, port);
                    ssocket = socket[0];
                    outputStream[0] = ssocket.getOutputStream();
                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                    //Send JSONObject to server
                    dataOutputStream[0].writeUTF(jsonObject.toString());

                    //If execution reaches till here, everything is working
                    failed = false;

                    //Close everything
                    dataOutputStream[0].close();
                    outputStream[0].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    String[] connectionReceiveStrings() {
        String[] strings;
        final String[] string = new String[1];
        final Socket[] socket = new Socket[1];
        final InputStream[] inputStream = new InputStream[1];
        final DataInputStream[] dataInputStream = new DataInputStream[1];
        failed = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Connect to server
                    socket[0] = new Socket(ip, port);
                    inputStream[0] = socket[0].getInputStream();
                    dataInputStream[0] = new DataInputStream(inputStream[0]);

                    //Add incoming string to arraylist
                    string[0] = dataInputStream[0].readUTF();

                    //If execution reaches till here, everything is working
                    failed = false;

                    //Close everything
                    dataInputStream[0].close();
                    inputStream[0].close();
                    socket[0].close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        android.os.SystemClock.sleep(500);

        if (failed) {
            return null;
        }

        //Split the incoming string and store it in strings
        strings = string[0].split(", ");

        return strings;
    }

    static ArrayList<JSONObject> connectionReceiveJSONObjects() {
        final ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        final InputStream[] inputStream = new InputStream[1];
        final DataInputStream[] dataInputStream = new DataInputStream[1];
        failed = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Connect to server
                    inputStream[0] = ssocket.getInputStream();
                    dataInputStream[0] = new DataInputStream(inputStream[0]);

                    //Add incoming string to arraylist
                    String string = dataInputStream[0].readUTF();

                    //Get number of incoming JSON Objects
                    int num = Integer.parseInt(string);
                    Log.d("Num", String.valueOf(num));
                    for (int i = 0; i < num; i++) {
                        //Add each JSON Object to arraylist
                        JSONObject jsonObject = new JSONObject(dataInputStream[0].readUTF());
                        Log.d("add JSON", jsonObject.toString());
                        jsonObjects.add(jsonObject);
                    }

                    //If execution reaches till here, everything is working
                    failed = false;

                    //Close everything
                    dataInputStream[0].close();
                    inputStream[0].close();
                    ssocket.close();
                } catch (IOException | JSONException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        android.os.SystemClock.sleep(500);

        if (failed) {
            return null;
        }

        return jsonObjects;
    }

    String[] readFile(String string) {
        String[] strings;
        File file = new File(this.getFilesDir(), string);
        FileInputStream fileInputStream = null;
        int length = (int) file.length();
        byte[] bytes = new byte[length];

        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = new String(bytes);

        //Spilt the input from the file
        strings = content.split("\n");

        return strings;
    }
}