import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/project-programming-3";
    private static final String USER = "your_mysql_username";
    private static final String PASSWORD = "your_mysql_password";

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database Connected Successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}