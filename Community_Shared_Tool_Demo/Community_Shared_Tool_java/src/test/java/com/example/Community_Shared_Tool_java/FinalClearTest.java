package com.example.Community_Shared_Tool_java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class FinalClearTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 禁用事务管理
    public void clearAllDataPermanently() {
        System.out.println("=== 永久清理所有数据 ===");
        
        // 定义需要清理的表
        String[] tables = {
            "borrow_info",     // 借用记录
            "borrow_record",   // 借用申请记录
            "published_tool"   // 发布的工具
        };
        
        for (String tableName : tables) {
            try {
                // 获取当前记录数
                long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                System.out.println("清理 " + tableName + " 表：");
                System.out.println("  当前记录数: " + count);
                
                if (count > 0) {
                    // 执行删除操作
                    int rowsDeleted = jdbcTemplate.update("DELETE FROM " + tableName);
                    System.out.println("  成功删除: " + rowsDeleted + " 条记录");
                    
                    // 验证删除结果
                    long newCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                    System.out.println("  删除后记录数: " + newCount);
                } else {
                    System.out.println("  表中没有记录需要删除");
                }
            } catch (Exception e) {
                System.out.println("  操作失败: " + e.getMessage());
            }
            System.out.println();
        }
        
        System.out.println("=== 永久清理完成 ===");
    }
}
