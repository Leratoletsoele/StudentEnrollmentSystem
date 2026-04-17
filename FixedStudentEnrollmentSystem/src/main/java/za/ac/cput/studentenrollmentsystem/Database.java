package za.ac.cput.studentenrollmentsystem;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String HOST   = "localhost";
    private static final int    PORT   = 1527;
    private static final String URL    = "jdbc:derby://" + HOST + ":" + PORT + "/StudentEnrollmentDB;create=true";
    private static final String USER   = "administrator";
    private static final String PASS   = "admin";
    private static final String SCHEMA = "ADMINISTRATOR";

    /** Main connection entrypoint (auto starts Derby server if needed, creates schema/tables). */
    public static Connection getConnection() throws SQLException {
        try {
            return connectAndInit();
        } catch (SQLException first) {
            if (isConnectRefused(first)) {
                autoStartDerbyNetworkServer();
                SQLException last = first;
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(500);
                        return connectAndInit();
                    } catch (SQLException e) {
                        last = e;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                throw last;
            }
            throw first;
        }
    }

    private static Connection connectAndInit() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USER, PASS);

        try (Statement s = con.createStatement()) {
            try {
                s.executeUpdate("SET SCHEMA " + SCHEMA);
            } catch (SQLException e) {
                if ("42Y07".equals(e.getSQLState())) { // schema missing
                    s.executeUpdate("CREATE SCHEMA " + SCHEMA);
                    s.executeUpdate("SET SCHEMA " + SCHEMA);
                } else {
                    throw e;
                }
            }
        }

        createTables(con);
        return con;
    }

    private static boolean isConnectRefused(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        String st  = e.getSQLState();
        return msg.contains("connection refused") || "08001".equals(st) || "08006".equals(st);
    }

    /** Try to start Derby Network Server automatically if derbynet is on classpath. */
    private static void autoStartDerbyNetworkServer() {
        try {
            Class<?> nsc = Class.forName("org.apache.derby.drda.NetworkServerControl");
            Object ctrl = nsc.getConstructor(InetAddress.class, int.class)
                    .newInstance(InetAddress.getByName(HOST), PORT);
            nsc.getMethod("start", PrintWriter.class).invoke(ctrl, new PrintWriter(System.out, true));
            System.out.println("Attempted to start Derby Network Server on " + HOST + ":" + PORT);
        } catch (ClassNotFoundException cnf) {
            System.out.println("derbynet.jar not on classpath; start Derby server manually.");
        } catch (Exception ex) {
            System.out.println("Failed to auto-start Derby server: " + ex.getMessage());
        }
    }

    /** Create required tables if missing (idempotent). */
    private static void createTables(Connection con) {
        try (Statement stmt = con.createStatement()) {
            execCreate(stmt,
                "CREATE TABLE USERS (" +
                "  USER_ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                "  ROLE VARCHAR(20) NOT NULL, " +          // 'STUDENT'
                "  FULL_NAME VARCHAR(80) NOT NULL, " +
                "  STUDENT_NUMBER VARCHAR(30) UNIQUE NOT NULL, " +
                "  EMAIL VARCHAR(120), " +
                "  PASSWORD_HASH VARCHAR(200) NOT NULL, " +
                "  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");

            execCreate(stmt,
                "CREATE TABLE APPLICATIONS (" +
                "  APP_ID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                "  FULL_NAME VARCHAR(80) NOT NULL, " +
                "  STUDENT_NUMBER VARCHAR(30) NOT NULL, " +
                "  PROGRAM VARCHAR(60) NOT NULL, " +
                "  SPECIALIZATION VARCHAR(60) NOT NULL, " +
                "  COURSES VARCHAR(400) NOT NULL, " +
                "  STATUS VARCHAR(20) NOT NULL DEFAULT 'Pending'" +
                ")");
        } catch (SQLException e) {
            System.out.println("DDL error: " + e.getMessage() + " [SQLState=" + e.getSQLState() + "]");
        }
    }

    private static void execCreate(Statement stmt, String sql) throws SQLException {
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            if (!"X0Y32".equals(e.getSQLState())) { // X0Y32 = table already exists
                throw e;
            }
        }
    }

    // --- Minimal password hashing (static salt for coursework) ---
    public static String hashPassword(String raw) {
        final String SALT = "uni-app-v1";
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((SALT + raw).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }

    public static boolean verifyPassword(String raw, String storedHash) {
        return hashPassword(raw).equals(storedHash);
    }

    // ----- Simple DB helpers used by GUIs -----

    public static boolean userExists(String studentNumber) throws SQLException {
        String sql = "SELECT 1 FROM USERS WHERE STUDENT_NUMBER=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void registerStudent(String fullName, String studentNumber, String email, String password) throws SQLException {
        String sql = "INSERT INTO USERS (ROLE, FULL_NAME, STUDENT_NUMBER, EMAIL, PASSWORD_HASH) VALUES (?,?,?,?,?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "STUDENT");
            ps.setString(2, fullName);
            ps.setString(3, studentNumber);
            ps.setString(4, email);
            ps.setString(5, hashPassword(password));
            ps.executeUpdate();
        }
    }

    public static boolean authenticateStudent(String studentNumber, String password) throws SQLException {
        String sql = "SELECT PASSWORD_HASH FROM USERS WHERE STUDENT_NUMBER=? AND ROLE='STUDENT'";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                return verifyPassword(password, rs.getString(1));
            }
        }
    }

    public static int addApplication(String fullName, String studentNumber, String program, String specialization, String coursesCsv) throws SQLException {
        String sql = "INSERT INTO APPLICATIONS (FULL_NAME, STUDENT_NUMBER, PROGRAM, SPECIALIZATION, COURSES) VALUES (?,?,?,?,?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, studentNumber);
            ps.setString(3, program);
            ps.setString(4, specialization);
            ps.setString(5, coursesCsv);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public static List<String[]> findApplicationsByStudent(String studentNumber) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT APP_ID, FULL_NAME, STUDENT_NUMBER, PROGRAM, SPECIALIZATION, COURSES, STATUS " +
                     "FROM APPLICATIONS WHERE STUDENT_NUMBER=? ORDER BY APP_ID DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        String.valueOf(rs.getInt(1)),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7)
                    });
                }
            }
        }
        return rows;
    }

    public static List<String[]> findAllApplications() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT APP_ID, FULL_NAME, STUDENT_NUMBER, PROGRAM, SPECIALIZATION, COURSES, STATUS " +
                     "FROM APPLICATIONS ORDER BY APP_ID DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{
                    String.valueOf(rs.getInt(1)),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getString(7)
                });
            }
        }
        return rows;
    }

    public static int updateApplicationStatus(int appId, String status) throws SQLException {
        String sql = "UPDATE APPLICATIONS SET STATUS=? WHERE APP_ID=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appId);
            return ps.executeUpdate();
        }
    }

    public static int deleteApplication(int appId) throws SQLException {
        String sql = "DELETE FROM APPLICATIONS WHERE APP_ID=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, appId);
            return ps.executeUpdate();
        }
    }
}
