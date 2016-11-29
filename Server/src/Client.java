/**
 * Created by Khanh Tran on 10/9/16.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class Client {
    public static void main(String[] args) {
        String serverName = "128.211.225.79";
        //String serverName = "10.186.89.115";
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

            /*obj = new JSONObject();
            obj.put("function", "updateInstanceID");
            obj.put("googleID", "hamzafarrukh141@gmail.com");
            obj.put("instanceID", "iasudbasudfn58ha");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "getUserCommunities");
            obj.put("googleID", "hamzafarrukh141@gmail.com");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addUser");
            obj.put("firstName", "Khanh");
            obj.put("lastName", "Tran");
            obj.put("googleID", "newcdragon@gmail.com");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "updateInstanceID");
            obj.put("googleID", "newcdragon@gmail.com");
            obj.put("instanceID", "ertyui178394iujrhtnbg");
            out.writeUTF(obj.toString());*/

            /*obj2.put("function", "addUser");
            obj2.put("firstName", "Larry");
            obj2.put("lastName", "Bird");
            obj2.put("googleID", "LarryB@gmail.com");
            out.writeUTF(obj2.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunity");
            //obj.put("category", "Academic");
            //obj.put("subCategory", "CS");
            obj.put("name", "CS307");
            //obj.put("description", "project class");
            obj.put("dateCreated", "2016-10-28");
            //obj.put("private", "0");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunityUser");
            obj.put("communityName", "CS307");
            obj.put("idUsers", "1");
            obj.put("isLeader", "1");
            out.writeUTF(obj.toString());*/
            /*obj = new JSONObject();
            obj.put("function", "addCommunityUser");
            obj.put("communityName", "CS307");
            obj.put("idUsers", "2");
            obj.put("isLeader", "0");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "leaveCommunityUser");
            obj.put("communityName", "CS307");
            obj.put("idUsers", "2");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunity");
            obj.put("category", "sports");
            obj.put("subCategory", "soccer");
            obj.put("name", "Chelsea FC");
            obj.put("description", "London Is Blue");
            obj.put("dateCreated", "2016-10-28");
            obj.put("private", "0");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunity");
            obj.put("category", "sports");
            obj.put("subCategory", "hockey");
            obj.put("name", "Red Wings");
            obj.put("description", "Best Original 6");
            obj.put("dateCreated", "2016-10-30");
            obj.put("private", "0");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addCommunity"); obj.put("category", "Academic");
            obj.put("subCategory", "CS"); obj.put("name", "CS307");
            obj.put("description", "project class"); obj.put("dateCreated", "2016-10-28");
            obj.put("private", "0");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "deleteEvent"); obj.put("communityName", "hahaha");
            obj.put("eventName", "hahaha");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "getCommunityUsers"); obj.put("communityName", "hahaha");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addEvent");
            obj.put("eventName", "West Ham vs Chelsea");
            obj.put("description", "Football League Cup Round of 16");
            obj.put("date", "2016-11-26");
            obj.put("time", "15:00");
            obj.put("city", "London");
            //obj.put("state", "MO");
            obj.put("zip", "63103");
            obj.put("address", "Queen Elizabeth Olympic Park, London E20 2ST, United Kingdom");
            obj.put("locationName", "London Stadium");
            obj.put("numAttendees", "1");
            obj.put("listAttendees", "newcdragon@gmail.com");
            obj.put("communityName", "Red Wings");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "upcomingEventNotification");
            out.writeUTF(obj.toString());*/

            /*obj2 = new JSONObject();
            obj2.put("function", "getEvents"); obj2.put("communityName", "hahaha");
            out.writeUTF(obj2.toString());
            System.out.println("Server says " + in.readUTF());*/

            //obj2.put("function", "getNeighborhoodEvents");
            //out.writeUTF(obj2.toString());

            obj = new JSONObject();
            obj.put("function", "emailInvite");
            obj.put("to", "newcdragon@hotmail.com"); obj.put("fromName", "Khanh");
            out.writeUTF(obj.toString());

            /*obj2 = new JSONObject();
            obj2.put("function", "getCommunities");
            out.writeUTF(obj2.toString());
            System.out.println("Server says " + in.readUTF());*/

            /*obj = new JSONObject();
            obj.put("function", "genericNotification");
            obj.put("googleID", "newcdragon@gmail.com");
            obj.put("message", "ready for spam??");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addHangout");
            obj.put("creator", "Khanh");
            obj.put("communityName", "Red Wings");
            obj.put("hangoutName", "Tonight's Game");
            obj.put("startTime", "19:30");
            obj.put("endTime", "22:00");
            obj.put("date", "2016-11-25");
            obj.put("locationName", "My place");
            obj.put("googleID", "newcdragon@gmail.com");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "addHangoutUser");
            obj.put("communityName", "Red Wings");
            obj.put("hangoutName", "Tonight's Game");
            obj.put("googleID", "test@gmail.com");
            out.writeUTF(obj.toString());*/

            /*obj = new JSONObject();
            obj.put("function", "communitySearch");
            obj.put("type", "subcategory");
            obj.put("value", "hockey");
            out.writeUTF(obj.toString());*/

            //System.out.println("Server says 1 " + in.readUTF());
            //System.out.println("Server says 2 " + in.readUTF());
            //System.out.println("Server says " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}