package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.entity.BorrowRecord;
import com.example.Community_Shared_Tool_java.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class BorrowController {
    
    @Autowired
    private BorrowService borrowService;
    
    // 申请借用
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applyBorrow(@RequestBody Map<String, Object> request) {
        try {
            Integer toolId = Integer.parseInt(request.get("toolId").toString());
            Integer borrowerId = Integer.parseInt(request.get("borrowerId").toString());
            Integer ownerId = Integer.parseInt(request.get("ownerId").toString());
            Integer borrowDays = Integer.parseInt(request.get("borrowDays").toString());
            String applyReason = request.get("applyReason").toString();
            
            BorrowRecord record = borrowService.applyBorrow(toolId, borrowerId, ownerId, borrowDays, applyReason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "借用申请提交成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 同意借用申请
    @PostMapping("/approve/{borrowRecordId}")
    public ResponseEntity<Map<String, Object>> approveBorrow(@PathVariable Integer borrowRecordId) {
        try {
            BorrowRecord record = borrowService.approveBorrow(borrowRecordId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "借用申请已同意");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 拒绝借用申请
    @PostMapping("/reject/{borrowRecordId}")
    public ResponseEntity<Map<String, Object>> rejectBorrow(@PathVariable Integer borrowRecordId, 
                                                           @RequestBody Map<String, Object> request) {
        try {
            String rejectReason = request.get("rejectReason").toString();
            BorrowRecord record = borrowService.rejectBorrow(borrowRecordId, rejectReason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "借用申请已拒绝");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 确认取用
    @PostMapping("/take/{borrowRecordId}")
    public ResponseEntity<Map<String, Object>> confirmTake(@PathVariable Integer borrowRecordId) {
        try {
            BorrowRecord record = borrowService.confirmTake(borrowRecordId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工具取用确认成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 归还工具
    @PostMapping("/return/{borrowRecordId}")
    public ResponseEntity<Map<String, Object>> returnTool(@PathVariable Integer borrowRecordId) {
        try {
            BorrowRecord record = borrowService.returnTool(borrowRecordId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工具归还成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户的借用记录
     */
    @GetMapping("/my-borrows/{userId}")
    public ResponseEntity<Map<String, Object>> getMyBorrows(@PathVariable Integer userId) {
        try {
            List<BorrowRecord> borrowRecords = borrowService.getBorrowRecordsByBorrowerId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", borrowRecords);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取借用记录失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户收到的借用申请
     */
    @GetMapping("/my-applications/{userId}")
    public ResponseEntity<Map<String, Object>> getMyApplications(@PathVariable Integer userId) {
        try {
            List<BorrowRecord> applications = borrowService.getBorrowApplicationsByOwnerId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", applications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取借用申请失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取用户正在借用的记录
    @GetMapping("/borrowing/{borrowerId}")
    public ResponseEntity<Map<String, Object>> getBorrowingRecords(@PathVariable Integer borrowerId) {
        try {
            List<BorrowRecord> records = borrowService.getBorrowingRecordsByBorrowerId(borrowerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", records);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 确认归还工具（所有者确认）
    @PostMapping("/confirm-return/{borrowRecordId}")
    public ResponseEntity<Map<String, Object>> confirmReturn(@PathVariable Integer borrowRecordId) {
        try {
            BorrowRecord record = borrowService.confirmReturn(borrowRecordId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工具归还确认成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
