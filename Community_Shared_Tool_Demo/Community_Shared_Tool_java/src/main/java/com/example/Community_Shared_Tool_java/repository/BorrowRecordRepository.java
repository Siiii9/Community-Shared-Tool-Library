package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Integer> {
    
    // 根据借用人ID查询借用记录
    List<BorrowRecord> findByBorrowerId(Integer borrowerId);
    
    // 根据物品所有者ID查询借用记录
    List<BorrowRecord> findByOwnerId(Integer ownerId);
    
    // 根据工具ID查询借用记录
    List<BorrowRecord> findByToolId(Integer toolId);
    
    // 根据状态查询借用记录
    List<BorrowRecord> findByStatus(String status);
    
    // 根据借用人ID和状态查询
    List<BorrowRecord> findByBorrowerIdAndStatus(Integer borrowerId, String status);
    
    // 根据所有者ID和状态查询
    List<BorrowRecord> findByOwnerIdAndStatus(Integer ownerId, String status);
    
    // 查询特定工具当前正在借用的记录
    @Query("SELECT br FROM BorrowRecord br WHERE br.toolId = :toolId AND br.status IN ('APPROVED', 'TAKEN')")
    List<BorrowRecord> findActiveBorrowRecordsByToolId(@Param("toolId") Integer toolId);
}
