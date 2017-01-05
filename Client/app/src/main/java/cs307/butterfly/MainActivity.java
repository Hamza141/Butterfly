package cs307.butterfly;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
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
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {

    //10.0.2.2
    static String ip = "192.168.1.7";
    static int port = 3300;
    static final Object lock = new Object();
    static final Semaphore sema = new Semaphore(1);
    static boolean server = true;
    static boolean failed = true;
    static boolean completed = false;
    static boolean inProgress = false;

    static String[] strings;

    static String fullName = "";
    static String googleID = "";

    static ArrayList<Community> myCommunities;
    static ArrayList<String> iModerator;
    static ArrayList<Community> buffer = new ArrayList<>();

    static ArrayList<String> hangoutsJoined = new ArrayList<>();

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
                    outputStream[0] = socket[0].getOutputStream();
                    dataOutputStream[0] = new DataOutputStream(outputStream[0]);

                    //Send JSONObject to server
                    dataOutputStream[0].writeUTF(jsonObject.toString());

                    //If execution reaches till here, everything is working
                    failed = false;

                    //Close everything
                    dataOutputStream[0].close();
                    outputStream[0].close();
                    synchronized (lock) {
                        lock.notify();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    lock.notify();
                }
            }
        }).start();

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static String[] connectionSendReceiveStrings(final JSONObject jsonObject) {
        final String[] string = new String[1];
        failed = true;
        completed = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Connect to server
                    Socket socket = new Socket(ip, port);
                    OutputStream outputStream = socket.getOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    //Send JSONObject to server
                    dataOutputStream.writeUTF(jsonObject.toString());

                    //Receive String from server
                    InputStream inputStream = socket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    synchronized (lock) {
                        lock.notify();
                    }

                    //Save incoming string to array
                    string[0] = dataInputStream.readUTF();

                    //If execution reaches till here, everything is working
                    failed = false;
                    completed = true;

                    //Close everything
                    dataInputStream.close();
                    inputStream.close();
                    dataOutputStream.close();
                    outputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Thread.yield();
        android.os.SystemClock.sleep(100);

        if (failed) {
            return null;
        }

        //Split the incoming string and store it in strings
        //strings = string[0].split(", ");
        strings = string;
        return strings;
    }

    static ArrayList<JSONObject> connectionSendReceiveJSONObjects(final JSONObject jsonObject) {
        final ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        failed = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Connect to server
                    Socket socket = new Socket(ip, port);
                    OutputStream outputStream = socket.getOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    //Send JSONObject to server
                    dataOutputStream.writeUTF(jsonObject.toString());

                    //Receive String from server
                    InputStream inputStream = socket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);

                    synchronized (lock) {
                        lock.notify();
                    }

                    //Save incoming string
                    String string = dataInputStream.readUTF();

                    //Get number of incoming JSON Objects
                    int num = Integer.parseInt(string);
                    Log.d("Num", String.valueOf(num));
                    for (int i = 0; i < num; i++) {
                        //Add each JSON Object to arraylist
                        JSONObject jsonObject = new JSONObject(dataInputStream.readUTF());
                        jsonObjects.add(jsonObject);
                    }

                    //If execution reaches till here, everything is working
                    failed = false;

                    //Close everything
                    dataInputStream.close();
                    inputStream.close();
                    dataOutputStream.close();
                    outputStream.close();
                    socket.close();
                } catch (IOException | JSONException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Thread.yield();
        android.os.SystemClock.sleep(100);

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

class ConnectSend extends AsyncTask<JSONObject, Void, Boolean> {
    OutputStream outputStream;
    DataOutputStream dataOutputStream;

    @Override
    protected void onPreExecute() {
        MainActivity.completed = false;
        MainActivity.inProgress = true;
    }

    @Override
    protected Boolean doInBackground(JSONObject... params) {
        try {
            //Connect to server
            //MainActivity.ssocket = new Socket(ip, port);
            //outputStream = MainActivity.ssocket.getOutputStream();
            //dataOutputStream = new DataOutputStream(outputStream);

            //Send JSONObject to server
            dataOutputStream.writeUTF(params[0].toString());

            //Close everything
            dataOutputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.completed = false;
            MainActivity.inProgress = false;
            return false;
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        MainActivity.inProgress = true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            MainActivity.completed = true;
            MainActivity.inProgress = false;
        }
    }

    @Override
    protected void onCancelled() {
        MainActivity.completed = false;
        MainActivity.inProgress = false;
    }
}

class ConnectReceiveStrings extends AsyncTask<JSONObject, Void, String> {
    OutputStream outputStream;
    DataOutputStream dataOutputStream;
    InputStream inputStream;
    DataInputStream dataInputStream;
    private String input;

    @Override
    protected void onPreExecute() {
        MainActivity.completed = false;
        MainActivity.inProgress = true;
    }

    @Override
    protected String doInBackground(JSONObject... params) {
        try {
            //publishProgress();

            //Connect to server
            // MainActivity.ssocket = new Socket(ip, port);
            //outputStream = MainActivity.ssocket.getOutputStream();
            // dataOutputStream = new DataOutputStream(outputStream);

            //Send JSONObject to server
            dataOutputStream.writeUTF(params[0].toString());

            //Receive String from server
            //inputStream = MainActivity.ssocket.getInputStream();
            // dataInputStream = new DataInputStream(inputStream);
            input = dataInputStream.readUTF();


            //Close everything
            dataOutputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        //synchronized (CommunityActivity.lock) {
        //    CommunityActivity.lock.notify();
        //}
        return input;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        while (MainActivity.inProgress) {
            android.os.SystemClock.sleep(300);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (!result.equals("")) {
            MainActivity.completed = true;
            MainActivity.inProgress = false;
            MainActivity.strings = new String[]{input};
            //MainActivity.strings = input.split(", ");
        }

    }

    @Override
    protected void onCancelled() {
        MainActivity.completed = false;
        MainActivity.inProgress = false;
    }
}