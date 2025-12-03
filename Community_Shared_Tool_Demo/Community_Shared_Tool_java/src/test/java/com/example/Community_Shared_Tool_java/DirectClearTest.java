package com.example.Community_Shared_Tool_java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DirectClearTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void clearAllDataDirectly() {
        System.out.println("=== 直接清理所有数据 ===");
        
        // 直接使用SQL语句删除所有记录
        String[] tablesToClear = {
            "borrow_info",     // 借用记录
            "borrow_record"    // 借用申请记录
        };
        
        for (String tableName : tablesToClear) {
            try {
                // 获取删除前的记录数
                long countBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                
                // 执行删除操作
                int rowsDeleted = jdbcTemplate.update("DELETE FROM " + tableName);
                
                // 获取删除后的记录数
                long countAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                
                System.out.println(tableName + " 表：");
                System.out.println("  删除前记录数: " + countBefore);
                System.out.println("  成功删除: " + rowsDeleted + " 条记录");
                System.out.println("  删除后记录数: " + countAfter);
                System.out.println();
            } catch (Exception e) {
                System.out.println("操作 " + tableName + " 表时出现错误: " + e.getMessage());
                System.out.println();
            }
        }
        
        System.out.println("=== 直接清理完成 ===");
    }
}
