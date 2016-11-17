/*
 * Created by Khanh Tran on 10/9/16.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings({"unchecked", "InfiniteLoopStatement"})
class Server extends Thread {
    static final private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final private String DB_URL = "jdbc:mysql://localhost/Butterfly";
    static final private String USER = "root";
    static private String appKey, PASS, Authorization;
    private Connection conn;
    private ServerSocket serverSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private String sql;
    private PreparedStatement ps;
    private ResultSet rs;
    private JSONParser parser;

    private Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    public void run() {
        System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
        while (true) {
            try {
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                parser = new JSONParser();
                in = new DataInputStream(server.getInputStream());
                out = new DataOutputStream(server.getOutputStream());
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                conn.getMetaData().getCatalogs();
                conn.createStatement();
                while (in.available() > 0) {
                    Date currentDate = Calendar.getInstance().getTime();
                    System.out.println(currentDate);
                    Object parsed = parser.parse(in.readUTF());
                    JSONObject obj = (JSONObject) parsed;
                    switch ((String) obj.get("function")) {
                        case "addCommunity":
                            addCommunity(obj);
                            break;
                        case "addCommunityUser":
                            addCommunityUser(obj);
                            break;
                        case "addEvent":
                            addEvent(obj);
                            break;
                        case "addMessage":
                            addMessage(obj);
                            break;
                        case "addUser":
                            addUser(obj);
                            break;
                        case "deleteEvent":
                            deleteEvent(obj);
                            break;
                        case "editEvent":
                            editEvent(obj);
                            break;
                        case "emailInvite":
                            emailInvite((String) obj.get("from"), (String) obj.get("fromName"),
                                    (String) obj.get("to"));
                            break;
                        case "genericNotification":
                            genericNotification(getInstanceID((String) obj.get("googleID")),
                                    (String) obj.get("message"));
                            break;
                        case "getCommunities":
                            getCommunities();
                            break;
                        case "getCommunityUsers":
                            getCommunityUsers((String) obj.get("communityName"));
                            break;
                        case "getEvents":
                            getEvents((String) obj.get("communityName"));
                            break;
                        case "getMessages":
                            getMessages((String) obj.get("communityName"));
                            break;
                        case "getNeighborhoodEvents":
                            getNeighborhoodEvents();
                            break;
                        case "getUserCommunities":
                            getUserCommunities((String) obj.get("googleID"));
                            break;
                        case "getUserCommunityEvents":
                            getUserCommunityEvents((String) obj.get("googleID"));
                            break;
                        case "leaveCommunityUser":
                        case "removeCommunityUser":
                            leaveCommunityUser(obj);
                            break;
                        case "pingNotification":
                            pingNotification((String) obj.get("googleID"));
                            break;
                        case "upcomingEventNotification":
                            upcomingEventNotificationCheck((String) obj.get("googleID"));
                            break;
                        case "updateInstanceID":
                            updateInstanceID(obj);
                            break;
                        case "updateUserProfile":
                            updateUserProfile(obj);
                            break;
                    }
                }
            } catch (ParseException | IOException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void addCommunity(JSONObject obj) {
        //calls createCommunityBoardTable, createCommunityEventTable, createCommunityUserTable
        String neighborhoodID = "purdue";
        String name = (String) obj.get("name");
        try {
            sql = "SELECT count(*) from Communities WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    sql = "INSERT INTO Communities (neighborhoodID, category, subcategory, name, "
                            + "description, numMembers, numUpcomingEvents, dateCreated, private) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, neighborhoodID);
                    ps.setString(2, (String) obj.get("category"));
                    ps.setString(3, (String) obj.get("subCategory"));
                    ps.setString(4, name);
                    ps.setString(5, (String) obj.get("description"));
                    ps.setString(6, "1");
                    ps.setString(7, "0");
                    ps.setString(8, (String) obj.get("dateCreated"));
                    ps.setString(9, (String) obj.get("private"));
                    System.out.println(ps);
                    ps.executeUpdate();
                    name = name.replaceAll("\\s", "_");
                    createCommunityBoardTable(name);
                    createCommunityEventTable(name);
                    createCommunityUserTable(name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCommunityUser(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT count(*) from " + communityName + " WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("addCommunityUser " + Integer.toString(rs.getInt(1)));
                if (rs.getInt(1) == 0) {
                    sql = "INSERT INTO " + communityName + " (googleID) VALUES (?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();
                    sql = "UPDATE " + communityName + " SET isLeader = ? WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("isLeader"));
                    ps.setString(2, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();
                    sql = "UPDATE Users SET communitiesList = " +
                            "CONCAT(?, communitiesList) WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, obj.get("communityName") + ", ");
                    ps.setString(2, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();

                    /*sql = "SELECT communitiesList FROM Users WHERE googleID = ?";
                    ps.setString(1, googleID);
                    System.out.println(ps);
                    rs = ps.executeQuery(sql);
                    if (rs.next()) {

                    }*/
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addEvent(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        try {
            sql = "INSERT INTO " + communityName + " (eventName, description, date, time, city, "
                    + "state, address, zip, locationName, numAttendees) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, (String) obj.get("eventName"));
            ps.setString(2, (String) obj.get("description"));
            ps.setString(3, (String) obj.get("date"));
            ps.setString(4, (String) obj.get("time"));
            ps.setString(5, (String) obj.get("city"));
            ps.setString(6, (String) obj.get("state"));
            ps.setString(7, (String) obj.get("address"));
            ps.setString(8, (String) obj.get("zip"));
            ps.setString(9, (String) obj.get("locationName"));
            ps.setString(10, (String) obj.get("numAttendees"));
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_"); communityName += "_Board";
        String name = (String) obj.get("name"); String message = (String) obj.get("message");
        try {
            sql = "INSERT INTO " + communityName + " (pinned, name, date, message) VALUES(?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, (String) obj.get("pinned"));
            ps.setString(2, name);
            ps.setString(3, (String) obj.get("date"));
            ps.setString(4, message);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        newBoardNotification(communityName, name, message);
    }

    private void addUser(JSONObject obj) {
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT count(*) from Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("addUser " + Integer.toString(rs.getInt(1)));
                if (rs.getInt(1) == 0) {
                    sql = "INSERT INTO Users (firstName, lastName, googleID) "
                            + "VALUES (?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("firstName"));
                    ps.setString(2, (String) obj.get("lastName"));
                    ps.setString(3, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityBoardTable(String communityName) {
        //called by addCommunity
        communityName += "_Board";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idEvents INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, "
                + "pinned INT(4), " + "name VARCHAR(255), " + "date DATE, "
                + "message TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityEventTable(String communityName) {
        //called by addCommunity
        //DATETIME: YYYY-MM-DD HH:MM:SS, DATE: YYYY-MM-DD
        //SELECT TIME_FORMAT('21:46:25', '%r') = 09:46:25 PM
        communityName += "_Calendar";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idEvents INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, " + "eventName VARCHAR(255), "
                + "description VARCHAR(255), " + "date DATE, " + "time TIME, " + "city VARCHAR(255), "
                + "state VARCHAR(255), " + "address VARCHAR(255), " + "zip VARCHAR(255), "
                + "locationName VARCHAR(255), " + "numAttendees INT(4))";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityUserTable(String communityName) {
        //called by addCommunity
        System.out.println(communityName);
        communityName += "_Users";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idUsers INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, " + "googleID VARCHAR(255), "
                + "isLeader TINYINT(1), " + "subscribed INT(1))";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteEvent(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        try {
            sql = "DELETE FROM " + communityName + " WHERE name = " + obj.get("name");
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editEvent(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        String eventName = (String) obj.get("eventName");
        try {
            sql = "SELECT * FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (obj.get("eventName") != null && obj.get("eventName") != rs.getString("eventName")) {
                    sql = "UPDATE Users SET eventName = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, eventName);
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("description") != null &&
                        obj.get("description") != rs.getString("description")) {
                    sql = "UPDATE Users SET description = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("description"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("date") != null && obj.get("date") != rs.getString("date")) {
                    sql = "UPDATE Users SET date = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("date"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("time") != null && obj.get("time") != rs.getString("time")) {
                    sql = "UPDATE Users SET time = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("time"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("city") != null && obj.get("city") != rs.getString("city")) {
                    sql = "UPDATE Users SET city = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("city"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("state") != null && obj.get("state") != rs.getString("state")) {
                    sql = "UPDATE Users SET state = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("state"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("address") != null && obj.get("address") != rs.getString("address")) {
                    sql = "UPDATE Users SET address = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("address"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("zip") != null && obj.get("zip") != rs.getString("zip")) {
                    sql = "UPDATE Users SET zip = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("zip"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("locationName") != null &&
                        obj.get("locationName") != rs.getString("locationName")) {
                    sql = "UPDATE Users SET locationName = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("locationName"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("numAttendees") != null &&
                        obj.get("numAttendees") != rs.getString("numAttendees")) {
                    sql = "UPDATE Users SET numAttendees = ? WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("numAttendees"));
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void emailInvite(String from, String fromName, String to) {
        final String password = appKey;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(from, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Butterfly Invite");
            message.setText(fromName + " wants you to use Butterfly");
            Transport.send(message);
            System.out.println("Invite Sent from: " + from + " to: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void genericNotification(String to, String message) {
        //TODO #10?
        //JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject parent = new JSONObject();
        notification.put("body", message);
        notification.put("title", "TITLE");
        parent.put("to", to);
        parent.put("notification", notification);
        parent.put("priority", "high");
        System.out.println("sent " + parent.toString());
        notificationWrite(parent);
    }

    private void getCommunities() {
        String comma = ", ";
        StringBuilder communities = new StringBuilder();
        try {
            sql = "SELECT * FROM Communities";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                communities.append(rs.getString("name"));
                communities.append(comma);
            }
            //communities.setLength(communities.length() - 2);
            System.out.println(communities);
            out.writeUTF(communities.toString());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getCommunityUsers(String communityName) {
        communityName += "_Users";
        try {
            sql = "SELECT * FROM " + communityName;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            JSONObject obj;
            while (rs.next()) {
                obj = new JSONObject();
                obj.put("idUsers", rs.getString("idUsers"));
                obj.put("firstName", rs.getString("firstName"));
                obj.put("lastName", rs.getString("lastName"));
                obj.put("googleID", rs.getString("googleID"));
                System.out.println(obj.toString());
                out.writeUTF(obj.toString());
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getEvents(String communityName) {
        String oldName = communityName;
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        try {
            sql = "SELECT count(*) from " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                out.writeUTF(Integer.toString(rs.getInt(1)));
                sql = "SELECT * FROM " + communityName;
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                rs = ps.executeQuery();
                JSONObject obj;
                while (rs.next()) {
                    obj = new JSONObject();
                    obj.put("eventName", rs.getString("eventName"));
                    obj.put("city", rs.getString("city"));
                    obj.put("date", rs.getString("date"));
                    obj.put("time", rs.getString("time"));
                    obj.put("address", rs.getString("address"));
                    obj.put("state", rs.getString("state"));
                    obj.put("zip", rs.getString("zip"));
                    obj.put("description", rs.getString("description"));
                    obj.put("communityName", oldName);
                    obj.put("locationName", rs.getString("locationName"));
                    obj.put("numAttendees", rs.getString("numAttendees"));
                    System.out.println(obj.toString());
                    out.writeUTF(obj.toString());
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private String getInstanceID(String googleID) {
        //function passed to genericNotification
        try {
            sql = "SELECT * from Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println(rs.getString("googleID"));
                return rs.getString("instanceID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void getMessages(String communityName) {
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Board";
        JSONObject obj;
        try {
            sql = "SELECT count(*) from " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                out.write(rs.getInt(1));
                sql = "SELECT * FROM " + communityName;
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    obj = new JSONObject();
                    obj.put("pinned", rs.getString("pinned"));
                    obj.put("name", rs.getString("name"));
                    obj.put("date", rs.getString("date"));
                    obj.put("message", rs.getString("message"));
                    System.out.println(obj);
                    out.writeUTF(obj.toString());
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getNeighborhoodEvents() {
        try {
            sql = "SELECT name FROM Communities";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                ArrayList<String> communities = new ArrayList<>();
                communities.add(rs.getString("name"));
                out.write(communities.size());
                communities.forEach(this::getEvents);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserCommunities(String googleID) {
        try {
            sql = "SELECT communitiesList FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            String list;
            if (rs.next()) {
                list = rs.getString("communitiesList");
                out.writeUTF(list);
            }
        } catch (SQLException |IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserCommunityEvents(String googleID) {
        try {
            sql = "SELECT communitiesList FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            String list;
            if (rs.next()) {
                list = rs.getString("communitiesList");
                ArrayList<String> communities = new ArrayList<>(Arrays.asList(list.split(", ")));
                out.writeUTF(Integer.toString(communities.size()));
                for (String community: communities) {
                    out.writeUTF(community);
                    getEvents(community);
                }
            }
        } catch (SQLException |IOException e) {
            e.printStackTrace();
        }
    }

    private void leaveCommunityUser(JSONObject obj) {
        //TODO call updateUserProfile and update communitiesList
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT count(*) from " + communityName + " WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 1) {
                    sql = "DELETE FROM " + communityName + " WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();
                    sql = "SELECT communitiesList FROM Users WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, googleID);
                    System.out.println(ps);
                    rs = ps.executeQuery();
                    String list;
                    if (rs.next()) {
                        list = rs.getString("communitiesList");
                        ArrayList<String> communities;
                        communities = new ArrayList<>(Arrays.asList(list.split(", ")));
                        StringBuilder string = new StringBuilder();
                        for (String community : communities) {
                            if(community.contains((String) obj.get("communityName"))) {
                                System.out.println("found " + community);
                            } else {
                                string.append(rs.getString("name"));
                                string.append(", ");
                            }
                        }
                        sql = "UPDATE Users SET communitiesList = ? WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, string.toString());
                        ps.setString(2, googleID);
                        System.out.println(ps);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void newBoardNotification(String community, String name, String message) {
        // called by addMessage
        JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject parent = new JSONObject();
        notification.put("body", name + " has posted a new message");
        notification.put("title", "New Message");
        data.put("body", message);
        parent.put("to", "/topics/" + community);
        parent.put("notification", notification);
        parent.put("data", data);
        parent.put("priority", "high");
        notificationWrite(parent);
        /*ArrayList<String> IDs = getSubscribedMembers(community);
        for (String ID : IDs) {
            String to = getInstanceID(ID);
            parent.put("to", to);
            System.out.println(parent);
            notificationWrite(parent);
        }*/
    }

    private void notificationWrite(JSONObject parent) {
        //called by genericNotification and newBoardNotification
        System.out.println(parent.toString());
        try {
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setDoOutput(true);
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Authorization", Authorization);
            OutputStream os = http.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(parent.toString());
            osw.flush();
            osw.close();
            BufferedReader is = new BufferedReader(new InputStreamReader(http.getInputStream()));
            String response;
            while ((response = is.readLine()) != null) {
                System.out.println(response);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pingNotification(String to) {
        //TODO what is a ping, what does it contain
        // just says ping to all members
    }

    private void timeCheck(String communityCalendar) {
        // called by upcomingEventNotification
        // TIME: HH:MM:SS, DATE: YYYY-MM-DD
        try {
            sql = "SELECT * FROM " + communityCalendar;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String eventDate = rs.getString("date");
                String eventTime = rs.getString("time");
                ArrayList<String> dateSplit;
                dateSplit = new ArrayList<>(Arrays.asList(eventDate.toString().split("-")));
                ArrayList<String> timeSplit;
                timeSplit = new ArrayList<>(Arrays.asList(eventTime.toString().split(":")));
                Date date = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                if (month == Integer.parseInt(dateSplit.get(1))) {
                    if (Integer.parseInt(dateSplit.get(2)) - day == 1) {
                        //send notification
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void upcomingEventNotificationCheck(String to) {
        /*TODO figure out when it is 24 hours before an event time
          TODO send notification to all users in that community
          TODO will have to be always running to be prompt
        */
        try {
            sql = "SELECT name FROM Communities";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                ArrayList<String> communities = new ArrayList<>();
                String temp = rs.getString("name").replaceAll("\\s", "_");
                temp += "_Calendar";
                communities.add(temp);
                communities.forEach(this::timeCheck);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateInstanceID(JSONObject obj) {
        try {
            sql = "UPDATE Users SET instanceID = ? WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, (String) obj.get("instanceID"));
            ps.setString(2, (String) obj.get("googleID"));
            System.out.println(ps);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUserProfile(JSONObject obj) {
        //TODO other variables for user profile
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT * FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (obj.get("firstName") != null && obj.get("firstName") != rs.getString("firstName")) {
                    sql = "UPDATE Users SET firstName = ? WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("firstName"));
                    ps.setString(2, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("lastName") != null && obj.get("lastName") != rs.getString("lastName")) {
                    sql = "UPDATE Users SET lastName = ? WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("lastName"));
                    ps.setString(2, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                if (obj.get("birthDate") != null && obj.get("birthDate") != rs.getString("birthDate")) {
                    sql = "UPDATE Users SET birthDate = ? WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("birthDate"));
                    ps.setString(2, googleID);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkifLeader(JSONObject obj) {
        /*String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        try {
            sql = "SELECT isLeader from " + communityName + " WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, (String) obj.get("googleID"));
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 1) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        return false;
    }

    private ArrayList<String> getSubscribedMembers(String communityName) {
        ArrayList<String> subscribedGoogleIDs = new ArrayList<>();
        /*try {
            sql = "SELECT * from " + communityName + "_Users WHERE subscribed = '1'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                subscribedGoogleIDs.add(rs.getString("googleID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        return subscribedGoogleIDs;
    }

    public static void main(String[] args) {
        Date currentDate = Calendar.getInstance().getTime();
        System.out.println(currentDate);
        try {
            FileInputStream fileIn = new FileInputStream("/home/khanh/keys.txt");
            InputStreamReader isr = new InputStreamReader(fileIn, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            appKey = br.readLine();
            PASS = br.readLine();
            Authorization = br.readLine();
            br.close();
            isr.close();
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setServiceAccount(new FileInputStream("/home/khanh/Butterfly-40ec2c75b546.json"))
                    .setDatabaseUrl("butterfly-145620.firebaseio.com/")
                    .build();
            FirebaseApp.initializeApp(options);
            Thread t = new Server(3300);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
