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

    private Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs;
        String sql;
        while (true) {
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
                String first = (String) obj2.get("firstName");
                String last = (String) obj2.get("lastName");
                String google = (String) obj2.get("GoogleID");
                String dateCreated = (String) obj2.get("dateCreated");
                sql = "INSERT INTO Users VALUES('5', '" + first + "', '" + last + "', '" + google + "', '" + dateCreated + "')";
                System.out.println(sql);
                stmt.execute(sql);
                sql = "INSERT INTO Users VALUES('" + first + "', '" + last + "', '" + google + "', '" + dateCreated + "')";
                stmt.execute(sql);
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

    public static void main(String[] args) {
        int port = 60660;

        try {

            /*//INSERT VALUES INTO TABLE
            //FORMAT sql = "INSERT INTO <TABLENAME> VALUES (<listvalues>)";
            //watch for value type format
            //sql = "INSERT INTO Users VALUES(1, 'Khanh', 'Tran', 'Google', '2016-10-09')";
            sql = "%s %s %s ";
            rs = stmt.executeQuery(sql);

            //READ VALUES FROM TABLE
            //FORMAT sql = "SELECT <listvalues> FROM <TABLENAME>";
            //sql = "SELECT * FROM Users";
            rs = stmt.executeQuery(sql);
            while(rs.next()) {
                //retrieve data by column name
                //some var = rs.getType(<listvalue>);
                //if multiple values, read into arraylist, obj.put("fieldname", arraylist);
            }

            //UPDATE VALUES IN TABLE
            //FORMAT sql = "UPDATE <TABLENAME> SET <COLUMNNAME> = 'NEWVALUE' WHERE 'IDVALUE' = 'ID'";
            //sql = "UPDATE Users SET GoogleID = 'notGoggly' WHERE idUsers='1'";
            //multiple "UPDATE Users SET GoogleID = "notGoggly", firstName = "K" WHERE idUsers = 1";
            rs = stmt.executeQuery(sql);

            //DELETE ROW FROM TABLE
            //FORMAT sql = "DELETE FROM <TABLENAME> WHERE <ID> = <desiredID>";
            */Thread t = new Server(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        //} finally {
            /*try {
                if (stmt!=null)
                    stmt.close();
            } catch(SQLException se2) {
            }// nothing we can do
            try {
                if (conn!=null)
                    conn.close();
            } catch(SQLException se) {
                se.printStackTrace();
            }*/
        }
    }
}
