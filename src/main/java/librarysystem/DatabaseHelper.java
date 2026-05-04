package librarysystem;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DatabaseHelper {

    public static final Object DB_LOCK = new Object();

    private static final String JDBC_URL_PART = "jdbc:sqlite:";
    private static String fullUrl = "";

    private DatabaseHelper() {
    }

    public static Connection openConnection() throws Exception {
        Connection conn = DriverManager.getConnection(fullUrl);
        conn.setAutoCommit(true);
        return conn;
    }

    public static void makeDatabaseReady() throws Exception {
        String folderPath = "database";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        fullUrl = JDBC_URL_PART + folderPath + File.separator + "stmarys_library.sqlite";
        SqliteJdbcLoader.loadDriver();
        createTablesAndSampleDataIfNeeded();
    }

    private static void createTablesAndSampleDataIfNeeded() throws Exception {
        int numBookCount = 0;

        synchronized (DB_LOCK) {
            Connection conn = openConnection();

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS books ("
                    + "book_id INTEGER PRIMARY KEY, "
                    + "title TEXT NOT NULL, "
                    + "author TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "availability_status TEXT NOT NULL)");
            stmt.close();

            stmt = conn.createStatement();
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS members ("
                    + "member_id INTEGER PRIMARY KEY, "
                    + "member_name TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "membership_type TEXT NOT NULL)");
            stmt.close();

            stmt = conn.createStatement();
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS borrow_records ("
                    + "record_id INTEGER PRIMARY KEY, "
                    + "book_id INTEGER NOT NULL, "
                    + "member_id INTEGER NOT NULL, "
                    + "borrow_date DATE NOT NULL, "
                    + "due_date DATE NOT NULL, "
                    + "return_status TEXT NOT NULL)");
            stmt.close();

            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS num FROM books");
            if (rs.next()) {
                numBookCount = rs.getInt("num");
            }
            rs.close();
            stmt.close();

            if (numBookCount == 0) {
                stmt = conn.createStatement();
                stmt.executeUpdate(
                    "INSERT INTO books (title, author, category, availability_status) VALUES "
                        + "('Introduction to Java', 'John Smith', 'Programming', 'Available'), "
                        + "('Database Systems', 'Maria Garcia', 'Computer Science', 'Borrowed'), "
                        + "('Software Engineering Principles', 'Alan Brown', 'Engineering', 'Available')");
                stmt.close();

                stmt = conn.createStatement();
                stmt.executeUpdate(
                    "INSERT INTO members (member_name, email, membership_type) VALUES "
                        + "('Alice Johnson', 'alice.johnson@stmarys.ac.uk', 'Student'), "
                        + "('Michael Lee', 'michael.lee@stmarys.ac.uk', 'Staff'), "
                        + "('Sara Ahmed', 'sara.ahmed@stmarys.ac.uk', 'Student')");
                stmt.close();

                stmt = conn.createStatement();
                stmt.executeUpdate(
                    "INSERT INTO borrow_records (book_id, member_id, borrow_date, due_date, return_status) VALUES "
                        + "(2, 1, '2025-03-01', '2025-03-15', 'Borrowed'), "
                        + "(1, 2, '2025-03-02', '2025-03-16', 'Returned'), "
                        + "(3, 3, '2025-03-05', '2025-03-19', 'Borrowed')");
                stmt.close();
            }
            conn.close();
        }
    }
}
