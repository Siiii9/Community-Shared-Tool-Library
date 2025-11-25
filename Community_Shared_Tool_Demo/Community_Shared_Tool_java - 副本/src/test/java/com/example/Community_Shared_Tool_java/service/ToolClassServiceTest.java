package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.dto.ToolClassDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class ToolClassServiceTest {

    @Autowired
    private ToolClassService toolClassService;

    @Test
    public void testGetTreeDeviceClassesAndPrintFromDB() {
        // 获取树状结构数据
        List<ToolClassDto> treeData = toolClassService.getTreeDeviceClasses();

        // 验证数据不为空
        assertFalse(treeData.isEmpty(), "树状结构数据不应该为空");

        // 打印树状结构
        System.out.println("=== Device Class Tree Structure from Database ===");
        printTree(treeData, 0);
    }

    // 递归打印树状结构的方法
    private void printTree(List<ToolClassDto> nodes, int level) {
        for (ToolClassDto node : nodes) {
            // 缩进表示层级
            String indent = "  ".repeat(level);
            System.out.println(indent + "└─ " + node.getDeviceClassName() + " (ID: " + node.getDeviceClassId() + ")");
            // 递归打印子节点
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                printTree(node.getChildren(), level + 1);
            }
        }
    }
}