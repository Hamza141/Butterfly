/*
 * Created by Khanh Tran on 10/9/16.
 */

import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.io.*;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;
import com.google.firebase.*;
import org.json.simple.parser.*;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class Server extends Thread {
    static final private String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final private String DB_URL = "jdbc:mysql://localhost/Butterfly";
    static final private String USER = "root";
    static private String appKey, PASS, Authorization;
    private Connection conn = null;
    private Statement stmt = null;
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
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            try {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                rs = conn.getMetaData().getCatalogs();
                stmt = conn.createStatement();
                while (in.available() > 0) {
                    Object parsed = parser.parse(in.readUTF());
                    JSONObject obj = (JSONObject) parsed;
                    String function = (String) obj.get("function");
                    System.out.println(function);
                    if (function.equals("addUser")) {
                        addUser(obj);
                    } else if (function.equals("updateInstanceID")) {
                        updateInstanceID(obj);
                    } else if (function.equals("addCommunity")) {
                        addCommunity(obj);
                    } else if (function.equals("getCommunities")) {
                        getCommunities();
                    } else if (function.equals("addCommunityUser")) {
                        addCommunityUser(obj);
                    } else if (function.equals("getCommunityUsers")) {
                        getCommunityUsers((String) obj.get("communityName"));
                    } else if (function.equals("leaveCommunityUser")
                            || function.equals("removeCommunityUser")) {
                        leaveCommunityUser(obj);
                    } else if (function.equals("getMessages")) {
                        getMessages((String) obj.get("communityName"));
                    } else if (function.equals("addMessage")) {
                        addMessage(obj);
                    } else if (function.equals("addEvent")) {
                        addEvent(obj);
                    } else if (function.equals("getEvents")) {
                        getEvents((String) obj.get("communityName"));
                    } else if (function.equals("getNeighborhoodEvents")) {
                        getNeighborhoodEvents();
                    } else if (function.equals("emailInvite")) {
                        sendInvite((String) obj.get("from"), (String) obj.get("fromName"),
                                (String) obj.get("to"));
                    } else if (function.equals("genericNotification")) {
                        genericNotification(getInstanceID((String) obj.get("googleID")),
                                (String) obj.get("message"));
                    } else if (function.equals("pingNotification")) {
                        pingNotification((String) obj.get("googleID"));
                    } else if (function.equals("upcomingEventNotification")) {
                        upcomingEventNotification((String) obj.get("googleID"));
                    } else if (function.equals("updateUserProfile")) {
                        updateUserProfile(obj);
                    } else if (function.equals("getUserCommunityEvents")) {
                        getUserCommunityEvents((String) obj.get("googleID"));
                    }
                }
            } catch (org.json.simple.parser.ParseException | IOException
                    | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUserProfile(JSONObject obj) {
        //TODO other variables for user profile
        //TODO update communitiesList
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT * FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getMessages(String communityName) {
        communityName = communityName.replaceAll("\\s", "_"); communityName += "_Board";
        JSONObject obj;
        try {
            sql = "SELECT * FROM " + communityName;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                obj = new JSONObject();
                obj.put("pinned", rs.getString("pinned"));
                obj.put("name", rs.getString("name"));
                obj.put("message", rs.getString("message"));
                System.out.println(obj);
                out.writeUTF(obj.toString());
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_"); communityName += "_Board";
        String pinned = (String) obj.get("pinned"); String name = (String) obj.get("name");
        String message = (String) obj.get("message");
        try {
            sql = "INSERT INTO " + communityName + " (pinned, name, message) VALUES(?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, pinned); ps.setString(2, name); ps.setString(3, message);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        newBoardNotification(communityName, name, message);
    }

    private void getCommunities() {
        //TODO changed formatting for return string
        String comma = ", ";
        StringBuilder communities = new StringBuilder();
        try {
            sql = "SELECT * FROM Communities";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                communities.append(rs.getString("name"));
                communities.append(comma);
            }
            communities.setLength(communities.length() - 2);
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
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
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

    private void leaveCommunityUser(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        String idUsers = (String) obj.get("idUsers");
        try {
            sql = "DELETE FROM " + communityName + " WHERE idUsers = " + idUsers;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendInvite(String from, String fromName, String to) {
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
            throw new RuntimeException(e);
        }
    }

    private void getUserCommunityEvents(String googleID) {
        try {
            sql = "SELECT communities FROM Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
            System.out.println(ps);
            rs = ps.executeQuery();
            String list;
            if (rs.next()) {
                list = rs.getString("communitiesList");
                ArrayList<String> communities = new ArrayList<>(Arrays.asList(list.split(" ,")));
                communities.forEach(this::getEvents);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void getNeighborhoodEvents() {
        try {
            sql = "SELECT name FROM Communities";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                ArrayList<String> communities = new ArrayList<>();
                communities.add(rs.getString("name"));
                communities.forEach(this::getEvents);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getEvents(String communityName) {
        String oldName = communityName;
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        try {
            sql = "SELECT * FROM " + communityName;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            JSONObject obj;
            while (rs.next()) {
                obj = new JSONObject();
                obj.put("name", rs.getString("name")); obj.put("city", rs.getString("city"));
                obj.put("date", rs.getString("date")); obj.put("address", rs.getString("address"));
                obj.put("state", rs.getString("state")); obj.put("zip", rs.getString("zip"));
                obj.put("description", rs.getString("description")); obj.put("communityName", oldName);
                obj.put("locationName", rs.getString("locationName"));
                obj.put("numAttendees", rs.getString("numAttendees"));
                System.out.println(obj.toString());
                out.writeUTF(obj.toString());
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkifLeader(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        String googleID = (String) obj.get("googleID");
        try {
            sql = "SELECT isLeader from " + communityName + " WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, googleID);
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
    }

    private void addEvent(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Calendar";
        //TODO client not sending communityName
        String name = (String) obj.get("name"); String description = (String) obj.get("description");
        String date = (String) obj.get("date"); String city = (String) obj.get("city");
        String state = (String) obj.get("state"); String address = (String) obj.get("address");
        String zip = (String) obj.get("zip");
        String locationName = (String) obj.get("locationName");
        String numAttendees = (String) obj.get("numAttendees");
        try {
            sql = "INSERT INTO " + communityName + " (name, description, date, city, state, "
                    + "address, zip, locationName, numAttendees) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name); ps.setString(2, description); ps.setString(3, date);
            ps.setString(4, city); ps.setString(5, state); ps.setString(6, address);
            ps.setString(7, zip); ps.setString(8, locationName); ps.setString(9, numAttendees);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteEvent(JSONObject obj) {

    }

    private void createCommunityUserTable(String communityName) {
        //DATE: YYYY-MM-DD
        //TODO need more than googleID and isLeader?
        communityName += "_Users";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idUsers INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, " + "firstName VARCHAR(255), "
                + "lastName VARCHAR(255), " + "googleID VARCHAR(255), " + "isLeader TINYINT(1), "
                + "subscribed INT(1))";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityEventTable(String communityName) {
        // DATETIME: YYYY-MM-DD HH:MM:SS, DATE: YYYY-MM-DD
        communityName += "_Calendar";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idEvents INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, " + "name VARCHAR(255), "
                + "description VARCHAR(255), " + "date DATE, " + "city VARCHAR(255), "
                + "state VARCHAR(255), " + "address VARCHAR(255), " + "zip VARCHAR(255), "
                + "locationName VARCHAR(255), " + "numAttendees INT(4))";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityBoardTable(String communityName) {
        //TODO: figure out what variables needed for a board table. id, message, name, date
        communityName += "_Board";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "idEvents INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, "
                + "pinned INT(4), " + "name VARCHAR(255), "
                + "message TEXT CHARACTER SET latin1 COLLATE latin1_general_cs)";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCommunity(JSONObject obj) {
        String idCommunities = "0"; String neighborhoodID = "0"; String numUpcomgEvents = "0";
        String numMembers = "1"; String category = (String) obj.get("category");
        String name = (String) obj.get("name"); String subCategory = (String) obj.get("subCategory");
        String Private = (String) obj.get("private");
        String description = (String) obj.get("description");
        String dateCreated = (String) obj.get("dateCreated");
        try {
            sql = "SELECT count(*) from Communities WHERE name = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    sql = "INSERT INTO Communities VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, idCommunities); ps.setString(2, neighborhoodID);
                    ps.setString(3, category); ps.setString(4, subCategory);
                    ps.setString(5, name); ps.setString(6, description);
                    ps.setString(7, numMembers); ps.setString(8, numUpcomgEvents);
                    ps.setString(9, dateCreated); ps.setString(10, Private);
                    System.out.println(ps);
                    ps.executeUpdate();
                    name = name.replaceAll("\\s", "_");
                    createCommunityUserTable(name);
                    createCommunityEventTable(name);
                    createCommunityBoardTable(name);
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
        String idUsers = (String) obj.get("idUsers");
        String isLeader = (String) obj.get("isLeader");
        try {
            sql = "INSERT INTO " + communityName
                    + " SELECT * FROM Users WHERE idUsers = " + idUsers;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
            sql = "UPDATE " + communityName + " SET isLeader = ? WHERE idUsers = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, isLeader); ps.setString(2, idUsers);
            System.out.println(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateInstanceID(JSONObject obj) {
        try {
            String googleID = (String) obj.get("googleID");
            String instanceID = (String) obj.get("instanceID");
            sql = "UPDATE Users SET instanceID = ? WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, instanceID); ps.setString(2, googleID);
            System.out.println(ps);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addUser(JSONObject obj) {
        String first = (String) obj.get("firstName");
        String last = (String) obj.get("lastName");
        String google = (String) obj.get("googleID");
        String instanceID = (String) obj.get("instanceID");
        try {
            sql = "SELECT count(*) from Users WHERE googleID = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, google);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    sql = "INSERT INTO Users (firstName, lastName, googleID, instanceID) VALUES (?, ?, ?, ?)";
                    ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, first); ps.setString(2, last); ps.setString(3, google);
                    ps.setString(4, instanceID);
                    System.out.println(ps);
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys();
                    int key = 0;
                    if (rs.next()) {
                        key = rs.getInt(1);
                        System.out.println(key);
                    }
                    String id = Integer.toString(key);
                    out.writeUTF(id);
                } else {
                    out.writeUTF("-1");
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private String getInstanceID(String googleID) {
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
        System.out.println(parent);
        notificationWrite(parent);
    }

    private void pingNotification(String to) {
        //TODO what is a ping, what does it contain
    }

    private void upcomingEventNotification(String to) {
        /*TODO loop through each calendar table
          TODO figure out when it is 24 hours before an event time
          TODO send notification to all users in that community
          TODO will have to be always running to be prompt
        */
    }

    private void newBoardNotification(String community, String name, String message) {
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

    public static void main(String[] args) {
        int port = 3300;
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
            Thread t = new Server(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
