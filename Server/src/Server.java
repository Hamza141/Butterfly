/**
 * Created by nick on 10/9/16.
 */

import java.net.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;
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
    private DataOutputStream out;
    private DataInputStream in;
    private JSONParser parser;

    private Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    public void run() {
        while (true) {
            try {
                URL myip = new URL("http://checkip.amazonaws.com");
                BufferedReader buffIn = new BufferedReader(new InputStreamReader(myip.openStream()));
                String ip = buffIn.readLine();
                System.out.println(ip);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                    Object obj = parser.parse(in.readUTF());
                    JSONObject obj2 = (JSONObject) obj;
                    String function = (String) obj2.get("function");
                    if (function.equals("addUser")) {
                        addUser(obj2);
                    } else if (function.equals("getCommunityUsers")) {
                        getCommunityUsers((String) obj2.get("communityName"));
                    } else if (function.equals("addCommunityUser")) {
                        addCommunityUser(obj2);
                    } else if (function.equals("addCommunity")) {
                        addCommunity(obj2);
                    } else if (function.equals("addEvent")) {
                        addEvent(obj2);
                    } else if (function.equals("getEvents")) {
                        getEvents((String) obj2.get("communityName"));
                    } else if (function.equals("getNeighborhoodEvents")) {
                        getNeighborhoodEvents();
                    } else if (function.equals("emailInvite")) {
                        sendInvite((String) obj2.get("from"), (String) obj2.get("fromName"),
                                (String) obj2.get("to"));
                    } else if (function.equals("leaveCommunityUser")) {
                        leaveCommunityUser(obj2);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (org.json.simple.parser.ParseException p) {

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCommunityUsers(String communityName) {
        communityName += "_Users";
        try {
            String sql = "SELECT * FROM " + communityName;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            JSONObject obj;
            while (rs.next()) {
                obj = new JSONObject();
                obj.put("idUsers", rs.getString("idUsers")); obj.put("firstName", rs.getString("firstName"));
                obj.put("lastName", rs.getString("lastName")); obj.put("googleID", rs.getString("googleID"));
                System.out.println(obj.toString());
                out.writeUTF(obj.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void leaveCommunityUser(JSONObject obj) {
        String communityName = (String) obj.get("communityName");
        communityName = communityName.replaceAll("\\s", "_");
        communityName += "_Users";
        String idUsers = (String) obj.get("idUsers");
        try {
            String sql = "DELETE FROM " + communityName + " WHERE idUsers = " + idUsers;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
            out.writeUTF("1");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendInvite(String from, String fromName, String to) {
        final String password = "gmktgecmvmrtdgia";
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

    private void getNeighborhoodEvents() {
        String sql = "SELECT name FROM Communities";
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            ArrayList<String> communities = new ArrayList<>();
            while (rs.next()) {
                communities.add(rs.getString("name"));
            }
            System.out.println("call");
            communities.forEach((name) -> getEvents(name));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getEvents(String communityName) {
        communityName += "_Calendar";
        try {
            String sql = "SELECT * FROM " + communityName;
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
        communityName += "_Calendar";
        String idEvents = "0";
        String name = (String) obj.get("name");
        String description = (String) obj.get("description");
        String date = (String) obj.get("date");
        String city = (String) obj.get("city");
        String state = (String) obj.get("state");
        String address = (String) obj.get("address");
        String zipcode = (String) obj.get("zipcode");
        String locationName = (String) obj.get("locationName");
        String numAttendees = (String) obj.get("numAttendees");
        try {
            String sql = "INSERT INTO " + communityName + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, idEvents); ps.setString(2, name);
            ps.setString(3, description); ps.setString(4, date); ps.setString(5, city);
            ps.setString(6, state); ps.setString(7, address); ps.setString(8, zipcode);
            ps.setString(9, locationName); ps.setString(10, numAttendees);
            System.out.println(ps);
            ps.executeUpdate();
            out.writeUTF("1");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityUserTable(String communityName) {
        //DATE: YYYY-MM-DD
        communityName += "_Users";
        String newTable = "CREATE TABLE " + communityName + " ("
            + "idUsers INT(4) AUTO_INCREMENT NOT NULL PRIMARY KEY, " + "firstName VARCHAR(255), " + "lastName VARCHAR(255), "
            + "googleID VARCHAR(255), " + "isLeader TINYINT(1))";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
            out.writeUTF("1");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityEventTable(String communityName) {
        // DATETIME: YYYY-MM-DD HH:MM:SS, DATE: YYYY-MM-DD
        communityName += "_Calendar";
        String newTable = "CREATE TABLE " + communityName + " ("
            + "idEvents INT(4), " + "name VARCHAR(255), " + "description VARCHAR(255), "
            + "date DATE, " + "city VARCHAR(255), " + "state VARCHAR(255), "
            + "address VARCHAR(255), " + "zipcode VARCHAR(255), " + "locationName VARCHAR(255), "
            + "numAttendees INT(4))";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
            out.writeUTF("1");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCommunityBoardTable(String communityName) {
        //TODO: figure out what variables needed for a board table.
        communityName += "_Board";
        String newTable = "CREATE TABLE " + communityName + " ("
                + "";
        System.out.println(newTable);
        try {
            stmt.executeUpdate(newTable);
            out.writeUTF("1");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
                    name = name.replaceAll("\\s", "_");
                    createCommunityUserTable(name);
                    createCommunityEventTable(name);
                    createCommunityBoardTable(name);
                    out.writeUTF("1");
                }
            } else {
                out.writeUTF("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
            String sql = "INSERT INTO " + communityName + " SELECT * FROM Users WHERE idUsers = " + idUsers;
            ps = conn.prepareStatement(sql);
            System.out.println(ps);
            ps.executeUpdate();
            sql = "UPDATE " + communityName + " SET isLeader = ? WHERE idUsers = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, isLeader);
            ps.setString(2, idUsers);
            System.out.println(ps);
            ps.executeUpdate();
            out.writeUTF("1");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addUser(JSONObject obj) {
        String first = (String) obj.get("firstName");
        String last = (String) obj.get("lastName");
        String google = (String) obj.get("googleID");
        try {
            queryCheck = "SELECT count(*) from Users WHERE googleID = ?";
            ps = conn.prepareStatement(queryCheck);
            ps.setString(1, google);
            System.out.println(ps);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) == 0) {
                    String sql = "INSERT INTO Users (firstName, lastName, googleID) VALUES (?, ?, ?)";
                    ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, first);
                    ps.setString(2, last);
                    ps.setString(3, google);
                    System.out.println(ps);
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys();
                    int key = 0;
                    if (rs.next()) {
                        key = rs.getInt(1);
                    }
                    String id = Integer.toString(key);
                    out.writeUTF(id);
                }
            } else {
                out.writeUTF("0");
            }
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
