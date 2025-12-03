package com.example.Community_Shared_Tool_java;

import com.example.Community_Shared_Tool_java.entity.Tool;
import com.example.Community_Shared_Tool_java.repository.ToolRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class CheckToolTableTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ToolRepository toolRepository;

    @Test
    public void checkToolTable() {
        System.out.println("=== 检查数据库表结构 ===");
        
        // 获取所有表名
        List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES");
        System.out.println("数据库中的所有表：");
        for (Map<String, Object> table : tables) {
            System.out.println("- " + table.values().iterator().next());
        }
        
        // 检查Tool实体对应的表结构
        System.out.println("\n=== 检查Tool实体对应的表结构 ===");
        
        // 先尝试从ToolRepository获取数据
        long toolCount = toolRepository.count();
        System.out.println("ToolRepository 统计的工具数量: " + toolCount);
        
        // 尝试直接查询tool相关的表
        List<String> toolTables = List.of("tool", "tools", "device", "devices");
        for (String tableName : toolTables) {
            try {
                long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                System.out.println(tableName + " 表中的记录数量: " + count);
                
                // 如果表存在，显示前几行数据
                if (count > 0) {
                    List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName + " LIMIT 3");
                    System.out.println(tableName + " 表的前3行数据:");
                    for (Map<String, Object> row : rows) {
                        System.out.println("  " + row);
                    }
                }
            } catch (Exception e) {
                // 表不存在，忽略异常
            }
        }
        
        System.out.println("\n=== 检查完成 ===");
    }
}
