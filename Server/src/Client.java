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
            JSONObject obj2 = new JSONObject();
            /*obj.put("function", "addUser"); obj.put("firstName", "Khanh"); obj.put("lastName", "Tran");
            obj.put("GoogleID", "newcdragon@gmail.com"); obj.put("dateCreated", "2016-10-09");*/

            /*obj.put("function", "addCommunity"); obj.put("category", "sports");
            obj.put("subCategory", "soccer"); obj.put("name", "Chelsea FC");
            obj.put("description", "London is Blue"); obj.put("dateCreated", "2016-10-25");
            obj.put("private", "0");*/

            /*obj.put("function", "addEvent"); obj.put("name", "West Ham vs Chelsea");
            obj.put("description", "Football League Cup Round of 16"); obj.put("city", "London");
            //obj.put("state", "MO"); obj.put("zipcode", "63103");
            obj.put("address", "Queen Elizabeth Olympic Park, London E20 2ST, United Kingdom");
            obj.put("locationName", "London Stadium");
            obj.put("numAttendees", "0"); obj.put("communityName", "Chelsea FC");*/

            //obj2.put("function", "getEvents"); obj2.put("communityName", "Red Wings");
            //obj2.put("function", "getNeighborhoodEvents");
            obj.put("function", "emailInvite"); obj.put("from", "newcdragon@gmail.com");
            obj.put("to", "newcdragon@hotmail.com"); obj.put("fromName", "Khanh");
            out.writeUTF(obj.toString());
            //out.writeUTF(obj2.toString());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
            System.out.println("Server says " + in.readUTF());
            System.out.println("Server says " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}