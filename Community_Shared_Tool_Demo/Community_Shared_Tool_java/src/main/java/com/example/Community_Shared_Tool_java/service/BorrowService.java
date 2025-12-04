package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import com.example.Community_Shared_Tool_java.entity.BorrowRecord;
import com.example.Community_Shared_Tool_java.entity.PublishedTool;
import com.example.Community_Shared_Tool_java.repository.BorrowInfoRepository;
import com.example.Community_Shared_Tool_java.repository.BorrowRecordRepository;
import com.example.Community_Shared_Tool_java.repository.PublishedToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private PublishedToolRepository publishedToolRepository;
    
    @Autowired
    private BorrowInfoRepository borrowInfoRepository;
    
    @Autowired
    private BorrowInfoService borrowInfoService;
    
    // 申请借用
    public BorrowRecord applyBorrow(Integer toolId, Integer borrowerId, Integer ownerId, 
                                   Integer borrowDays, String applyReason) {
        // 检查工具是否存在
        Optional<PublishedTool> toolOptional = publishedToolRepository.findById(toolId);
        if (toolOptional.isEmpty()) {
            throw new RuntimeException("工具不存在");
        }
        
        // 检查工具状态是否可用
        PublishedTool publishedTool = toolOptional.get();
        
        // 检查是否是自己借自己的工具
        if (borrowerId.equals(publishedTool.getOwnerId())) {
            throw new RuntimeException("不能借用自己发布的工具");
        }
        if (!"available".equals(publishedTool.getStatus())) {
            throw new RuntimeException("该工具当前不可用");
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
        
        // 保存借用记录
        BorrowRecord savedRecord = borrowRecordRepository.save(borrowRecord);
        
        // 修复：申请阶段不改变工具状态，保持available
        // publishedTool.setStatus("pending");
        // publishedToolRepository.save(publishedTool);
        
        return savedRecord;
    }
    
    // 通过toolId归还工具（方便前端调用）
    @Transactional
    public BorrowRecord returnToolByToolId(Integer toolId) {
        // 查找该工具当前正在进行的借用记录
        List<BorrowRecord> activeRecords = borrowRecordRepository.findActiveBorrowRecordsByToolId(toolId);
        if (activeRecords.isEmpty()) {
            throw new RuntimeException("该工具当前没有正在进行的借用记录");
        }
        
        // 使用第一条记录进行归还操作
        return returnTool(activeRecords.get(0).getId());
    }
    
    // 同意借用申请
    @Transactional
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
        
        // 更新借用记录
        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        
        // 查找对应的PublishedTool并更新状态
        Optional<PublishedTool> publishedToolOptional = publishedToolRepository.findById(record.getToolId());
        if (publishedToolOptional.isPresent()) {
            PublishedTool publishedTool = publishedToolOptional.get();
            publishedTool.setStatus("borrowed"); // 同意申请后直接将工具状态设为已借出
            publishedToolRepository.save(publishedTool);
            
            // 创建BorrowInfo记录
            BorrowInfo borrowInfo = new BorrowInfo();
            borrowInfo.setToolId(record.getToolId());
            borrowInfo.setBorrowerId(record.getBorrowerId());
            borrowInfo.setOwnerId(record.getOwnerId());
            borrowInfo.setExpectedReturnTime(record.getExpectedReturnTime());
            borrowInfo.setStatus("borrowing");
            borrowInfo.setToolName(publishedTool.getToolName());
            
            // 保存BorrowInfo记录
            borrowInfoRepository.save(borrowInfo);
        }
        
        return savedRecord;
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
    @Transactional
    public BorrowRecord confirmTake(Integer borrowRecordId) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        if ( !"APPROVED".equals(record.getStatus()) ) {
            throw new RuntimeException("该借用申请状态不允许取用操作");
        }
        
        record.setStatus("TAKEN");
        record.setTakeTime(new Timestamp(System.currentTimeMillis()));
        
        // 查找对应的PublishedTool
        Optional<PublishedTool> publishedToolOptional = publishedToolRepository.findById(record.getToolId());
        if (publishedToolOptional.isEmpty()) {
            throw new RuntimeException("未找到对应的发布工具记录");
        }
        
        PublishedTool publishedTool = publishedToolOptional.get();
        
        // 创建BorrowInfo记录
        BorrowInfo borrowInfo = new BorrowInfo();
        borrowInfo.setToolId(record.getToolId()); // 使用PublishedTool的ID
        borrowInfo.setBorrowerId(record.getBorrowerId());
        borrowInfo.setOwnerId(record.getOwnerId());
        borrowInfo.setExpectedReturnTime(record.getExpectedReturnTime());
        borrowInfo.setStatus("borrowing");
        borrowInfo.setToolName(publishedTool.getToolName());
        
        // 保存BorrowInfo记录
        borrowInfoRepository.save(borrowInfo);
        
        // 更新PublishedTool状态为borrowing
        publishedTool.setStatus("borrowed");
        publishedToolRepository.save(publishedTool);
        
        return borrowRecordRepository.save(record);
    }
    
    // 归还工具（用户申请归还）
    @Transactional
    public BorrowRecord returnTool(Integer borrowRecordId) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        // 修改状态检查，允许从APPROVED状态直接归还
        if (!"TAKEN".equals(record.getStatus()) && !"APPROVED".equals(record.getStatus())) {
            throw new RuntimeException("该借用记录状态不允许归还操作");
        }
        
        // 更新记录状态为等待归还确认
        record.setStatus("WAITING_RETURN_CONFIRM");
        record.setReturnTime(new Timestamp(System.currentTimeMillis()));
        
        // 更新借用记录
        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        
        // 更新BorrowInfo记录的状态为等待归还确认
        List<BorrowInfo> borrowInfos = borrowInfoRepository.findByToolIdAndBorrowerId(record.getToolId(), record.getBorrowerId());
        for (BorrowInfo borrowInfo : borrowInfos) {
            if ("borrowing".equals(borrowInfo.getStatus())) {
                borrowInfo.setStatus("waiting_return_confirm");
                borrowInfo.setActualReturnTime(new Timestamp(System.currentTimeMillis()));
                borrowInfoRepository.save(borrowInfo);
                break;
            }
        }
        
        return savedRecord;
    }
    
    // 确认归还工具（所有者确认）
    @Transactional
    public BorrowRecord confirmReturn(Integer borrowRecordId) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        // 检查状态是否为等待归还确认
        if (!"WAITING_RETURN_CONFIRM".equals(record.getStatus())) {
            throw new RuntimeException("该借用记录状态不允许确认归还操作");
        }
        
        // 更新记录状态为已归还
        record.setStatus("RETURNED");
        
        // 更新借用记录
        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        
        // 更新工具状态为可用
        Optional<PublishedTool> publishedToolOptional = publishedToolRepository.findById(record.getToolId());
        if (publishedToolOptional.isPresent()) {
            PublishedTool publishedTool = publishedToolOptional.get();
            publishedTool.setStatus("available");
            publishedToolRepository.save(publishedTool);
        }
        
        // 更新BorrowInfo记录的状态为已归还
        List<BorrowInfo> borrowInfos = borrowInfoRepository.findByToolIdAndBorrowerId(record.getToolId(), record.getBorrowerId());
        for (BorrowInfo borrowInfo : borrowInfos) {
            if ("waiting_return_confirm".equals(borrowInfo.getStatus())) {
                borrowInfo.setStatus("returned");
                borrowInfoRepository.save(borrowInfo);
                break;
            }
        }
        
        return savedRecord;
    }
    
    // 拒绝归还工具（所有者拒绝）
    @Transactional
    public BorrowRecord rejectReturn(Integer borrowRecordId) {
        Optional<BorrowRecord> recordOptional = borrowRecordRepository.findById(borrowRecordId);
        if (recordOptional.isEmpty()) {
            throw new RuntimeException("借用记录不存在");
        }
        
        BorrowRecord record = recordOptional.get();
        // 检查状态是否为等待归还确认
        if (!"WAITING_RETURN_CONFIRM".equals(record.getStatus())) {
            throw new RuntimeException("该借用记录状态不允许拒绝归还操作");
        }
        
        // 更新记录状态为已领取
        record.setStatus("TAKEN");
        
        // 更新借用记录
        BorrowRecord savedRecord = borrowRecordRepository.save(record);
        
        // 更新BorrowInfo记录的状态为借用中
        List<BorrowInfo> borrowInfos = borrowInfoRepository.findByToolIdAndBorrowerId(record.getToolId(), record.getBorrowerId());
        for (BorrowInfo borrowInfo : borrowInfos) {
            if ("waiting_return_confirm".equals(borrowInfo.getStatus())) {
                borrowInfo.setStatus("borrowing");
                borrowInfoRepository.save(borrowInfo);
                break;
            }
        }
        
        return savedRecord;
    }
    
    // 获取用户的借用记录
    public List<BorrowRecord> getBorrowRecordsByBorrowerId(Integer borrowerId) {
        return borrowRecordRepository.findByBorrowerId(borrowerId);
    }
    
    // 获取用户收到的借用申请
    public List<BorrowRecord> getBorrowApplicationsByOwnerId(Integer ownerId) {
        // 返回所有需要处理的申请，包括等待批准和等待归还确认的记录
        List<BorrowRecord> pendingRecords = borrowRecordRepository.findByOwnerIdAndStatus(ownerId, "PENDING");
        List<BorrowRecord> waitingReturnRecords = borrowRecordRepository.findByOwnerIdAndStatus(ownerId, "WAITING_RETURN_CONFIRM");
        
        // 合并结果
        pendingRecords.addAll(waitingReturnRecords);
        return pendingRecords;
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
