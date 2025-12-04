import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class check_table_structure {
    public static void main(String[] args) {
        // 数据库连接信息
        String url = "jdbc:mysql://localhost:3306/exam?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123456";
        
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立连接
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功！");
            
            // 查看published_tool表结构
            stmt = connection.createStatement();
            String sql = "DESCRIBE published_tool";
            rs = stmt.executeQuery(sql);
            
            System.out.println("published_tool表结构：");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " (" + rs.getString(2) + ") - " + rs.getString(3));
            }
            
            // 关闭当前的rs
            if (rs != null) rs.close();
            
            // 查看borrow_info表结构
            sql = "DESCRIBE borrow_info";
            rs = stmt.executeQuery(sql);
            
            System.out.println("\nborrow_info表结构：");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " (" + rs.getString(2) + ") - " + rs.getString(3));
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("驱动加载失败：" + e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQL执行失败：" + e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("资源关闭失败：" + e.getMessage());
            }
        }
    }
}