/**
 * Created by khanh on 10/9/16.
 */

import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;

public class Client {
    public static void main(String[] args) {
        String serverName = "128.211.225.79";
        int port = 60660;
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            JSONObject obj = new JSONObject();
            /*obj.put("function", "addUser"); obj.put("firstName", "Khanh"); obj.put("lastName", "Tran");
            obj.put("GoogleID", "Google1"); obj.put("dateCreated", "2016-10-09");*/
            /*obj.put("function", "addCommunity"); obj.put("category", "sports");
            obj.put("subCategory", "hockey"); obj.put("name", "Red Wings");
            obj.put("description", "Best Original 6"); obj.put("dateCreated", "2016-10-23");
            obj.put("private", "0");*/
            obj.put("function", "addEvent"); obj.put("name", "Tonight's Game");
            obj.put("description", "Hurricanes at Red Wings"); obj.put("city", "Detroit");
            obj.put("state", "MI"); obj.put("address", "19 Steve Yzerman Dr");
            obj.put("zipcode", "48226"); obj.put("locationName", "Joe Louis Arena");
            obj.put("numAttendees", "0"); obj.put("communityName", "Red Wings");
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