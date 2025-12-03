package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import com.example.Community_Shared_Tool_java.entity.PublishedTool;
import com.example.Community_Shared_Tool_java.repository.BorrowInfoRepository;
import com.example.Community_Shared_Tool_java.repository.PublishedToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BorrowInfoService {
    
    @Autowired
    private BorrowInfoRepository borrowInfoRepository;
    
    @Autowired
    private PublishedToolRepository publishedToolRepository;
    
    // 创建借用记录
    @Transactional
    public BorrowInfo createBorrowInfo(BorrowInfo borrowInfo) {
        // 检查工具是否存在
        PublishedTool tool = publishedToolRepository.findById(borrowInfo.getToolId())
                .orElseThrow(() -> new RuntimeException("工具不存在"));
        
        // 更新工具状态为已借出
        tool.setStatus("borrowed");
        publishedToolRepository.save(tool);
        
        // 设置借用信息
        borrowInfo.setStatus("borrowing");
        borrowInfo.setToolName(tool.getToolName());
        borrowInfo.setToolType(tool.getToolType());
        
        return borrowInfoRepository.save(borrowInfo);
    }
    
    // 获取用户的借用记录
    public List<BorrowInfo> getBorrowInfosByBorrowerId(Integer borrowerId) {
        return borrowInfoRepository.findByBorrowerId(borrowerId);
    }
    
    // 根据ID获取借用记录
    public Optional<BorrowInfo> getBorrowInfoById(Integer id) {
        return borrowInfoRepository.findById(id);
    }
    
    // 归还工具
    @Transactional
    public BorrowInfo returnTool(Integer id) {
        BorrowInfo borrowInfo = borrowInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借用记录不存在"));
        
        // 更新借用状态
        borrowInfo.setStatus("returned");
        borrowInfo.setActualReturnTime(new Timestamp(System.currentTimeMillis()));
        
        // 更新工具状态为可借用
        PublishedTool tool = publishedToolRepository.findById(borrowInfo.getToolId())
                .orElseThrow(() -> new RuntimeException("工具不存在"));
        tool.setStatus("available");
        publishedToolRepository.save(tool);
        
        return borrowInfoRepository.save(borrowInfo);
    }
    
    // 更新借用记录
    @Transactional
    public BorrowInfo updateBorrowInfo(BorrowInfo borrowInfo) {
        return borrowInfoRepository.save(borrowInfo);
    }
    
    // 检查并更新逾期状态
    @Transactional
    public void checkAndUpdateOverdueStatus() {
        List<BorrowInfo> borrowInfos = borrowInfoRepository.findByStatus("borrowing");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        for (BorrowInfo borrowInfo : borrowInfos) {
            if (borrowInfo.getExpectedReturnTime().before(now)) {
                borrowInfo.setStatus("overdue");
                borrowInfoRepository.save(borrowInfo);
            }
        }
    }
    
    // 根据条件搜索借用记录
    public List<BorrowInfo> searchBorrowInfos(Integer borrowerId, String toolName, String status) {
        if (borrowerId != null && toolName != null && !toolName.isEmpty() && status != null && !status.isEmpty()) {
            return borrowInfoRepository.findByBorrowerIdAndToolNameContaining(borrowerId, toolName)
                    .stream()
                    .filter(info -> info.getStatus().equals(status))
                    .collect(Collectors.toList());
        } else if (borrowerId != null && toolName != null && !toolName.isEmpty()) {
            return borrowInfoRepository.findByBorrowerIdAndToolNameContaining(borrowerId, toolName);
        } else if (borrowerId != null && status != null && !status.isEmpty()) {
            return borrowInfoRepository.findByBorrowerIdAndStatus(borrowerId, status);
        } else if (borrowerId != null) {
            return borrowInfoRepository.findByBorrowerId(borrowerId);
        } else {
            return borrowInfoRepository.findAll();
        }
    }
}