package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbTest {
    public static void main(String[] args) {
        try {
            Connection conn =
                    DriverManager.getConnection("jdbc:mysql://localhost/crisms_db?" +
                            "user=root&password=p@ssword");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
