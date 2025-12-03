package com.example.Community_Shared_Tool_java;

import com.example.Community_Shared_Tool_java.service.BorrowInfoService;
import com.example.Community_Shared_Tool_java.repository.BorrowInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class ReturnTest {

    @Autowired
    private BorrowInfoService borrowInfoService;
    
    @Autowired
    private BorrowInfoRepository borrowInfoRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void testReturnTool() {
        try {
            System.out.println("=== 测试归还工具功能 ===");
            
            // 先检查数据库中有哪些BorrowInfo记录
            System.out.println("\n1. 查询所有BorrowInfo记录：");
            var allBorrowInfos = borrowInfoRepository.findAll();
            System.out.println("总共有 " + allBorrowInfos.size() + " 条BorrowInfo记录");
            
            for (var info : allBorrowInfos) {
                System.out.println("ID: " + info.getId() + ", 状态: " + info.getStatus() + ", 工具ID: " + info.getToolId() + ", 借用者ID: " + info.getBorrowerId());
            }
            
            // 如果有记录，尝试归还第一条
            if (!allBorrowInfos.isEmpty()) {
                Integer borrowInfoId = allBorrowInfos.get(0).getId();
                System.out.println("\n2. 尝试归还工具，BorrowInfo ID: " + borrowInfoId);
                
                // 调用归还方法
                var result = borrowInfoService.returnTool(borrowInfoId);
                
                System.out.println("3. 归还成功！");
                System.out.println("   更新后的状态: " + result.getStatus());
                System.out.println("   实际归还时间: " + result.getActualReturnTime());
                
            } else {
                System.out.println("\n没有找到BorrowInfo记录，无法测试归还功能");
            }
            
        } catch (Exception e) {
            System.out.println("\n4. 归还失败: " + e.getMessage());
            System.out.println("异常堆栈信息：");
            e.printStackTrace();
        }
    }
}
