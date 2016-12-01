//TODO check all SELECT name statements work correctly with rs.next, want all names at once into arraylist
/*
 * Created by Khanh Tran on 10/9/16.
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Blob;
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

import static java.lang.Thread.sleep;

@SuppressWarnings({"unchecked", "InfiniteLoopStatement"})
 class serverStart implements Runnable {
    static private String appKey, PASS, Authorization;
    private Connection conn;
    private DataOutputStream out;
    private String sql;
    private PreparedStatement ps;
    private ResultSet rs;
    private JSONObject obj;

    serverStart(JSONObject pobj, DataOutputStream pout) throws IOException {
        obj = pobj;
        out = pout;
        FileInputStream fileIn = new FileInputStream("/home/khanh/keys.txt");
        InputStreamReader isr = new InputStreamReader(fileIn, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        appKey = br.readLine();
        PASS = br.readLine();
        Authorization = br.readLine();
        br.close();
        isr.close();
    }

    public void run() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/Butterfly", "root", PASS);
            conn.getMetaData().getCatalogs();
            conn.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        switch ((String) obj.get("function")) {
            case "addCommunity":
                addCommunity(obj);
                break;
            case "addCommunityUser":
                addCommunityUser(obj);
                break;
            case "addHangoutUser":
                addHangoutUser(obj);
                break;
            case "addEvent":
                addEvent(obj);
                break;
            case "addHangout":
                addHangout(obj);
                break;
            case "addMessage":
                addMessage(obj);
                break;
            case "addUser":
                addUser(obj);
                break;
            case "communityInvite":
                communityInvite(obj);
                break;
            case "communitySearch":
                communitySearch((String) obj.get("type"), (String) obj.get("value"));
                break;
            case "checkIn":
                checkIn(obj);
                break;
            case "deleteEvent":
                deleteEvent(obj);
                break;
            case "editEvent":
                editEvent(obj);
                break;
            case "emailInvite":
                emailInvite("user", (String) obj.get("to"));
                break;
            case "genericNotification":
                genericNotification(getInstanceID((String) obj.get("googleID")),
                        (String) obj.get("message"), (String) obj.get("title"));
                break;
            case "getCommunities":
                getCommunities();
                break;
            case "getCommunityUsers":
                getCommunityUsers((String) obj.get("communityName"));
                break;
            case "getCommunityUsersWithID":
                getCommunityUsersWithID((String) obj.get("communityName"));
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
            case "getrsvp":
                getrsvp((String) obj.get("communityName"), (String) obj.get("eventName"));
                break;
            case "getUserCommunities":
                getUserCommunities((String) obj.get("googleID"));
                break;
            case "getUserCommunityEvents":
                getUserCommunityEvents((String) obj.get("googleID"));
                break;
            case "getUserName":
                getUserName((String) obj.get("googleID"));
                break;
            case "getUserModerator":
                getUserModerator((String) obj.get("googleID"));
                break;
            case "getUserProfile":
                getUserProfile((String) obj.get("googleID"));
                break;
            case "groupNotification":
                groupNotification(obj);
                break;
            case "leaveCommunityUser":
            case "removeCommunityUser":
                leaveCommunityUser(obj);
                break;
            case "removeAllCommunities":
                removeAllCommunities();
                break;
            case "removeCommunity":
                removeCommunity((String) obj.get("communityName"));
                break;
            case "rsvpEvent":
                rsvpEvent(obj);
                break;
            case "rsvpCheck":
                rsvpCheck(obj);
                break;
            case "rsvpEventRemove":
                rsvpEventRemove(obj);
                break;
            case "upcomingEventNotification":
                upcomingEventNotificationCheck();
                break;
            case "updateInstanceID":
                updateInstanceID(obj);
                break;
            case "updateUserProfile":
                updateUserProfile(obj);
                break;
            case "wipe":
                wipe();
                break;
            default:
                System.out.println("default " + obj.toString());
        }
    }

    private void addCommunity(JSONObject obj) {
        //calls createCommunityBoardTable, createCommunityEventTable
        //createCommunityUserTable, createHCommunityHangouts
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
                    createCommunityEventsTable(name);
                    //createCommunityEventsCheckInTable(name);
                    createCommunityHangoutsTable(name);
                    createCommunityUsersTable(name);
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
            sql = "SELECT count(*) FROM " + communityName + " WHERE googleID = ?";
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
                    if (Integer.parseInt((String) obj.get("isLeader")) == 1) {
                        sql = "UPDATE Users SET moderatorOf = " +
                                "CONCAT(?, moderatorOf) WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, obj.get("communityName") + ", ");
                        ps.setString(2, googleID);
                        System.out.println(ps);
                        ps.executeUpdate();
                    }
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
                    + "state, address, zip, locationName, numAttendees, listAttendees) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.setString(11, "");
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addHangout(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Hangouts";
        String hangoutName = (String) obj.get("hangoutName");
        try {
            sql = "SELECT count(*) FROM " + communityName + " WHERE hangoutName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, hangoutName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    sql = "INSERT INTO " + communityName + " (creator, hangoutName, startTime, "
                            + "endTime, date, address, locationName, listAttendees) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("creator"));
                    ps.setString(2, hangoutName);
                    ps.setString(3, (String) obj.get("startTime"));
                    ps.setString(4, (String) obj.get("endTime"));
                    ps.setString(5, (String) obj.get("date"));
                    ps.setString(6, (String) obj.get("address"));
                    ps.setString(7, (String) obj.get("locationName"));
                    ps.setString(8, (String) obj.get("googleID"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addHangoutUser(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Hangouts";
        String googleID = (String) obj.get("googleID");
        String hangoutName = (String) obj.get("hangoutName");
        try {
            sql = "SELECT * FROM " + communityName + " WHERE hangoutName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, hangoutName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                sql = "UPDATE " + communityName + " SET listAttendees = "
                        + "CONCAT(?, listAttendees) WHERE hangoutName = ?";
                ps = conn.prepareStatement(sql);
                googleID += ", ";
                ps.setString(1, googleID);
                ps.setString(2, hangoutName);
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Board";
        String message = (String) obj.get("message");
        String name = message.split(":")[0];
        try {
            sql = "INSERT INTO " + communityName
                    + " (pinned, name, date, message) VALUES(?, ?, ?, ?)";
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
        newBoardNotification((String) obj.get("communityName"), name, message);
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
                    sql = "INSERT INTO Users (firstName, lastName, googleID, communitiesList, "
                            + "moderatorOf, pictureURL) VALUES (?, ?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, (String) obj.get("firstName"));
                    ps.setString(2, (String) obj.get("lastName"));
                    ps.setString(3, googleID);
                    ps.setString(4, "");
                    ps.setString(5, "");
                    ps.setString(6, (String) obj.get("pictureURL"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void communityInvite(JSONObject obj) {
        //TODO test
        JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject parent = new JSONObject();
        notification.put("body", "You have received an invitation to join a community");
        notification.put("title", "New Community Invitation");
        data.put("body", obj.get("user") + " has invited you to join " + obj.get("communityName"));
        data.put("title", "New community invitation by " + obj.get("user"));
        data.put("communityName", obj.get("communityName"));
        parent.put("notification", notification);
        parent.put("data", data);
        parent.put("priority", "high");
        messageWrite(parent);
    }

    private String communitySearch(String type, String value) {
        StringBuilder communities = new StringBuilder();
        try {
            sql = "SELECT * FROM Communities WHERE " + type + " = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, value);
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                communities.append(rs.getString("name"));
                communities.append(", ");
            }
            out.writeUTF(communities.toString());
            System.out.println(communities);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void checkIn(JSONObject obj) {
        //TODO test
        String communityName = (String) obj.get("communityName");
        String eventName = (String) obj.get("eventName");
        String googleID = (String) obj.get("googleID");
        String listCheckedIn, id;
        try {
            sql = "SELECT * FROM eventsCheckIn WHERE (communityName = ? AND eventName = ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, communityName);
            ps.setString(2, eventName);
            if (rs.next()) {
                id = rs.getString("idEventsCheckIns");
                listCheckedIn = rs.getString("listCheckedIn");
                listCheckedIn += googleID + ", ";
                sql = "UPDATE eventsCheckIn SET communityName = ?, eventName = ?, "
                        + "numCheckedIn = numCheckedIn + 1, listCheckedIn = ? "
                        + "WHERE idEventCheckIns = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, communityName);
                ps.setString(2, eventName);
                ps.setString(3, listCheckedIn);
                ps.setString(4, id);
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityBoardTable(String communityName) {
        //called by addCommunity
        communityName += "_Board";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idMessage INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, "
                + "pinned INT(4), name VARCHAR(255), date DATE, "
                + "message TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityEventsTable(String communityName) {
        //called by addCommunity
        //DATETIME: YYYY-MM-DD HH:MM:SS, DATE: YYYY-MM-DD
        //SELECT TIME_FORMAT('21:46:25', '%r') = 09:46:25 PM
        communityName += "_Calendar";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idEvents INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, eventName VARCHAR(255), "
                + "description VARCHAR(255), date DATE, time TIME, city VARCHAR(255), "
                + "state VARCHAR(255), address VARCHAR(255), zip VARCHAR(255), "
                + "locationName VARCHAR(255), numAttendees INT(4), "
                + "listAttendees TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityHangoutsTable(String communityName) {
        //called by addCommunity
        communityName += "_Hangouts";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idHangouts INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, creator VARCHAR(255), "
                + "hangoutName VARCHAR(255), startTime TIME, endTime TIME, date DATE, "
                + "address VARCHAR(255), locationName VARCHAR(255), "
                + "listAttendees TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityUsersTable(String communityName) {
        //called by addCommunity
        communityName += "_Users";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idUsers INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, googleID VARCHAR(255), "
                + "isLeader TINYINT(1), subscribed INT(1))";
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
            sql = "DELETE FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, (String) obj.get("eventName"));
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
                sql = "UPDATE Users SET (eventName, description, date, time, city, state, address, "
                        + "zip, locationName, numAttendees) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "WHERE eventName = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(11, rs.getString("eventName"));
                if (obj.get("eventName") != rs.getString("eventName")
                        && obj.get("eventName") != null) {
                    ps.setString(1, (String) obj.get("eventName"));
                } else {
                    ps.setString(1, rs.getString("eventName"));
                }
                if (obj.get("description") != rs.getString("description")
                        && obj.get("description") != null) {
                    ps.setString(2, (String) obj.get("description"));
                } else {
                    ps.setString(2, rs.getString("description"));
                }
                if (obj.get("date") != null && obj.get("date") != rs.getString("date")) {
                    ps.setString(3, (String) obj.get("date"));
                } else {
                    ps.setString(3, rs.getString("date"));
                }
                if (obj.get("time") != null && obj.get("time") != rs.getString("time")) {
                    ps.setString(4, (String) obj.get("time"));
                } else {
                    ps.setString(4, rs.getString("time"));
                }
                if (obj.get("city") != null && obj.get("city") != rs.getString("city")) {
                    ps.setString(5, (String) obj.get("city"));
                } else {
                    ps.setString(5, rs.getString("city"));
                }
                if (obj.get("state") != null && obj.get("state") != rs.getString("state")) {
                    ps.setString(6, (String) obj.get("state"));
                } else {
                    ps.setString(6, rs.getString("state"));
                }
                if (obj.get("address") != null && obj.get("address") != rs.getString("address")) {
                    ps.setString(7, (String) obj.get("address"));
                } else {
                    ps.setString(7, rs.getString("address"));
                }
                if (obj.get("zip") != null && obj.get("zip") != rs.getString("zip")) {
                    ps.setString(8, (String) obj.get("zip"));
                } else {
                    ps.setString(8, rs.getString("zip"));
                }
                if (obj.get("locationName") != rs.getString("locationName")
                        && obj.get("locationName") != null) {
                    ps.setString(9, (String) obj.get("locationName"));
                } else {
                    ps.setString(9, rs.getString("locationName"));
                }
                if (obj.get("numAttendees") != rs.getString("numAttendees")
                        && obj.get("numAttendees") != null) {
                    ps.setString(10, (String) obj.get("numAttendees"));
                } else {
                    ps.setString(10, rs.getString("numAttendees"));
                }
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void emailInvite(String fromName, String to) {
        final String password = appKey;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication("noreply.butterfly@gmail.com", password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply.butterfly@gmail.com"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Butterfly Invite");
            message.setText("A wants you to use Butterfly");
            Transport.send(message);
            System.out.println("Invite Sent from: noreply.butterfly@gmail.com to: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void endedHangoutsCheck() {
        try {
            sql = "SELECT name FROM Communities";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                ArrayList<String> communities = new ArrayList<>();
                String temp = rs.getString("name").replaceAll("\\s", "_");
                temp += "_Hangouts";
                communities.add(temp);
                communities.forEach(this::timeCheckHangouts);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void genericNotification(String to, String message, String title) {
        JSONObject notification = new JSONObject();
        JSONObject parent = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("body", "aiuosdfhuiashdfuioasduiof");
        data.put("title", title);
        data.put("communityName", "Hahahaha ");
        parent.put("data", data);
        notification.put("body", message);
        notification.put("title", title);
        parent.put("to", to);
        parent.put("notification", notification);
        parent.put("priority", "high");
        messageWrite(parent);
    }

    private void getCommunities() {
        StringBuilder communities = new StringBuilder();
        try {
            sql = "SELECT * FROM Communities";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("name") + " " + rs.getInt("private"));
                if (rs.getInt("private") == 0) {
                    communities.append(rs.getString("name"));
                    communities.append(", ");
                }
            }
            out.writeUTF(communities.toString());
            System.out.println(communities);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getCommunityUsers(String communityName) {
        //TODO test
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    System.out.println("Users in community " + Integer.toString(rs.getInt(1)));
                    out.writeUTF(Integer.toString(rs.getInt(1)));
                    sql = "SELECT * FROM " + communityName;
                    ps = conn.prepareStatement(sql);
                    rs = ps.executeQuery();
                    JSONObject obj;
                    while (rs.next()) {
                        sql = "SELECT * FROM Users WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, rs.getString("googleID"));
                        ResultSet result = ps.executeQuery();
                        if (result.next()) {
                            obj = new JSONObject();
                            obj.put("firstName", result.getString("firstName"));
                            obj.put("lastName", result.getString("lastName"));
                            out.writeUTF(obj.toString());
                            System.out.println(obj.toString());
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getCommunityUsersWithID(String communityName) {
        //TODO test
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    System.out.println("Users in community " + Integer.toString(rs.getInt(1)));
                    out.writeUTF(Integer.toString(rs.getInt(1)));
                    sql = "SELECT * FROM " + communityName;
                    ps = conn.prepareStatement(sql);
                    rs = ps.executeQuery();
                    JSONObject obj;
                    while (rs.next()) {
                        sql = "SELECT * FROM Users WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, rs.getString("googleID"));
                        ResultSet result = ps.executeQuery();
                        if (result.next()) {
                            obj = new JSONObject();
                            obj.put("firstName", result.getString("firstName"));
                            obj.put("lastName", result.getString("lastName"));
                            obj.put("googleID", result.getString("googleID"));
                            out.writeUTF(obj.toString());
                            System.out.println(obj.toString());
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getEvents(String communityName) {
        //TODO check if user is in any communities before executing
        String oldName = communityName;
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    System.out.println("in if " + Integer.toString(rs.getInt(1)));
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
                } else {
                    System.out.println("else 0");
                    out.writeUTF("0");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private String getIdUsers(String googleID) {
        try {
            sql = "SELECT idUsers FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println(rs.getString("idUsers"));
                return rs.getString("idUsers");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String getInstanceID(String googleID) {
        //function passed to genericNotification
        try {
            sql = "SELECT * FROM Users WHERE googleID = ?";
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

    /*
        Function which writes to client the number of messages stored in the community
        board table and then all rows in the table in JSON format.
     */
    private void getMessages(String communityName) {
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Board";
        JSONObject obj;
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                out.writeUTF(Integer.toString(rs.getInt(1)));
                System.out.println("total messages " + rs.getInt(1));
                sql = "SELECT * FROM " + communityName;
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    obj = new JSONObject();
                    obj.put("pinned", rs.getString("pinned"));
                    obj.put("name", rs.getString("name"));
                    obj.put("date", rs.getString("date"));
                    obj.put("message", rs.getString("message"));
                    out.writeUTF(obj.toString());
                    System.out.println(obj);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which loops through every community in the neighborhood table
        and writes to client number of communities in the neighborhood and then
        calls a function to write all events from the list of communities.
        Calls getEvents.
     */
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

    /*
        Function which writes to client string list of users which have rsvp'ed to specified
        event from specified community. If there are no users who have rsvp'ed then send write
        back empty string.
     */
    private void getrsvp(String communityName, String eventName) {
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        try {
            sql = "SELECT listAttendees FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            System.out.println(ps);
            rs = ps.executeQuery();
            String list;
            if (rs.next()) {
                list = rs.getString("listAttendees");
                StringBuilder namesList = new StringBuilder();
                ArrayList<String> ids = new ArrayList<>(Arrays.asList(list.split(", ")));
                //out.writeUTF(Integer.toString(ids.size()));
                for (String id : ids) {
                    sql = "SELECT * FROM Users WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, id);
                    System.out.println(ps);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        namesList.append(rs.getString("firstName"));
                        namesList.append(" ");
                        namesList.append(rs.getString("lastName"));
                        namesList.append(", ");
                    }
                }
                out.writeUTF(namesList.toString());
                System.out.println("sent " + list);
            } else {
                out.writeUTF("");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which writes to client string of community names that the user is a part
        of from the communitiesList column in Users table. If user is not in any communities
        then write empty string.
    */
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
                System.out.println("sent " + list);
            } else {
                out.writeUTF("");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which writes to client the number of communities the user is in then
        the community name and calls a function to get all events of that community.
        If user is not in any communities then write empty string. Calls getEvents.
     */
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
                for (String community : communities) {
                    out.writeUTF(community);
                    getEvents(community);
                }
            } else {
                out.writeUTF("");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserName(String googleID) {
        try {
            sql = "SELECT * FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("firstName");
                name += " ";
                name += rs.getString("lastName");
                out.writeUTF(name);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which writes to client string of community names
        where the user is a moderator of. Writes an empty string if
        user is not a moderator of any communities.
    */
    private void getUserModerator(String googleID) {
        try {
            sql = "SELECT moderatorOf FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            String list;
            if (rs.next()) {
                list = rs.getString("moderatorOf");
                out.writeUTF(list);
                System.out.println("sent " + list);
            } else {
                out.writeUTF("");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserProfile(String googleID) {
        try {
            sql = "SELECT * FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                JSONObject returnJSON = new JSONObject();
                returnJSON.put("firstName", rs.getString("firstName"));
                returnJSON.put("lastName", rs.getString("lastName"));
                returnJSON.put("googleID", rs.getString("googleID"));
                returnJSON.put("communitiesList", rs.getString("communitiesList"));
                returnJSON.put("moderatorOf", rs.getString("moderatorOf"));
                returnJSON.put("pictureURL", rs.getString("pictureURL"));
                out.writeUTF(returnJSON.toString());
                System.out.println("sent " + returnJSON.toString());
            } else {
                out.writeUTF("");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void groupNotification(JSONObject obj) {
        String name = (String) obj.get("name");
        String community = (String) obj.get("communityName");
        String message = (String) obj.get("message");
        JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject parent = new JSONObject();
        notification.put("body", name + " pinged the group");
        notification.put("title", "New ping in " + community + " by " + name);
        //data.put("body", message);
        data.put("title", "New ping in " + community + " by " + name);
        data.put("communityName", community);
        community.replaceAll("\\s", "_");
        parent.put("to", "/topics/" + community);
        parent.put("notification", notification);
        parent.put("data", data);
        parent.put("priority", "high");
        messageWrite(parent);
    }

    /*
        Function which removes the user from the community users table,
        updates the communitiesList for the user in the Users table,
        if the user was a moderator for the community left, then also updates
        the moderatorOf for the user in the Users table.
     */
    private void leaveCommunityUser(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT COUNT(*) FROM " + communityName + " WHERE googleID = ?";
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
                        StringBuilder communitiesList = new StringBuilder();
                        for (String community : communities) {
                            if (community.contains((String) obj.get("communityName"))) {
                                System.out.println("found " + community);
                            } else {
                                communitiesList.append(community);
                                communitiesList.append(", ");
                            }
                        }
                        sql = "UPDATE Users SET communitiesList = ? WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, communitiesList.toString());
                        ps.setString(2, googleID);
                        System.out.println(ps);
                        ps.executeUpdate();
                    }
                    sql = "SELECT moderatorOf FROM Users WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, googleID);
                    System.out.println(ps);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        list = rs.getString("moderatorOf");
                        ArrayList<String> communities;
                        communities = new ArrayList<>(Arrays.asList(list.split(", ")));
                        StringBuilder communitiesList = new StringBuilder();
                        for (String community : communities) {
                            if (community.contains((String) obj.get("communityName"))) {
                                System.out.println("found " + community);
                            } else {
                                communitiesList.append(community);
                                if (!community.equals(" ")) {
                                    communitiesList.append(", ");
                                }
                            }
                        }
                        sql = "UPDATE Users SET moderatorOf = ? WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, communitiesList.toString());
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

    /*
        Function which removes the user from a hangout in the specified community
        at the specified hangout name.
     */
    private void leaveHangoutUser(JSONObject obj) {
        //TODO finish
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Hangouts";
        String googleID = (String) obj.get("googleID");
        String eventName = (String) obj.get("hangoutName");
        try {
            sql = "SELECT * FROM " + communityName + " WHERE hangoutName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            rs = ps.executeQuery();
            if (rs.next()) {
                String list = rs.getString("listAttendees");
                ArrayList<String> attendees;
                attendees = new ArrayList<>(Arrays.asList(list.split(", ")));
                StringBuilder string = new StringBuilder();
                for (String attendee : attendees) {
                    if (attendee.contains(googleID)) {
                        System.out.println("found " + attendee);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which sends a http POST request to FireBaseIO and receives a response
        JSON back from FireBaseIO. Called by genericNotification and newBoardNotification.
     */
    private void messageWrite(JSONObject parent) {
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

    /*
        Function which builds the JSON for notification when a new board message has been
        added. Uses FireBase topic messaging. Called by addMessage and calls messageWrite.
     */
    private void newBoardNotification(String community, String name, String message) {
        JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject parent = new JSONObject();
        notification.put("body", name + " has posted a new message");
        notification.put("title", "New Message in " + community + " by " + name);
        //data.put("body", message);
        data.put("title", "New Message in " + community + " by " + name);
        data.put("communityName", community);
        community.replaceAll("\\s", "_");
        parent.put("to", "/topics/" + community);
        parent.put("notification", notification);
        parent.put("data", data);
        parent.put("priority", "high");
        messageWrite(parent);
    }

    private void removeAllCommunities() {
        try {
            sql = "SELECT name FROM Communities";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                String communityName = rs.getString("name");
                sql = "DROP TABLE " + communityName + "_Board, " + communityName + "_Calendar, "
                        + communityName + "_Hangouts, " + communityName + "_Users";
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                ps.executeUpdate();
            }
            sql = "SELECT * FROM Users";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                sql = "UPDATE Users SET (communitiesList, moderatorOf) VALUES (?, ?) WHERE googleID = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, "");
                ps.setString(2, "");
                ps.setString(3, rs.getString("googleID"));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeCommunity(String communityName) {
        String name = communityName;
        try {
            sql = "DELETE FROM Communities WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, communityName);
            System.out.println(ps);
            ps.executeUpdate();
            communityName = communityName.replaceAll("\\s", "_");
            sql = "SELECT googleID FROM " + communityName + "_Users";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                sql = "SELECT communitiesList FROM Users WHERE googleID = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, rs.getString("googleID"));
                ResultSet result = ps.executeQuery();
                if (result.next()) {
                    ArrayList<String> communities = new ArrayList<>(
                            Arrays.asList(result.getString("communitiesList").split(", ")));
                    StringBuilder string = new StringBuilder();
                    for (String community : communities) {
                        if (community.contains(name)) {
                            System.out.println("found " + community);
                        } else {
                            string.append(community);
                            string.append(", ");
                        }
                    }
                    sql = "UPDATE Users SET communitiesList = ? WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, string.toString());
                    ps.setString(2, rs.getString("googleID"));
                    ps.executeUpdate();
                }
            }
            sql = "DROP TABLE " + communityName + "_Board, " + communityName + "_Calendar, "
                    + communityName + "_Hangouts, " + communityName + "_Users";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which adds the user to listAttendees in community calendar
        table for specified event.
     */
    private void rsvpEvent(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        String googleID = (String) obj.get("googleID");
        String eventName = (String) obj.get("eventName");
        try {
            sql = "SELECT * FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                sql = "UPDATE " + communityName + " SET listAttendees = "
                        + "CONCAT(?, listAttendees) WHERE eventName = ?";
                ps = conn.prepareStatement(sql);
                googleID += ", ";
                ps.setString(1, googleID);
                ps.setString(2, eventName);
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which selects from the community calendar table and checks
        whether the user has already rsvp'ed to the specified event. Returns
        string "1" if found and "0" otherwise.
     */
    private void rsvpCheck(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        String googleID = (String) obj.get("googleID");
        String eventName = (String) obj.get("eventName");
        try {
            sql = "SELECT * FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            rs = ps.executeQuery();
            if (rs.next()) {
                String list = rs.getString("listAttendees");
                ArrayList<String> attendees;
                attendees = new ArrayList<>(Arrays.asList(list.split(", ")));
                for (String attendee : attendees) {
                    if (attendee.contains(googleID)) {
                        System.out.println("found " + attendee);
                        out.writeUTF("1");
                        return;
                    }
                }
                System.out.println("0");
                out.writeUTF("0");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Updates listAttendees in the community calendar table
        for an event if a user removes rsvp from event.
     */
    private void rsvpEventRemove(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        String googleID = (String) obj.get("googleID");
        String eventName = (String) obj.get("eventName");
        try {
            sql = "SELECT * FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            rs = ps.executeQuery();
            if (rs.next()) {
                String list = rs.getString("listAttendees");
                ArrayList<String> attendees;
                attendees = new ArrayList<>(Arrays.asList(list.split(", ")));
                StringBuilder string = new StringBuilder();
                for (String attendee : attendees) {
                    if (attendee.contains(googleID)) {
                        System.out.println("found " + attendee);
                    } else {
                        string.append(rs.getString("name"));
                        string.append(", ");
                    }
                }
                sql = "UPDATE " + communityName + " SET listAttendees = ? WHERE eventName = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, string.toString());
                ps.setString(2, eventName);
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which loops through rows in the community calendar table
        and if the startDate for an event is within 24 hours then send out
        a notification to all subscribed members of the community and creates
        a new row into eventsCheckIn.
     */
    private void timeCheckEvents(String communityName) {
        // called by upcomingEventNotification
        // TIME: HH:MM:SS, DATE: YYYY-MM-DD
        String communityReplaced = communityName.replaceAll("\\s", "_");
        String communityCalendar = communityReplaced;
        communityCalendar += "_Calendar";
        try {
            sql = "SELECT * FROM " + communityCalendar;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String eventDate = rs.getString("date");
                //String eventTime = rs.getString("time");
                ArrayList<String> dateSplit;
                dateSplit = new ArrayList<>(Arrays.asList(eventDate.split("-")));
                //ArrayList<String> timeSplit;
                //timeSplit = new ArrayList<>(Arrays.asList(eventTime.split(":")));
                Date date = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int currYear = cal.get(Calendar.YEAR);
                int currMonth = cal.get(Calendar.MONTH);
                int currDay = cal.get(Calendar.DAY_OF_MONTH);
                //int hour = cal.get(Calendar.HOUR_OF_DAY);
                //int minute = cal.get(Calendar.MINUTE);
                int eventYear = Integer.parseInt(dateSplit.get(0));
                int eventMonth = Integer.parseInt(dateSplit.get(1));
                if (eventMonth > 0) {
                    eventMonth--;
                }
                int eventDay = Integer.parseInt(dateSplit.get(2));
                if (currYear == eventYear && currMonth == eventMonth && eventDay - currDay < 1) {
                    String topic = "/topics/";
                    topic += communityReplaced;
                    genericNotification(topic, "A Community event is upcoming", "Upcoming Event");
                    sql = "INSERT INTO eventsCheckIn (communityName, eventName) VALUES(?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, "communityName");
                    ps.setString(2, rs.getString("eventName"));
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which loops through rows in the community hangouts table
        and removes the hangouts which have passed.
     */
    private void timeCheckHangouts(String communityHangout) {
        //TODO test
        try {
            sql = "SELECT * FROM " + communityHangout;
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String eventDate = rs.getString("date");
                String eventTime = rs.getString("endTime");
                ArrayList<String> dateSplit;
                dateSplit = new ArrayList<>(Arrays.asList(eventDate.split("-")));
                ArrayList<String> timeSplit;
                timeSplit = new ArrayList<>(Arrays.asList(eventTime.split(":")));
                Date date = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int currYear = cal.get(Calendar.YEAR);
                int currMonth = cal.get(Calendar.MONTH);
                int currDay = cal.get(Calendar.DAY_OF_MONTH);
                int currHour = cal.get(Calendar.HOUR_OF_DAY);
                int currMinute = cal.get(Calendar.MINUTE);
                int hangoutYear = Integer.parseInt(dateSplit.get(0));
                int hangoutMonth = Integer.parseInt(dateSplit.get(1));
                if (hangoutMonth > 0) {
                    hangoutMonth--;
                }
                int hangoutDay = Integer.parseInt(dateSplit.get(2));
                int hangoutHour = Integer.parseInt(timeSplit.get(0));
                int hangoutMinute = Integer.parseInt(timeSplit.get(1));
                if (currYear == hangoutYear && currMonth == hangoutMonth
                        && hangoutDay - currDay == 1) {
                    if (currHour - hangoutHour >= 0 && currMinute - hangoutMinute >= 0) {
                        sql = "DELETE FROM " + communityHangout + " WHERE idHangouts = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, rs.getString("idHangouts"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which loops through rows in Communities table then sends community name
        to secondary function to check whether an event is upcoming.
     */
    private void upcomingEventNotificationCheck() {
        //TODO will have to be always running to be prompt
        try {
            sql = "SELECT name FROM Communities";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                ArrayList<String> communities = new ArrayList<>();
                communities.add(rs.getString("name"));
                System.out.println(rs.getString("name"));
                communities.forEach(this::timeCheckEvents);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Updates the Users table with the current instanceID provided
        by FireBaseIO because instanceIDs can change depending on something.
     */
    private void updateInstanceID(JSONObject obj) {
        try {
            sql = "UPDATE Users SET instanceID = ? WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, (String) obj.get("instanceID"));
            ps.setString(2, (String) obj.get("googleID"));
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Updates the Users table with any different information from
        JSON which is sent from client.
     */
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
                sql = "UPDATE Users SET (firstName, lastName, birthDate) "
                        + "VALUES (?, ?, ?) WHERE googleID = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(4, googleID);
                if (obj.get("firstName") != null
                        && obj.get("firstName") != rs.getString("firstName")) {
                    ps.setString(1, (String) obj.get("firstName"));
                } else {
                    ps.setString(1, rs.getString("firstName"));
                }
                if (obj.get("lastName") != null
                        && obj.get("lastName") != rs.getString("lastName")) {
                    ps.setString(2, (String) obj.get("lastName"));
                } else {
                    ps.setString(2, rs.getString("lastName"));
                }
                if (obj.get("birthDate") != null
                        && obj.get("birthDate") != rs.getString("birthDate")) {
                    ps.setString(3, (String) obj.get("birthDate"));
                } else {
                    ps.setString(3, rs.getString("birthDate"));
                }
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void wipe() {
        try {
            sql = "SELECT name FROM Communities";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                String communityName = rs.getString("name");
                communityName = communityName.replaceAll("\\s", "_");
                sql = "DROP TABLE " + communityName + "_Board, " + communityName + "_Calendar, "
                        + communityName + "_Hangouts, " + communityName + "_Users";
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                ps.executeUpdate();
            }
            /*sql = "TRUNCATE TABLE Users";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();*/
            /*sql = "TRUNCATE TABLE Communities";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*private boolean checkIfLeader(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
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
        }
        return false;
    }*/

    /*private ArrayList<String> getSubscribedMembers(String communityName) {
        ArrayList<String> subscribedGoogleIDs = new ArrayList<>();
        try {
            sql = "SELECT * from " + communityName + "_Users WHERE subscribed = '1'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                subscribedGoogleIDs.add(rs.getString("googleID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subscribedGoogleIDs;
    }*/
}

@SuppressWarnings("InfiniteLoopStatement")
public class Server {
    public static void main(String[] args) {
        Date currentDate = Calendar.getInstance().getTime();
        System.out.println(currentDate);
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(new FileInputStream("/home/khanh/Butterfly-40ec2c75b546.json"))
                .setDatabaseUrl("butterfly-145620.firebaseio.com/")
                .build();
            FirebaseApp.initializeApp(options);
            ServerSocket serverSocket = new ServerSocket(3300);
            while (true) {
                System.out.println("Waiting on port " + serverSocket.getLocalPort() + "...");
                try {
                    Socket server = serverSocket.accept();
                    System.out.println("Just connected to " + server.getRemoteSocketAddress());
                    JSONParser parser = new JSONParser();
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    sleep(100);
                    if (in.available() > 0) {
                        Object parsed = parser.parse(in.readUTF());
                        JSONObject obj = (JSONObject) parsed;
                        System.out.println("TIMESTAMP " + currentDate + " " + obj.get("function"));
                        new serverStart(obj, out).run();
                    }
                } catch (SocketTimeoutException s) {
                    System.out.println("Socket timed out!");
                } catch (ParseException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
