/**
 * Created by Khanh Tran on 10/9/16.
 */

import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;

public class Client {
    public static void main(String[] args) {
        String serverName = "128.211.225.79";
        //String serverName = "10.186.90.80";
        int port = 3300;
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            JSONObject obj;
            JSONObject obj2;

            /*obj = new JSONObject();
            obj.put("function", "addUser"); obj.put("firstName", "Khanh");
            obj.put("lastName", "Tran");
            obj.put("googleID", "newcdragon@gmail.com");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "updateInstanceID");
            obj.put("googleID", "newcdragon@gmail.com");
            obj.put("instanceID", "ertyui178394iujrhtnbg");
            out.writeUTF(obj.toString());*/

            /*obj2.put("function", "addUser"); obj2.put("firstName", "Larry");
            obj2.put("lastName", "Bird");
            obj2.put("googleID", "LarryB@gmail.com");
            out.writeUTF(obj2.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunity");
            //obj.put("category", "Academic");
            //obj.put("subCategory", "CS");
            obj.put("name", "CS307");
            //obj.put("description", "project class"); obj.put("dateCreated", "2016-10-28");
            //obj.put("private", "0");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunityUser"); obj.put("communityName", "CS307");
            obj.put("idUsers", "1");
            obj.put("isLeader", "1");
            out.writeUTF(obj.toString());*/
            /*obj = new JSONObject();
            obj.put("function", "addCommunityUser"); obj.put("communityName", "CS307");
            obj.put("idUsers", "2");
            obj.put("isLeader", "0");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "leaveCommunityUser"); obj.put("communityName", "CS307");
            obj.put("idUsers", "2");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunity"); obj.put("category", "sports");
            obj.put("subCategory", "soccer"); obj.put("name", "Chelsea FC");
            obj.put("description", "London Is Blue"); obj.put("dateCreated", "2016-10-28");
            obj.put("private", "0");
            out.writeUTF(obj.toString());

            obj = new JSONObject();
            obj.put("function", "addCommunity"); obj.put("category", "sports");
            obj.put("subCategory", "hockey"); obj.put("name", "Red Wings");
            obj.put("description", "Best Original 6"); obj.put("dateCreated", "2016-10-30");
            obj.put("private", "0");
            out.writeUTF(obj.toString());

            obj = new JSONObject();
            obj.put("function", "addCommunity"); obj.put("category", "Academic");
            obj.put("subCategory", "CS"); obj.put("name", "CS307");
            obj.put("description", "project class"); obj.put("dateCreated", "2016-10-28");
            obj.put("private", "0");
            out.writeUTF(obj.toString());

            obj = new JSONObject();
            obj.put("function", "addEvent"); obj.put("name", "West Ham vs Chelsea");
            obj.put("description", "Football League Cup Round of 16"); obj.put("city", "London");
            //obj.put("state", "MO"); obj.put("zip", "63103");
            obj.put("address", "Queen Elizabeth Olympic Park, London E20 2ST, United Kingdom");
            obj.put("locationName", "London Stadium");
            obj.put("numAttendees", "0"); obj.put("communityName", "Chelsea FC");
            out.writeUTF(obj.toString());

            obj2 = new JSONObject();
            obj2.put("function", "getEvents"); obj2.put("communityName", "Chelsea FC");
            out.writeUTF(obj2.toString());
            System.out.println("Server says " + in.readUTF());*/

            //obj2.put("function", "getNeighborhoodEvents");
            //out.writeUTF(obj2.toString());
            /*obj.put("function", "emailInvite"); obj.put("from", "newcdragon@gmail.com");
            obj.put("to", "newcdragon@hotmail.com"); obj.put("fromName", "Khanh");*/

            /*obj2 = new JSONObject();
            obj2.put("function", "getCommunities");
            out.writeUTF(obj2.toString());
            System.out.println("Server says " + in.readUTF());*/
            //while(true) {
                obj = new JSONObject();
                obj.put("function", "genericNotification");
                obj.put("idUsers", "2");
                obj.put("message", "haha");
                out.writeUTF(obj.toString());
            //}
            //System.out.println("Server says " + in.readUTF());
            //System.out.println("Server says " + in.readUTF());
            //System.out.println("Server says " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}