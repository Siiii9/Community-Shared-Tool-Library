package com.example.Community_Shared_Tool_java;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import com.example.Community_Shared_Tool_java.entity.BorrowRecord;
import com.example.Community_Shared_Tool_java.entity.Tool;
import com.example.Community_Shared_Tool_java.repository.BorrowInfoRepository;
import com.example.Community_Shared_Tool_java.repository.BorrowRecordRepository;
import com.example.Community_Shared_Tool_java.repository.ToolRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class ClearDatabaseTest {

    @Autowired
    private BorrowInfoRepository borrowInfoRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Test
    @Transactional
    public void clearAllBorrowRecordsAndTools() {
        System.out.println("=== 开始清理数据库 ===");
        
        // 1. 清理借用记录 (BorrowInfo)
        long borrowInfoCount = borrowInfoRepository.count();
        borrowInfoRepository.deleteAll();
        System.out.println("已删除 " + borrowInfoCount + " 条借用记录");
        
        // 2. 清理借用申请记录 (BorrowRecord)
        long borrowRecordCount = borrowRecordRepository.count();
        borrowRecordRepository.deleteAll();
        System.out.println("已删除 " + borrowRecordCount + " 条借用申请记录");
        
        // 3. 清理发布的工具 (Tool)
        long toolCount = toolRepository.count();
        toolRepository.deleteAll();
        System.out.println("已删除 " + toolCount + " 个发布的工具");
        
        System.out.println("=== 数据库清理完成 ===");
    }
}
