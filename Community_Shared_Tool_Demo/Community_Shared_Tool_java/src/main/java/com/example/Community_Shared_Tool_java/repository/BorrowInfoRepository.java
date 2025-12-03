package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowInfoRepository extends JpaRepository<BorrowInfo, Integer> {
    
    // 根据借用人ID查询借用信息
    List<BorrowInfo> findByBorrowerId(Integer borrowerId);
    
    // 根据工具ID查询借用信息
    List<BorrowInfo> findByToolId(Integer toolId);
    
    // 根据借用人ID和状态查询
    List<BorrowInfo> findByBorrowerIdAndStatus(Integer borrowerId, String status);
    
    // 根据借用人ID和工具名称模糊查询
    List<BorrowInfo> findByBorrowerIdAndToolNameContaining(Integer borrowerId, String toolName);
    
    // 根据工具ID和状态查询当前有效的借用信息
    List<BorrowInfo> findByToolIdAndStatus(Integer toolId, String status);
    
    // 根据状态查询借用信息
    List<BorrowInfo> findByStatus(String status);
}