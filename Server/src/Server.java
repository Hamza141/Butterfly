/**
 * Created by nick on 10/9/16.
 */

import java.util.*;
import java.net.*;
import java.io.*;
import java.sql.*;
/*import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;*/
import org.json.simple.parser.*;
import org.json.simple.JSONObject;


public class Server extends Thread {
    private ServerSocket serverSocket;
    static final private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final private String DB_URL = "jdbc:mysql://localhost/Butterfly";
    static final private String USER = "root";
    static final private String PASS = "Ghost999";
    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet rs;
    private String queryCheck;
    private PreparedStatement ps;
    static private long id;
    static private DataOutputStream out;

    private Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        try {
            URL myip = new URL("http://checkip.amazonaws.com");
            BufferedReader buffIn = new BufferedReader(new InputStreamReader(myip.openStream()));
            String ip = buffIn.readLine();
            System.out.println(ip);
        } catch(Exception e) {
            e.printStackTrace();
        }
        while (true) {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            try {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                rs = conn.getMetaData().getCatalogs();
                stmt = conn.createStatement();
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                JSONParser parser = new JSONParser();
                DataInputStream in = new DataInputStream(server.getInputStream());
                out = new DataOutputStream(server.getOutputStream());
                Object obj = parser.parse(in.readUTF());
                JSONObject obj2 = (JSONObject) obj;
                String function = (String) obj2.get("function");
                if (function.equals("addUser")) {
                    addUser(obj2);
                } else if (function.equals("addCommunityUser")) {
                    addCommunityUser((String) obj2.get("communityName"));
                } else if (function.equals("addCommunity")) {
                    addCommunity(obj2);
                } else if (function.equals("addEvent")) {
                    addEvent(obj2);
                } else if (function.equals("getEvents")) {
                    getEvents((String) obj2.get("communityName"));
                } else if (function.equals("getNeighborhoodEvents")) {
                    getNeighborhoodEvents();
                } else if (function.equals("leaveCommunity")) {
                    leaveCommunity(obj2);
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

    private void leaveCommunity(JSONObject obj) {

    }

    private void getNeighborhoodEvents() {
        String sql = "SELECT name FROM Communities";
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            ArrayList<String> communities = new ArrayList<String>();
            while (rs.next()) {
                communities.add(rs.getString("name"));
            }
            for (String name : communities) {
                System.out.println("Community " + name);
                getEvents(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getEvents(String communityName) {
        try {
            String sql = "SELECT * FROM `" + communityName + " Calendar`";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            JSONObject obj;
            while (rs.next()) {
                obj = new JSONObject();
                obj.put("name", rs.getString("name"));
                obj.put("description", rs.getString("description"));
                obj.put("date", rs.getString("date")); obj.put("city", rs.getString("city"));
                obj.put("state", rs.getString("state")); obj.put("address", rs.getString("address"));
                obj.put("zipcode", rs.getString("zipcode"));
                obj.put("locationName", rs.getString("locationName"));
                obj.put("numAttendees", rs.getString("numAttendees"));
                System.out.println(obj.toString());
                out.writeUTF(obj.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addEvent(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName += " Calendar";
        String idEvents = "0";
        String name = (String) obj.get("name");
        String description = (String) obj.get("description");
        String date = (String) obj.get("date");
        String time = (String) obj.get("time");
        String city = (String) obj.get("city");
        String state = (String) obj.get("state");
        String address = (String) obj.get("address");
        String zipcode = (String) obj.get("zipcode");
        String locationName = (String) obj.get("locationName");
        String numAttendees = (String) obj.get("numAttendees");
        try {
            String sql = "INSERT INTO `" + communityName + "` VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, idEvents); ps.setString(2, name);
            ps.setString(3, description); ps.setString(4, date); ps.setString(5, city);
            ps.setString(6, state); ps.setString(7, address); ps.setString(8, zipcode);
            ps.setString(9, locationName); ps.setString(10, numAttendees);
            System.out.println(ps);
            ps.executeUpdate();
            out.writeUTF("0");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityUserTable(String communityName) {
        //DATE: YYYY-MM-DD
        String newTable = "CREATE TABLE `" + communityName + " Users` ("
            + "idUsers INT(4), " + "firstName VARCHAR(255), " + "lastName VARCHAR(255), "
            + "googleID VARCHAR(255), " + "dateCreated DATE)";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityEventTable(String communityName) {
        // DATETIME: YYYY-MM-DD HH:MM:SS, DATE: YYYY-MM-DD
        String newTable = "CREATE TABLE `" + communityName + " Calendar` ("
            + "idEvents INT(4), " + "name VARCHAR(255), " + "description VARCHAR(255), "
            + "date DATE, " + "city VARCHAR(255), " + "state VARCHAR(255), "
            + "address VARCHAR(255), " + "zipcode VARCHAR(255), " + "locationName VARCHAR(255), "
            + "numAttendees INT(4))";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCommunity(JSONObject obj) {
        String idCommunities = "0";
        String neighboorhoodID = "0";
        String category = (String) obj.get("category");
        String subCategory = (String) obj.get("subCategory");
        String name = (String) obj.get("name");
        String description = (String) obj.get("description");
        String numMembers = "1";
        String numUpcomgEvents = "0";
        String dateCreated = (String) obj.get("dateCreated");
        String Private = (String) obj.get("private");
        try {
            queryCheck = "SELECT count(*) from Communities WHERE name = ?";
            ps = conn.prepareStatement(queryCheck);
            ps.setString(1, name);
            System.out.println(ps);
            rs = ps.executeQuery();
            if(rs.next()) {
                if (rs.getInt(1) == 0) {
                    String sql = "INSERT INTO Communities VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, idCommunities); ps.setString(2, neighboorhoodID);
                    ps.setString(3, category); ps.setString(4, subCategory); ps.setString(5, name);
                    ps.setString(6, description); ps.setString(7, numMembers);
                    ps.setString(8, numUpcomgEvents); ps.setString(9, dateCreated);
                    ps.setString(10, Private);
                    System.out.println(ps);
                    ps.executeUpdate();
                    createCommunityUserTable(name);
                    createCommunityEventTable(name);
                } else {
                    out.writeUTF("1");
                }
            }
            out.writeUTF("0");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addCommunityUser(String communityName) {

    }

    private void addUser(JSONObject obj) {
        String first = (String) obj.get("firstName");
        String last = (String) obj.get("lastName");
        String google = (String) obj.get("GoogleID");
        String dateCreated = (String) obj.get("dateCreated");
        try {
            queryCheck = "SELECT count(*) from Users WHERE GoogleID = ?";
            ps = conn.prepareStatement(queryCheck);
            ps.setString(1, google);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    String sql = "INSERT INTO Users VALUES('7', ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, first);
                    ps.setString(2, last);
                    ps.setString(3, google);
                    ps.setString(4, dateCreated);
                    System.out.println(ps);
                    ps.executeUpdate();
                } else {
                    out.writeUTF("1");
                }
            }
            out.writeUTF("0");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
