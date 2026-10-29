import java.sql.*;
final class Database {
    static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://127.0.0.1:13350/csc3350_test?sslMode=DISABLED&allowPublicKeyRetrieval=true", "root", "");
    }
}
