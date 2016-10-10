/**
 * Created by khanh on 10/9/16.
 */

import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;

public class Client {
    public static void main(String[] args) {
        String serverName = "localhost";
        int port = 60660;
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            JSONObject obj = new JSONObject();
            obj.put("firstName", "Khanh");
            obj.put("lastName", "Tran");
            obj.put("GoogleID", "Google1");
            obj.put("dateCreated", "2016-10-09");
            //out.writeUTF("{ \"firstName\": \"Khanh\", \"lastName\": \"Tran\", \"GoogleID\": \"Googly1\", \"dateCreated\": \"2016-10-09\", \"age\": 20 }");
            out.writeUTF(obj.toString());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}