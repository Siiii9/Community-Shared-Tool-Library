package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import com.example.Community_Shared_Tool_java.entity.BorrowRecord;
import com.example.Community_Shared_Tool_java.entity.PublishedTool;
import com.example.Community_Shared_Tool_java.repository.BorrowInfoRepository;
import com.example.Community_Shared_Tool_java.repository.BorrowRecordRepository;
import com.example.Community_Shared_Tool_java.repository.PublishedToolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(BorrowInfoService.class);
    
    @Autowired
    private BorrowInfoRepository borrowInfoRepository;
    
    @Autowired
    private PublishedToolRepository publishedToolRepository;
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    // 创建借用记录
    @Transactional
    public BorrowInfo createBorrowInfo(BorrowInfo borrowInfo) {
        // 检查工具是否存在
        PublishedTool tool = publishedToolRepository.findById(borrowInfo.getToolId())
                .orElseThrow(() -> new RuntimeException("工具不存在"));
        
        // 检查是否是自己借自己的工具
        if (borrowInfo.getBorrowerId().equals(tool.getOwnerId())) {
            throw new RuntimeException("不能借用自己发布的工具");
        }
        
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
    
    // 根据工具ID和借用者ID获取借用记录
    public List<BorrowInfo> getBorrowInfoByToolIdAndBorrowerId(Integer toolId, Integer borrowerId) {
        return borrowInfoRepository.findByToolIdAndBorrowerId(toolId, borrowerId);
    }
    
    // 归还工具（用户申请归还）
    @Transactional
    public BorrowInfo returnTool(Integer id) {
        logger.info("开始归还工具申请，BorrowInfo ID: {}", id);
        
        // 查找借用记录
        Optional<BorrowInfo> borrowInfoOpt = borrowInfoRepository.findById(id);
        logger.info("查找BorrowInfo结果: {}", borrowInfoOpt.isPresent());
        
        BorrowInfo borrowInfo = borrowInfoOpt.orElseThrow(() -> {
            logger.error("借用记录不存在，ID: {}", id);
            return new RuntimeException("借用记录不存在");
        });
        
        logger.info("当前BorrowInfo状态: {}, 工具ID: {}, 借用者ID: {}", 
                borrowInfo.getStatus(), borrowInfo.getToolId(), borrowInfo.getBorrowerId());
        
        // 检查是否已经归还或正在等待确认
        if ("returned".equals(borrowInfo.getStatus())) {
            logger.warn("工具已经归还，BorrowInfo ID: {}", id);
            throw new RuntimeException("工具已经归还");
        }
        if ("waiting_return_confirm".equals(borrowInfo.getStatus())) {
            logger.warn("工具正在等待归还确认，BorrowInfo ID: {}", id);
            throw new RuntimeException("工具正在等待归还确认，请耐心等待所有者确认");
        }
        
        // 更新借用状态为等待归还确认
        borrowInfo.setStatus("waiting_return_confirm");
        borrowInfo.setActualReturnTime(new Timestamp(System.currentTimeMillis()));
        logger.info("更新BorrowInfo状态为waiting_return_confirm，设置实际归还时间");
        
        // 同时更新对应的BorrowRecord状态为等待归还确认
        boolean recordUpdated = false;
        
        // 优先使用borrowRecordId直接查找
        if (borrowInfo.getBorrowRecordId() != null) {
            Optional<BorrowRecord> borrowRecordOpt = borrowRecordRepository.findById(borrowInfo.getBorrowRecordId());
            if (borrowRecordOpt.isPresent()) {
                BorrowRecord record = borrowRecordOpt.get();
                logger.info("通过borrowRecordId找到记录: ID: {}, 状态: {}", record.getId(), record.getStatus());
                if ("APPROVED".equals(record.getStatus()) || "TAKEN".equals(record.getStatus())) {
                    record.setStatus("WAITING_RETURN_CONFIRM");
                    record.setReturnTime(new Timestamp(System.currentTimeMillis()));
                    borrowRecordRepository.save(record);
                    logger.info("更新BorrowRecord ID: {} 状态为WAITING_RETURN_CONFIRM", record.getId());
                    recordUpdated = true;
                }
            } else {
                logger.warn("通过borrowRecordId未找到记录: {}", borrowInfo.getBorrowRecordId());
            }
        }
        
        // 如果没有通过borrowRecordId找到，使用旧方法查找
        if (!recordUpdated) {
            List<BorrowRecord> borrowRecords = borrowRecordRepository.findByToolIdAndBorrowerId(borrowInfo.getToolId(), borrowInfo.getBorrowerId());
            logger.info("找到BorrowRecord数量: {}", borrowRecords.size());
            
            for (BorrowRecord record : borrowRecords) {
                logger.info("BorrowRecord ID: {}, 状态: {}", record.getId(), record.getStatus());
                // 找到状态为APPROVED或TAKEN的记录并更新
                if ("APPROVED".equals(record.getStatus()) || "TAKEN".equals(record.getStatus())) {
                    record.setStatus("WAITING_RETURN_CONFIRM");
                    record.setReturnTime(new Timestamp(System.currentTimeMillis()));
                    borrowRecordRepository.save(record);
                    logger.info("更新BorrowRecord ID: {} 状态为WAITING_RETURN_CONFIRM", record.getId());
                    
                    // 设置borrowRecordId以便后续使用
                    borrowInfo.setBorrowRecordId(record.getId());
                    logger.info("设置BorrowInfo的borrowRecordId为: {}", record.getId());
                    
                    recordUpdated = true;
                    break;
                }
            }
        }
        
        if (!recordUpdated) {
            logger.warn("未找到需要更新的BorrowRecord记录，工具ID: {}, 借用者ID: {}", borrowInfo.getToolId(), borrowInfo.getBorrowerId());
        }
        
        BorrowInfo savedBorrowInfo = borrowInfoRepository.save(borrowInfo);
        logger.info("归还工具申请完成，BorrowInfo ID: {}", savedBorrowInfo.getId());
        
        return savedBorrowInfo;
    }
    
    // 确认归还工具（所有者确认）
    @Transactional
    public BorrowInfo confirmReturn(Integer id) {
        logger.info("开始确认归还工具，BorrowInfo ID: {}", id);
        
        // 查找借用记录
        Optional<BorrowInfo> borrowInfoOpt = borrowInfoRepository.findById(id);
        logger.info("查找BorrowInfo结果: {}", borrowInfoOpt.isPresent());
        
        BorrowInfo borrowInfo = borrowInfoOpt.orElseThrow(() -> {
            logger.error("借用记录不存在，ID: {}", id);
            return new RuntimeException("借用记录不存在");
        });
        
        logger.info("当前BorrowInfo状态: {}, 工具ID: {}, 借用者ID: {}", 
                borrowInfo.getStatus(), borrowInfo.getToolId(), borrowInfo.getBorrowerId());
        
        // 检查是否正在等待归还确认
        if (!"waiting_return_confirm".equals(borrowInfo.getStatus())) {
            logger.warn("工具未处于等待归还确认状态，BorrowInfo ID: {}", id);
            return borrowInfo;
        }
        
        // 更新借用状态为已归还
        borrowInfo.setStatus("returned");
        logger.info("更新BorrowInfo状态为returned");
        
        // 更新工具状态为可借用
        Optional<PublishedTool> toolOpt = publishedToolRepository.findById(borrowInfo.getToolId());
        logger.info("查找PublishedTool结果: {}", toolOpt.isPresent());
        
        if (toolOpt.isPresent()) {
            PublishedTool tool = toolOpt.get();
            logger.info("当前工具状态: {}", tool.getStatus());
            tool.setStatus("available");
            publishedToolRepository.save(tool);
            logger.info("更新工具状态为available");
        } else {
            logger.warn("工具不存在，ID: {}，跳过工具状态更新", borrowInfo.getToolId());
        }
        
        // 同时更新对应的BorrowRecord状态为已归还
        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByToolIdAndBorrowerId(borrowInfo.getToolId(), borrowInfo.getBorrowerId());
        logger.info("找到BorrowRecord数量: {}", borrowRecords.size());
        
        boolean recordUpdated = false;
        for (BorrowRecord record : borrowRecords) {
            logger.info("BorrowRecord ID: {}, 状态: {}", record.getId(), record.getStatus());
            // 找到状态为WAITING_RETURN_CONFIRM的记录并更新
            if ("WAITING_RETURN_CONFIRM".equals(record.getStatus())) {
                record.setStatus("RETURNED");
                borrowRecordRepository.save(record);
                logger.info("更新BorrowRecord ID: {} 状态为RETURNED", record.getId());
                recordUpdated = true;
                break;
            }
        }
        
        if (!recordUpdated) {
            logger.warn("未找到需要更新的BorrowRecord记录，工具ID: {}, 借用者ID: {}", borrowInfo.getToolId(), borrowInfo.getBorrowerId());
        }
        
        BorrowInfo savedBorrowInfo = borrowInfoRepository.save(borrowInfo);
        logger.info("确认归还工具完成，BorrowInfo ID: {}", savedBorrowInfo.getId());
        
        return savedBorrowInfo;
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

    // 删除借用记录
    @Transactional
    public void deleteBorrowInfo(Integer id) {
        logger.info("开始删除借用记录，BorrowInfo ID: {}", id);
        
        // 查找借用记录
        Optional<BorrowInfo> borrowInfoOpt = borrowInfoRepository.findById(id);
        logger.info("查找BorrowInfo结果: {}", borrowInfoOpt.isPresent());
        
        if (borrowInfoOpt.isPresent()) {
            BorrowInfo borrowInfo = borrowInfoOpt.get();
            
            // 删除借用记录
            borrowInfoRepository.deleteById(id);
            logger.info("删除BorrowInfo成功，ID: {}", id);
            
            // 同时删除对应的BorrowRecord记录
            List<BorrowRecord> borrowRecords = borrowRecordRepository.findByToolIdAndBorrowerId(borrowInfo.getToolId(), borrowInfo.getBorrowerId());
            logger.info("找到BorrowRecord数量: {}", borrowRecords.size());
            
            for (BorrowRecord record : borrowRecords) {
                logger.info("删除BorrowRecord ID: {}", record.getId());
                borrowRecordRepository.deleteById(record.getId());
            }
        } else {
            logger.error("借用记录不存在，无法删除，ID: {}", id);
            throw new RuntimeException("借用记录不存在");
        }
    }
}