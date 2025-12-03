package com.example.Community_Shared_Tool_java;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class ClearPublishedToolsTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void clearPublishedTools() {
        System.out.println("=== 清理发布的工具 ===");
        
        // 检查并删除published_tool表中的所有记录
        try {
            long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM published_tool", Long.class);
            System.out.println("删除前 published_tool 表中的记录数量: " + count);
            
            if (count > 0) {
                int rowsDeleted = jdbcTemplate.update("DELETE FROM published_tool");
                System.out.println("成功删除 " + rowsDeleted + " 条发布的工具记录");
            } else {
                System.out.println("published_tool 表中没有记录需要删除");
            }
        } catch (Exception e) {
            System.out.println("操作 published_tool 表时出现错误: " + e.getMessage());
        }
        
        // 检查并删除其他可能的工具相关表
        String[] toolTables = {"device_index", "tool", "tools"};
        for (String tableName : toolTables) {
            try {
                long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                if (count > 0) {
                    int rowsDeleted = jdbcTemplate.update("DELETE FROM " + tableName);
                    System.out.println("成功删除 " + rowsDeleted + " 条 " + tableName + " 表中的记录");
                } else {
                    System.out.println(tableName + " 表中没有记录需要删除");
                }
            } catch (Exception e) {
                // 表不存在或其他错误，忽略
            }
        }
        
        System.out.println("\n=== 清理完成 ===");
    }
}
