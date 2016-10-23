/**
 * Created by nick on 10/9/16.
 */

import java.net.*;
import java.io.*;
import java.sql.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class Server extends Thread {
    private ServerSocket serverSocket;
    static final private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final private String DB_URL = "jdbc:mysql://localhost/Butterfly";
    static final private String USER = "root";
    static final private String PASS = "Ghost999";
    static private Connection conn = null;
    static private Statement stmt = null;
    static private ResultSet rs;
    static private String sql;
    static private long id;

    private Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void run() {

        while (true) {
            try {
                URL whatismyip;
                whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in;
                in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                String ip = in.readLine();
                System.out.println(ip);
            } catch(Exception e) {
                e.printStackTrace();
            }

            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                rs = conn.getMetaData().getCatalogs();
                stmt = conn.createStatement();
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                JSONParser parser = new JSONParser();
                DataInputStream is = new DataInputStream(server.getInputStream());
                Object obj = parser.parse(is.readUTF());
                JSONObject obj2 = (JSONObject) obj;
                String function = (String) obj2.get("function");
                if (function.equals("addUser")) {
                    addUser(obj2);
                } else if (function.equals("addCommunity")) {
                    String name = (String) obj2.get("communityName");
                    sql = "insert into Communities (name) values('" + name + "')";
                    stmt.execute(sql);
                } else if (function.equals("addEvent")) {
                    String eventName = (String) obj2.get("eventName");
                    String eventTime = (String) obj2.get("eventTime");
                    String desc = (String) obj2.get("description");
                    String location = (String) obj2.get("locationName");
                    sql = "INSERT INTO Events (Name, eventTime, description, location) values('" + eventName + "', '" + eventTime + "', '" + desc + "', '" + location +"')";
                } else if (function.equals("leaveCommunity")) {

                }

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ParseException p) {

                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addUser(JSONObject obj) {
        String first = addWcomma((String) obj.get("firstName"));
        String last = addWcomma((String) obj.get("lastName"));
        String google = addWcomma((String) obj.get("GoogleID"));
        String dateCreated = addEnd((String) obj.get("dateCreated"));
        try {
            String queryCheck = "SELECT count(*) from Users WHERE GoogleID = '" + obj.get("GoogleID") + "'";
            System.out.println(queryCheck);
            rs = stmt.executeQuery(queryCheck);
            if(rs.next()) {
                if (rs.getInt(1) == 0) {
                    String sql = "INSERT INTO Users VALUES('7', " + first + last + google + dateCreated + ")";
                    System.out.println(sql);
                    stmt.execute(sql);
                } else {
                    
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String addFirst(String value) {
        String formatted = "'";
        formatted += value;
        formatted += "' ";
        return formatted;
    }

    private String addWcomma(String value) {
        String formatted = "'";
        formatted += value;
        formatted += "', ";
        return formatted;
    }

    private String addEnd(String value) {
        String formatted = "'";
        formatted += value;
        formatted += "'";
        return formatted;
    }

    public static void main(String[] args) {
        int port = 60660;
        try {
            Thread t = new Server(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
}
