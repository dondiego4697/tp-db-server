package sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.*;

/**
 * Created by Denis on 17.02.2017.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ResultSet resultSet = null;
        final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/testarea";
        final String USER = "postgres";
        final String PASS = "dondiego666";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            final Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
            final Statement statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM movie");
            while (resultSet.next()) {
                System.out.print(resultSet.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SpringApplication.run(Application.class, args);
    }
}
