import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class InspectDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/college_freshman_helper?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false";
        String username = "root";
        String password = "12345678";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            printCount(statement, "SELECT COUNT(*) AS c FROM sys_user", "sys_user");
            printCount(statement, "SELECT COUNT(*) AS c FROM forum_post", "forum_post");
            printCount(statement, "SELECT COUNT(*) AS c FROM forum_reply", "forum_reply");
            printCount(statement, "SELECT COUNT(*) AS c FROM forum_like_record", "forum_like_record");

            try (ResultSet rs = statement.executeQuery(
                    "SELECT id, username, nickname, role, status FROM sys_user WHERE deleted = 0 ORDER BY created_at ASC LIMIT 20")) {
                System.out.println("users:");
                while (rs.next()) {
                    System.out.printf("%s | %s | %s | role=%s | status=%s%n",
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("nickname"),
                            rs.getInt("role"),
                            rs.getInt("status"));
                }
            }
        }
    }

    private static void printCount(Statement statement, String sql, String label) throws Exception {
        try (ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println(label + "=" + rs.getLong("c"));
            }
        }
    }
}
