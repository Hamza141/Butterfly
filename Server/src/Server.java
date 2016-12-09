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
import java.sql.Statement;
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
import static java.lang.Math.toIntExact;
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

    serverStart(JSONObject pobj, DataOutputStream pout, String key, String pass, String auth) {
        obj = pobj;
        out = pout;
        appKey = key;
        PASS = pass;
        Authorization = auth;
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
            case "addCrew":
                addCrew(obj);
                break;
            case "addEvent":
                addEvent(obj);
                break;
            case "addHangout":
                addHangout(obj);
                break;
            case "addHangoutUser":
                addHangoutUser(obj);
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
            case "editHangout":
                editHangout(obj);
                break;
            case "emailInvite":
                emailInvite("user", (String) obj.get("to"));
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
            case "getCrewUsers":
                getCrewUsers((String) obj.get("communityName"), (long) obj.get("idCrew"));
                break;
            case "getCrewMessages":
                getCrewMessages(obj);
                break;
            case "getEvents":
                getEvents((String) obj.get("communityName"));
                break;
            case "getHangouts":
                getHangouts((String) obj.get("communityName"));
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
            case "getUserCrews":
                getUserCrews((String) obj.get("communityName"), (String) obj.get("googleID"));
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
            case "inviteNotification":
                inviteNotification(obj);
                break;
            case "isPrivate":
                isPrivate((String) obj.get("communityName"));
                break;
            case "leaveCommunityUser":
            case "removeCommunityUser":
                leaveCommunityUser(obj);
                break;
            case "leaveHangoutUser":
                leaveHangoutUser(obj);
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

    /*
        Function which adds a new user created community into the database, inserts the new
        community into the Communities table and creates four new tables.
        Function calls createCommunityBoardTable, createCommunityEventTable,
        createCommunityUserTable, and createHCommunityHangouts.
     */
    private void addCommunity(JSONObject obj) {
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
                    createCommunityUsersTable(name);
                    createCommunityBoardTable(name);
                    createCommunityCrewsTable(name);
                    createCommunityEventsTable(name);
                    createCommunityHangoutsTable(name);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which adds a new user into the specified community users table if
        they are not already there. Updates the communitiesList of the user in Users
        table and if the new user is a moderator, update the moderatorOf list in the
        Users table.

     */
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addCrew(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        String list = (String) obj.get("list");
        String crewName = (String) obj.get("crewName");
        try {
            ArrayList<String> members;
            members = new ArrayList<>(Arrays.asList(list.split(", ")));
            String crewsTable = communityName + "_Crews";
            sql = "INSERT INTO " + crewsTable + " (crewName, numMembers, listMembers) VALUES (?, ?, ?)";
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, crewName);
            ps.setInt(2, members.size());
            ps.setString(3, list);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int idCrew = rs.getInt(1);
                String crewTable = communityName + "_" + crewName.replaceAll("\\s", "_") + "_" + idCrew + "_Board";
                String newTable = "CREATE TABLE " + crewTable + " ("
                        + "idMessage INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, "
                        + "pinned INT(4), name VARCHAR(255), date DATE, "
                        + "message TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
                System.out.println(newTable);
                try {
                    ps = conn.prepareStatement(newTable);
                    ps.executeUpdate();
                    out.write(idCrew);
                    System.out.println("idCrew " + idCrew);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
        Function which adds a new event into the specified community calendar table.
     */
    private void addEvent(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        String eventName = (String) obj.get("eventName");
        try {
            sql = "SELECT count(*) FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    sql = "INSERT INTO " + communityName + " (eventName, description, date, time, city, "
                            + "state, address, zip, locationName, numAttendees, notified, listAttendees) "
                            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)";
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which creates a new hangout in the specified community hangout table.
     */
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which adds a user to specified hangout. First checks the string
        listAttendees of hangout name for user googleID. If not found then concatenates
        the user googleID into the communitiesList for the hangout.
     */
    private void addHangoutUser(JSONObject obj) {
        //TODO test
        String name = (String) obj.get("communityName");
        name = name.replaceAll("\\s", "_");
        String communityName = name + "_Hangouts";
        String googleID = (String) obj.get("googleID");
        String hangoutName = (String) obj.get("hangoutName");
        int idHangout;
        try {
            sql = "SELECT listAttendees, idHangouts FROM " + communityName + " WHERE hangoutName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, hangoutName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                idHangout = rs.getInt("idHangouts");
                boolean found = false;
                String list = rs.getString("listAttendees");
                ArrayList<String> users;
                users = new ArrayList<>(Arrays.asList(list.split(", ")));
                for (String user : users) {
                    if (user.equals(googleID)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    sql = "UPDATE " + communityName + " SET (listAttendees, numUsers) VALUES (CONCAT(?, listAttendees), numUsers + 1) WHERE idHangouts = ?";
                    ps = conn.prepareStatement(sql);
                    googleID += ", ";
                    ps.setString(1, googleID);
                    ps.setInt(2, idHangout);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                sql = "SELECT maxUsers, numUsers, listAttendees FROM " + communityName + " WHERE idHangouts = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, idHangout);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("numUsers") == rs.getInt("maxUsers")) {
                        sql = "DELETE FROM " + communityName + " WHERE idHangouts = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setInt(1, idHangout);
                        ps.executeUpdate();
                        String crewsTable = name + "_Crews";
                        sql = "INSERT INTO " + crewsTable + " (crewName, numMembers, listMembers) VALUES (?, ?, ?)";
                        ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, hangoutName);
                        ps.setInt(2, rs.getInt("numUsers"));
                        ps.setString(3, rs.getString("listAttendees"));
                        System.out.println(ps);
                        ps.executeUpdate();
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            int idCrew = rs.getInt(1);
                            String crewName = name + hangoutName.replaceAll("\\s", "_") + "_" + idCrew + "_Board";
                            String newTable = "CREATE TABLE " + crewName + " ("
                                    + "idMessage INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, "
                                    + "pinned INT(4), name VARCHAR(255), date DATE, "
                                    + "message TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
                            System.out.println(newTable);
                            try {
                                ps = conn.prepareStatement(newTable);
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    ps.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                            out.writeUTF(obj.get("communityName") + ", " + hangoutName + ", " + idCrew);
                        }
                    } else {
                        out.writeUTF("not closed");
                    }
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("failed");
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which adds a new message into the specified community board table.
        After adding the message into the table calls function to send a notification.
        Calls newBoardNotification.
     */
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
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        newBoardNotification((String) obj.get("communityName"), name, message);
    }

    /*
        Function which adds a new user to the Users table if user is not already there.
     */
    private void addUser(JSONObject obj) {
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT count(*) from Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which creates the Firebase JSON with a message that a user is inviting
        another to specified community.
     */
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

    /*
        Function which searches the Communities for the specified value in provided variable
        type. If any communities are found, then write to client a comma separated string
        of all found communities, otherwise returns an empty string.
     */
    private String communitySearch(String type, String value) {
        StringBuilder communities = new StringBuilder();
        communities.append("");
        try {
            sql = "SELECT name FROM Communities WHERE " + type + " = ?";
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /*
        Function which concatenates a user into the list of users who have checked into
        the event.
     */
    private void checkIn(JSONObject obj) {
        //TODO test
        String communityName = (String) obj.get("communityName");
        String eventName = (String) obj.get("eventName");
        String googleID = (String) obj.get("googleID");
        String listCheckedIn;
        try {
            sql = "SELECT idEventsCheckIns, listCheckedIn FROM eventsCheckIn "
                    + "WHERE (communityName = ? AND eventName = ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, communityName);
            ps.setString(2, eventName);
            if (rs.next()) {
                listCheckedIn = rs.getString("listCheckedIn");
                boolean found = false;
                ArrayList<String> users;
                users = new ArrayList<>(Arrays.asList(listCheckedIn.split(", ")));
                for (String user : users) {
                    if (user.equals(googleID)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    int id = rs.getInt("idEventsCheckIns");
                    listCheckedIn += googleID + ", ";
                    sql = "UPDATE eventsCheckIn SET numCheckedIn = numCheckedIn + 1, listCheckedIn = ? "
                            + "WHERE idEventCheckIns = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, listCheckedIn);
                    ps.setInt(2, id);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which creates a new board table for newly added community.
        Called by addCommunity.
     */
    private void createCommunityBoardTable(String communityName) {
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
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCommunityCrewsTable(String communityName) {
        communityName += "_Crews";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idCrew INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, crewName VARCHAR(45), "
                + "numMembers INT(11), listMembers TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which creates a new calendar table for newly added community.
        Called by addCommunity.
     */
    private void createCommunityEventsTable(String communityName) {
        //DATETIME: YYYY-MM-DD HH:MM:SS, DATE: YYYY-MM-DD
        communityName += "_Calendar";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idEvents INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, eventName VARCHAR(255), "
                + "description VARCHAR(255), date DATE, time TIME, city VARCHAR(255), "
                + "state VARCHAR(255), address VARCHAR(255), zip VARCHAR(255), idCheckIn INT(4), "
                + "locationName VARCHAR(255), numAttendees INT(4), notified INT(1), "
                + "listAttendees TEXT CHARACTER SET latin1 COLLATE latin1_general_cs, "
                + "FOREIGN KEY (idCheckIn) REFERENCES eventsCheckIn (ideventCheckIns))";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which creates a new hangout table for newly added community.
        Called by addCommunity.
     */
    private void createCommunityHangoutsTable(String communityName) {
        communityName += "_Hangouts";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idHangouts INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, creator VARCHAR(255), "
                + "hangoutName VARCHAR(255), startTime TIME, endTime TIME, date DATE, "
                + "minUsers INT(11), maxUsers INT(11), numUsers INT(11), address VARCHAR(255), locationName VARCHAR(255), "
                + "listAttendees TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
        System.out.println(newTable);
        try {
            ps = conn.prepareStatement(newTable);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which creates a new users table for newly added community.
        Called by addCommunity.
     */
    private void createCommunityUsersTable(String communityName) {
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
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which deletes an event from the community calendar table.
     */
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
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which edits the values of an event in the calendar table for the specified
        community.
     */
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
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which edits the values of a hangout in the hangout table for the specified
        community.
     */
    private void editHangout(JSONObject obj) {
        //TODO test
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Hangout";
        String hangoutName = (String) obj.get("hangoutName");
        try {
            sql = "SELECT * FROM " + communityName + " WHERE hangoutName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, hangoutName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                sql = "UPDATE Users SET (hangoutName, startTime, endTime, date, address, locationName, "
                       + "listAttendees) VALUES (?, ?, ?, ?, ?, ?, ?) WHERE hangoutName = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(8, rs.getString("hangoutName"));
                if (obj.get("hangoutName") != rs.getString("hangoutName")
                        && obj.get("hangoutName") != null) {
                    ps.setString(1, (String) obj.get("hangoutName"));
                } else {
                    ps.setString(1, rs.getString("hangoutName"));
                }
                if (obj.get("startTime") != rs.getString("startTime")
                        && obj.get("startTime") != null) {
                    ps.setString(2, (String) obj.get("startTime"));
                } else {
                    ps.setString(2, rs.getString("startTime"));
                }
                if (obj.get("endTime") != null && obj.get("endTime") != rs.getString("endTime")) {
                    ps.setString(3, (String) obj.get("endTime"));
                } else {
                    ps.setString(3, rs.getString("endTime"));
                }
                if (obj.get("date") != null && obj.get("date") != rs.getString("date")) {
                    ps.setString(4, (String) obj.get("date"));
                } else {
                    ps.setString(4, rs.getString("date"));
                }
                if (obj.get("address") != null && obj.get("address") != rs.getString("address")) {
                    ps.setString(5, (String) obj.get("address"));
                } else {
                    ps.setString(5, rs.getString("address"));
                }
                if (obj.get("locationName") != rs.getString("locationName")
                        && obj.get("locationName") != null ) {
                    ps.setString(6, (String) obj.get("locationName"));
                } else {
                    ps.setString(6, rs.getString("locationName"));
                }
                if (obj.get("listAttendees") != rs.getString("listAttendees")
                        && obj.get("listAttendees") != null) {
                    ps.setString(7, (String) obj.get("listAttendees"));
                } else {
                    ps.setString(7, rs.getString("listAttendees"));
                }
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which sends an email to provided email address from the noreply.butterfly@gmail.com
        gmail account with a link to the app download page.
        Some email accounts cannot be sent to for whatever reason.
     */
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

    /*
        Function which writes to client a string of communities in the Communities table
        which are not marked as private.
     */
    private void getCommunities() {
        StringBuilder communities = new StringBuilder();
        try {
            sql = "SELECT name, private FROM Communities";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("private") == 0) {
                    communities.append(rs.getString("name"));
                    communities.append(", ");
                }
            }
            out.writeUTF(communities.toString());
            System.out.println(communities);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which writes to client JSON strings of users in specified community.
        First function writes number of following strings to send.
        JSON strings include first and last names of user.
     */
    private void getCommunityUsers(String communityName) {
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    out.writeUTF(Integer.toString(rs.getInt(1)));
                    sql = "SELECT googleID FROM " + communityName;
                    ps = conn.prepareStatement(sql);
                    System.out.println(ps);
                    rs = ps.executeQuery();
                    JSONObject obj;
                    while (rs.next()) {
                        sql = "SELECT firstName, lastName FROM Users WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, rs.getString("googleID"));
                        System.out.println(ps);
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which writes to client JSON strings of users in specified community.
        First function writes number of following strings to send.
        JSON strings include first and last names of user and googleID.
     */
    private void getCommunityUsersWithID(String communityName) {
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
                    sql = "SELECT googleID FROM " + communityName;
                    ps = conn.prepareStatement(sql);
                    rs = ps.executeQuery();
                    JSONObject obj;
                    while (rs.next()) {
                        sql = "SELECT firstName, lastName, googleID FROM Users WHERE googleID = ?";
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCrewUsers(String communityName, long idCrew) {
        //TODO test
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Crews";
        try {
            sql = "SELECT numMembers, listMembers FROM " + communityName + " WHERE idCrew = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, toIntExact(idCrew));
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                out.writeUTF(Integer.toString(rs.getInt("numMembers")));
                System.out.println("numMembers " + rs.getInt("numMembers"));
                ArrayList<String> ids = new ArrayList<>(Arrays.asList(rs.getString("listMembers").split(", ")));
                JSONObject obj;
                for (String id : ids) {
                    sql = "SELECT firstName, lastName, googleID FROM Users WHERE googleID = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, id);
                    System.out.println(ps);
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
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    /*
        Function which writes to client a string number of how many events in the
        community table then JSON strings of all rows in the provided community's
        calendar table.
     */
    private void getEvents(String communityName) {
        String community = communityName;
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
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
                        obj.put("communityName", community);
                        obj.put("locationName", rs.getString("locationName"));
                        obj.put("numAttendees", rs.getString("numAttendees"));
                        out.writeUTF(obj.toString());
                        System.out.println(obj.toString());
                    }
                } else {
                    out.writeUTF("0");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which writes to client a string containing the primary key idUsers
        associated with the googleID.
     */
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "0";
    }

    /*
        Function which writes to client a string containing the Firebase provided instanceID
        associated with the googleID.
     */
    private String getInstanceID(String googleID) {
        //function passed to genericNotification
        try {
            sql = "SELECT instanceID FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("instanceID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /*
        Function which writes to client JSON strings of hangouts in the specified community.
        First writes to client number of strings to be written.
     */
    private void getHangouts(String communityName) {
        //TODO test
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Hangouts";
        JSONObject obj;
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                out.writeUTF(Integer.toString(rs.getInt(1)));
                sql = "SELECT * FROM " + communityName;
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    obj = new JSONObject();
                    obj.put("creator", rs.getString("creator"));
                    obj.put("hangoutName", rs.getString("hangoutName"));
                    obj.put("startTime", rs.getString("startTime"));
                    obj.put("endTime", rs.getString("endTime"));
                    obj.put("date", rs.getString("date"));
                    obj.put("address", rs.getString("address"));
                    obj.put("locationName", rs.getString("locationName"));
                    obj.put("listIds", rs.getString("listAttendees"));
                    obj.put("minUsers", rs.getInt("minUsers"));
                    obj.put("maxUsers", rs.getInt("maxUsers"));
                    String list = rs.getString("listAttendees");
                    ArrayList<String> ids = new ArrayList<>(Arrays.asList(list.split(", ")));
                    StringBuilder names = new StringBuilder();
                    for (String id : ids) {
                        sql = "SELECT firstName, lastName FROM Users WHERE googleID = ?";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, id);
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            names.append(rs.getString("firstName"));
                            names.append(" ");
                            names.append(rs.getString("lastName"));
                            names.append(", ");
                        }
                    }
                    obj.put("listNames", names.toString());
                    out.writeUTF(obj.toString());
                    System.out.println(obj);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCrewMessages(JSONObject obj) {
        //TODO test
        String communityName = (String) obj.get("communityName");
        String crewName = (String) obj.get("crewName");
        int idCrew = toIntExact((long) obj.get("idCrew"));
        communityName = communityName.replaceAll("\\s", "_") + "_" + crewName.replaceAll("\\s", "_");
        communityName += "_" + idCrew + "_Board";
        JSONObject returnObj;
        try {
            sql = "SELECT COUNT(*) FROM " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                out.writeUTF(Integer.toString(rs.getInt(1)));
                sql = "SELECT * FROM " + communityName;
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                rs = ps.executeQuery();
                while (rs.next()) {
                    returnObj = new JSONObject();
                    returnObj.put("pinned", rs.getString("pinned"));
                    returnObj.put("name", rs.getString("name"));
                    returnObj.put("date", rs.getString("date"));
                    returnObj.put("message", rs.getString("message"));
                    out.writeUTF(returnObj.toString());
                    System.out.println(returnObj);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
                System.out.println(ps);
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                ArrayList<String> communities = new ArrayList<>();
                communities.add(rs.getString("name"));
                out.writeUTF(Integer.toString(communities.size()));
                System.out.println(communities.size());
                communities.forEach(this::getEvents);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
                    sql = "SELECT firstName, lastName FROM Users WHERE googleID = ?";
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
                System.out.println("sent " + namesList);
            } else {
                out.writeUTF("");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getUserCrews(String communityName, String googleID) {
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Crews";
        try {
            sql = "SELECT idCrew, crewName, listMembers FROM " + communityName;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            JSONObject returnObj;
            while (rs.next()) {
                String list = rs.getString("listMembers");
                ArrayList<String> users = new ArrayList<>(Arrays.asList(list.split(", ")));
                for (String user : users) {
                    if (user.equals(googleID)) {
                        returnObj = new JSONObject();
                        returnObj.put("crewName", rs.getString("crewName"));
                        returnObj.put("idCrew", rs.getInt("idCrew"));
                        out.writeUTF(returnObj.toString());
                        System.out.println(returnObj.toString());
                        break;
                    }
                }
            }
            returnObj = new JSONObject();
            returnObj.put("crewName", "END");
            returnObj.put("idCrew", -1);
            out.writeUTF(returnObj.toString());
            System.out.println(returnObj.toString());
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which writes to client a string that contains the first and last
        name of the user associated with the provided googleID.
     */
    private void getUserName(String googleID) {
        try {
            sql = "SELECT firstName, lastName FROM Users WHERE googleID = ?";
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which writes to client a JSON which holds all variable fields
        for a user in the Users table.
     */
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which builds the Firebase JSON for a notification that a user
        has pinged members of the community.
        Calls messageWrite.
     */
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
        parent.put("to", "/topics/" + community.replaceAll("\\s", "_"));
        parent.put("notification", notification);
        parent.put("data", data);
        parent.put("priority", "high");
        messageWrite(parent);
    }

    /*
        Function which builds the Firebase JSON for inviting a user who
        uses the app to the community.
        Calls messageWrite.
     */
    private void inviteNotification(JSONObject obj) {
        String name = (String) obj.get("name");
        String community = (String) obj.get("communityName");
        String message = (String) obj.get("message");
        String googleID = (String) obj.get("googleID");
        System.out.println(googleID);
        JSONObject data = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject parent = new JSONObject();
        notification.put("body", name + " wants you to join " + community);
        notification.put("title", "Community Invite");
        data.put("body", message);
        data.put("title", "Community Invite");
        data.put("communityName", community);
        //community.replaceAll("\\s", "_");
        parent.put("to", getInstanceID(googleID));
        parent.put("notification", notification);
        parent.put("data", data);
        parent.put("priority", "high");
        messageWrite(parent);
    }

    private void isPrivate(String communityName) {
        try {
            sql = "SELECT private FROM Communities WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, communityName);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("private") == 0) {
                    out.writeUTF("FALSE");
                } else {
                    out.writeUTF("TRUE");
                }
            }
        } catch (SQLException |IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
                            if (community.equals(obj.get("communityName"))) {
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
                            if (community.equals(obj.get("communityName"))) {
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which removes the user from a hangout in the specified community
        at the specified hangout name.
     */
    private void leaveHangoutUser(JSONObject obj) {
        //TODO test
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Hangouts";
        String googleID = (String) obj.get("googleID");
        String hangoutName = (String) obj.get("hangoutName");
        try {
            sql = "SELECT listAttendees FROM " + communityName + " WHERE hangoutName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, hangoutName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                String list = rs.getString("listAttendees");
                ArrayList<String> attendees;
                attendees = new ArrayList<>(Arrays.asList(list.split(", ")));
                StringBuilder string = new StringBuilder();
                for (String attendee : attendees) {
                    if (attendee.equals(googleID)) {
                        System.out.println("found " + attendee);
                    } else {
                        string.append(attendee);
                        string.append(", ");
                    }
                }
                sql = "UPDATE " + communityName + " SET listAttendees = ? WHERE hangoutName = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, string.toString());
                ps.setString(2, hangoutName);
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        data.put("body", message);
        data.put("title", "New Message in " + community + " by " + name);
        data.put("communityName", community);
        parent.put("to", "/topics/" + community.replaceAll("\\s", "_"));
        parent.put("notification", notification);
        parent.put("data", data);
        parent.put("priority", "high");
        messageWrite(parent);
    }

    /*
        Function which removes all communities from the database.
        Loops through the Communities table and drops all created tables
        associated with community name.
        Loops through Users table and truncates the rows for communitiesList and
        moderatorOf.
     */
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
            sql = "SELECT googleID FROM Users";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                sql = "UPDATE Users SET (communitiesList, moderatorOf) VALUES (?, ?) "
                        + "WHERE googleID = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, "");
                ps.setString(2, "");
                ps.setString(3, rs.getString("googleID"));
                System.out.println(ps);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which removes a community from the database.
        Removes the community from the Communities table.
        Loops through any users still in the community and removes the community
        from respective communitiesList.
        Drops all created tables associated from database.
     */
    private void removeCommunity(String communityName) {
        String name = communityName;
        ResultSet result;
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
                System.out.println(ps);
                result = ps.executeQuery();
                if (result.next()) {
                    ArrayList<String> communities = new ArrayList<>(
                            Arrays.asList(result.getString("communitiesList").split(", ")));
                    StringBuilder string = new StringBuilder();
                    for (String community : communities) {
                        if (community.equals(name)) {
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
                    System.out.println(ps);
                    ps.executeUpdate();
                    try {
                        result.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            sql = "DROP TABLE " + communityName + "_Board, " + communityName + "_Calendar, "
                    + communityName + "_Hangouts, " + communityName + "_Users";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            sql = "SELECT count(*) FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 1) {
                    sql = "UPDATE " + communityName + " SET listAttendees = "
                            + "CONCAT(?, listAttendees) WHERE eventName = ?";
                    ps = conn.prepareStatement(sql);
                    googleID += ", ";
                    ps.setString(1, googleID);
                    ps.setString(2, eventName);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            sql = "SELECT listAttendees FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                String list = rs.getString("listAttendees");
                ArrayList<String> attendees;
                attendees = new ArrayList<>(Arrays.asList(list.split(", ")));
                for (String attendee : attendees) {
                    if (attendee.equals(googleID)) {
                        out.writeUTF("1");
                        System.out.println("found " + attendee);
                        return;
                    }
                }
                out.writeUTF("0");
                System.out.println("0");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
            sql = "SELECT listAttendees FROM " + communityName + " WHERE eventName = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eventName);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                String list = rs.getString("listAttendees");
                ArrayList<String> attendees;
                attendees = new ArrayList<>(Arrays.asList(list.split(", ")));
                StringBuilder string = new StringBuilder();
                for (String attendee : attendees) {
                    if (attendee.equals(googleID)) {
                        System.out.println("found " + attendee);
                    } else {
                        string.append(attendee);
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
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Updates the Users table with any different information from
        JSON which is sent from client.
     */
    private void updateUserProfile(JSONObject obj) {
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
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        Function which drops all created tables and truncates the required tables Users, Communities,
        and eventsCheckIn.
     */
    private void wipe() {
        try {
            //sql = "SELECT "
            sql = "SELECT name FROM Communities";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                String communityName = rs.getString("name");
                communityName = communityName.replaceAll("\\s", "_");
                sql = "SELECT idCrew, crewName FROM " + communityName + "_Crews";
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                ResultSet result = ps.executeQuery();
                String all = "";
                while (result.next()) {
                    String crewName = communityName + "_"
                            + result.getString("crewName").replaceAll("\\s", "_") + "_"
                            + result.getInt("idCrew") + "_Board";
                    sql = "DROP TABLE " + crewName;
                    ps = conn.prepareStatement(sql);
                    System.out.println(ps);
                    ps.executeUpdate();
                }
                sql = "DROP TABLE " + communityName + "_Board, " + communityName + "_Calendar, "
                        + communityName + "_Hangouts, " + communityName + "_Users, "
                        + communityName + "_Crews";
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                ps.executeUpdate();
            }
            sql = "TRUNCATE TABLE Users, Communities, eventsCheckIn";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
            /*sql = "TRUNCATE TABLE Communities";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
            sql = "TRUNCATE TABLE eventsCheckIn";
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();*/
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

@SuppressWarnings({"unchecked", "InfiniteLoopStatement"})
class eventCheck implements Runnable {
    static private String Authorization;
    private Connection conn;
    private String sql;
    private PreparedStatement ps;
    private ResultSet rs;

    eventCheck(String PASS, String auth) {
        Authorization = auth;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/Butterfly", "root", PASS);
            conn.getMetaData().getCatalogs();
            conn.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        // called by upcomingEventNotification
        // TIME: HH:MM:SS, DATE: YYYY-MM-DD
        while (true) {
            try {
                sql = "SELECT name FROM Communities";
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                rs = ps.executeQuery();
                if (rs.next()) {
                    ArrayList<String> communities = new ArrayList<>();
                    communities.add(rs.getString("name"));
                    communities.forEach(this::timeCheckEvents);
                    communities.clear();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    rs.close();
                    ps.close();
                    Thread.sleep(60000);
                } catch (SQLException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
            sql = "SELECT idEvents, eventName, notified, date FROM " + communityCalendar;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("notified") == 0) {
                    sql = "UPDATE " + communityCalendar + " SET notified = 1 WHERE idEvents = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, rs.getInt("idEvents"));
                    System.out.println(ps);
                    ps.executeUpdate();
                    String eventDate = rs.getString("date");
                    ArrayList<String> dateSplit = new ArrayList<>(Arrays.asList(eventDate.split("-")));
                    Date date = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int currYear = cal.get(Calendar.YEAR);
                    int currMonth = cal.get(Calendar.MONTH);
                    int currDay = cal.get(Calendar.DAY_OF_MONTH);
                    int eventYear = Integer.parseInt(dateSplit.get(0));
                    int eventMonth = Integer.parseInt(dateSplit.get(1));
                    if (eventMonth > 0) {
                        eventMonth--;
                    }
                    int eventDay = Integer.parseInt(dateSplit.get(2));
                    if (currYear == eventYear && currMonth == eventMonth && eventDay - currDay < 1) {
                        String topic = "/topics/";
                        topic += communityReplaced;
                        eventNotification(topic, "A Community event is upcoming", "Upcoming Event",
                                communityName);
                        sql = "INSERT INTO eventsCheckIn (communityName, eventName, numCheckedIn, "
                                + "listCheckedIn) VALUES(?, ?, ?, ?)";
                        ps = conn.prepareStatement(sql);
                        ps.setString(1, "communityName");
                        ps.setString(2, rs.getString("eventName"));
                        ps.setInt(3, 0);
                        ps.setString(4, "");
                        System.out.println(ps);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void eventNotification(String to, String message, String title, String community) {
        JSONObject parent = new JSONObject();
        JSONObject notification = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("body", "message");
        data.put("title", title);
        data.put("communityName", community);
        parent.put("data", data);
        notification.put("body", message);
        notification.put("title", title);
        parent.put("to", to);
        parent.put("notification", notification);
        parent.put("priority", "high");
        messageWrite(parent);
    }
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
}

@SuppressWarnings("InfiniteLoopStatement")
class hangoutCheck implements Runnable {
    private Connection conn;
    private String sql;
    private PreparedStatement ps;
    private ResultSet rs;

    hangoutCheck(String PASS) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/Butterfly", "root", PASS);
            conn.getMetaData().getCatalogs();
            conn.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        ArrayList<String> communities = new ArrayList<>();
        while (true) {
            try {
                sql = "SELECT name FROM Communities";
                ps = conn.prepareStatement(sql);
                System.out.println(ps);
                rs = ps.executeQuery();
                if (rs.next()) {
                    String temp = rs.getString("name").replaceAll("\\s", "_");
                    temp += "_Hangouts";
                    communities.add(temp);
                    communities.forEach(this::timeCheckHangouts);
                    communities.clear();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    rs.close();
                    ps.close();
                    Thread.sleep(60000);
                } catch (SQLException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
        Function which loops through rows in the community hangouts table
        and removes the hangouts which have passed.
     */
    private void timeCheckHangouts(String communityHangout) {
        try {
            sql = "SELECT date, endTime, idHangouts FROM " + communityHangout;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
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
                        System.out.println("Events Thread " + ps);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
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
            JSONParser parser = new JSONParser();
            String appKey, PASS, Authorization;
            FileInputStream fileIn = new FileInputStream("/home/khanh/keys.txt");
            InputStreamReader isr = new InputStreamReader(fileIn, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            appKey = br.readLine();
            PASS = br.readLine();
            Authorization = br.readLine();
            br.close();
            isr.close();
            new Thread(new eventCheck(PASS, Authorization)).start();
            new Thread(new hangoutCheck(PASS)).start();
            while (true) {
                System.out.println("Waiting on port " + serverSocket.getLocalPort() + "...");
                try {
                    Socket server = serverSocket.accept();
                    System.out.println("Just connected to " + server.getRemoteSocketAddress());
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    sleep(100);
                    if (in.available() > 0) {
                        Object parsed = parser.parse(in.readUTF());
                        JSONObject obj = (JSONObject) parsed;
                        System.out.println("TIMESTAMP " + currentDate + " " + obj.get("function"));
                        new Thread(new serverStart(obj, out, appKey, PASS, Authorization)).start();

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
