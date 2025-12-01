package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.BorrowRecord;
import com.example.Community_Shared_Tool_java.entity.Tool;
import com.example.Community_Shared_Tool_java.repository.BorrowRecordRepository;
import com.example.Community_Shared_Tool_java.repository.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private ToolRepository toolRepository;
    
    // 申请借用
    public BorrowRecord applyBorrow(Integer toolId, Integer borrowerId, Integer ownerId, 
                                   Integer borrowDays, String applyReason) {
        // 检查工具是否存在
        Optional<Tool> toolOptional = toolRepository.findById(toolId);
        if (toolOptional.isEmpty()) {
            throw new RuntimeException("工具不存在");
        }
        
        // 检查是否已经有正在进行的借用记录
        List<BorrowRecord> activeRecords = borrowRecordRepository.findActiveBorrowRecordsByToolId(toolId);
        if (!activeRecords.isEmpty()) {
            throw new RuntimeException("该工具当前正在被借用");
        }
        
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setToolId(toolId);
        borrowRecord.setBorrowerId(borrowerId);
        borrowRecord.setOwnerId(ownerId);
        borrowRecord.setStatus("PENDING");
        borrowRecord.setBorrowDays(borrowDays);
        borrowRecord.setApplyReason(applyReason);
        
        // 计算预计归还时间
        Timestamp expectedReturnTime = new Timestamp(System.currentTimeMillis() + borrowDays * 24 * 60 * 60 * 1000L);
        borrowRecord.setExpectedReturnTime(expectedReturnTime);
        
        return borrowRecordRepository.save(borrowRecord);
    }
    
    // 同意借用申请
    public BorrowRecord approveBorrow(Integer borrowRecordId) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("该借用申请状态不允许同意操作");
        }
        
        record.setStatus("APPROVED");
        record.setApproveTime(new Timestamp(System.currentTimeMillis()));
        
        return borrowRecordRepository.save(record);
    }
    
    // 拒绝借用申请
    public BorrowRecord rejectBorrow(Integer borrowRecordId, String rejectReason) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        if (!"PENDING".equals(record.getStatus())) {
            throw new RuntimeException("该借用申请状态不允许拒绝操作");
        }
        
        record.setStatus("REJECTED");
        record.setRejectReason(rejectReason);
        
        return borrowRecordRepository.save(record);
    }
    
    // 确认取用
    public BorrowRecord confirmTake(Integer borrowRecordId) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        if (!"APPROVED".equals(record.getStatus())) {
            throw new RuntimeException("该借用申请状态不允许取用操作");
        }
        
        record.setStatus("TAKEN");
        record.setTakeTime(new Timestamp(System.currentTimeMillis()));
        
        return borrowRecordRepository.save(record);
    }
    
    // 归还工具
    public BorrowRecord returnTool(Integer borrowRecordId) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        if (!"TAKEN".equals(record.getStatus())) {
            throw new RuntimeException("该借用记录状态不允许归还操作");
        }
        
        record.setStatus("RETURNED");
        record.setReturnTime(new Timestamp(System.currentTimeMillis()));
        
        return borrowRecordRepository.save(record);
    }
    
    // 获取用户的借用记录
    public List<BorrowRecord> getBorrowRecordsByBorrowerId(Integer borrowerId) {
        return borrowRecordRepository.findByBorrowerId(borrowerId);
    }
    
    // 获取用户收到的借用申请
    public List<BorrowRecord> getBorrowApplicationsByOwnerId(Integer ownerId) {
        return borrowRecordRepository.findByOwnerIdAndStatus(ownerId, "PENDING");
    }
    
    // 获取用户已同意的借用记录
    public List<BorrowRecord> getApprovedBorrowRecordsByOwnerId(Integer ownerId) {
        return borrowRecordRepository.findByOwnerIdAndStatus(ownerId, "APPROVED");
    }
    
    // 获取用户正在借用的记录
    public List<BorrowRecord> getBorrowingRecordsByBorrowerId(Integer borrowerId) {
        return borrowRecordRepository.findByBorrowerIdAndStatus(borrowerId, "TAKEN");
    }
}
