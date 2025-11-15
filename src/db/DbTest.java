package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbTest {
    public static void main(String[] args) {
        try {
            Connection conn =
                    DriverManager.getConnection("jdbc:mysql://localhost/crisms_db?" +
                            "user=root&password=p@ssword");
            Statement statement = conn.createStatement();
            String query = "SELECT * FROM stock_locations";
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                System.out.println(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3));
            }

            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
