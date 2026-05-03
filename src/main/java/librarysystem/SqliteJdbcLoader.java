package librarysystem;

public final class SqliteJdbcLoader {

    private SqliteJdbcLoader() {
    }

    public static void loadDriver() throws Exception {
        Class.forName("org.sqlite.JDBC");
    }
}
