package com.example.Community_Shared_Tool_java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class VerifyClearTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void verifyAllDataCleared() {
        System.out.println("=== 验证数据清理结果 ===");
        
        // 检查所有相关表的数据量
        String[] tables = {
            "borrow_info",         // 借用记录
            "borrow_record",       // 借用申请记录
            "published_tool",      // 发布的工具
            "device_index",        // 可能的工具表
            "tool",                // 可能的工具表
            "tools"                // 可能的工具表
        };
        
        boolean allCleared = true;
        
        for (String tableName : tables) {
            try {
                long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                System.out.println(tableName + " 表中的记录数量: " + count);
                
                if (count > 0) {
                    allCleared = false;
                }
            } catch (Exception e) {
                System.out.println(tableName + " 表不存在或无法访问: " + e.getMessage());
            }
        }
        
        System.out.println("\n=== 清理验证结果 ===");
        if (allCleared) {
            System.out.println("✅ 所有借用记录和发布的工具都已成功删除！");
        } else {
            System.out.println("❌ 仍有部分数据未被删除，请检查！");
        }
    }
}
