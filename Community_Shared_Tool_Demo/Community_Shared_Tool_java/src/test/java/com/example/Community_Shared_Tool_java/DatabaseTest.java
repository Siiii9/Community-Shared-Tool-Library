package com.example.Community_Shared_Tool_java;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import com.example.Community_Shared_Tool_java.repository.BorrowInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DatabaseTest {

    @Autowired
    private BorrowInfoRepository borrowInfoRepository;

    @Test
    public void testBorrowInfoData() {
        System.out.println("=== 检查BorrowInfo数据 ===");
        
        List<BorrowInfo> borrowInfos = borrowInfoRepository.findAll();
        System.out.println("BorrowInfo表中共有 " + borrowInfos.size() + " 条记录");
        
        for (BorrowInfo borrowInfo : borrowInfos) {
            System.out.println("ID: " + borrowInfo.getId());
            System.out.println("  工具ID: " + borrowInfo.getToolId());
            System.out.println("  工具名称: " + borrowInfo.getToolName());
            System.out.println("  工具类型: " + borrowInfo.getToolType());
            System.out.println("  借用者ID: " + borrowInfo.getBorrowerId());
            System.out.println("  所有者ID: " + borrowInfo.getOwnerId());
            System.out.println("  借用时间: " + borrowInfo.getBorrowTime());
            System.out.println("  预计归还时间: " + borrowInfo.getExpectedReturnTime());
            System.out.println("  实际归还时间: " + borrowInfo.getActualReturnTime());
            System.out.println("  状态: " + borrowInfo.getStatus());
            System.out.println("  BorrowRecord ID: " + borrowInfo.getBorrowRecordId());
            System.out.println("------------------------");
        }
        
        System.out.println("=== 测试结束 ===");
    }
}
