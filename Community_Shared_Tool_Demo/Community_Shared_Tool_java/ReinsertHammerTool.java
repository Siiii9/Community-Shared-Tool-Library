import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ReinsertHammerTool {
    public static void main(String[] args) {
        // 数据库连接信息
        String url = "jdbc:mysql://localhost:3306/exam?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123456";
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立连接
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("数据库连接成功！");
            
            // 设置事务自动提交为false
            connection.setAutoCommit(false);
            
            // 1. 插入hammer工具到published_tool表
            String insertToolSql = "INSERT INTO published_tool (tool_name, tool_type, description, location, status, owner_id, borrower_id, borrow_days_limit) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(insertToolSql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, "hammer");
            stmt.setString(2, "hand tools"); // 明确设置tool_type
            stmt.setString(3, "A sturdy hammer for general use");
            stmt.setString(4, "Community Center");
            stmt.setString(5, "borrowing");
            stmt.setInt(6, 3); // 发出人是3
            stmt.setInt(7, 2); // 借的人是2
            stmt.setInt(8, 30); // 最大借用天数30天
            
            int toolRowsAffected = stmt.executeUpdate();
            System.out.println("插入工具记录：" + toolRowsAffected + " 行受影响");
            
            // 获取生成的tool_id
            int toolId = 0;
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                toolId = rs.getInt(1);
            }
            System.out.println("生成的工具ID：" + toolId);
            
            // 关闭当前的stmt和rs
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            
            // 2. 插入借用信息到borrow_info表
            String insertBorrowSql = "INSERT INTO borrow_info (tool_id, tool_name, tool_type, borrower_id, owner_id, borrow_time, expected_return_time, status) "
                                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(insertBorrowSql);
            stmt.setInt(1, toolId);
            stmt.setString(2, "hammer");
            stmt.setString(3, "hand tools"); // 为borrow_info表设置tool_type
            stmt.setInt(4, 2); // 借的人是2
            stmt.setInt(5, 3); // 发出人是3
            stmt.setTimestamp(6, new Timestamp(new Date().getTime()));
            
            // 计算预计归还时间（7天后）
            Date expectedReturnDate = new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000L);
            stmt.setTimestamp(7, new Timestamp(expectedReturnDate.getTime()));
            
            stmt.setString(8, "borrowing");
            
            int borrowRowsAffected = stmt.executeUpdate();
            System.out.println("插入借用记录：" + borrowRowsAffected + " 行受影响");
            
            // 提交事务
            connection.commit();
            System.out.println("hammer工具信息重新插入成功！");
            
        } catch (ClassNotFoundException e) {
            System.err.println("驱动加载失败：" + e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQL执行失败：" + e.getMessage());
            // 回滚事务
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("事务回滚失败：" + ex.getMessage());
            }
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