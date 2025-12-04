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
            // 检查并解析所有必需参数
            if (!request.containsKey("toolId") || request.get("toolId") == null) {
                throw new IllegalArgumentException("缺少必需参数: toolId");
            }
            if (!request.containsKey("borrowerId") || request.get("borrowerId") == null) {
                throw new IllegalArgumentException("缺少必需参数: borrowerId");
            }
            if (!request.containsKey("ownerId") || request.get("ownerId") == null) {
                throw new IllegalArgumentException("缺少必需参数: ownerId");
            }
            if (!request.containsKey("borrowDays") || request.get("borrowDays") == null) {
                throw new IllegalArgumentException("缺少必需参数: borrowDays");
            }
            if (!request.containsKey("applyReason") || request.get("applyReason") == null) {
                throw new IllegalArgumentException("缺少必需参数: applyReason");
            }
            
            Integer toolId = Integer.parseInt(request.get("toolId").toString());
            Integer borrowerId = Integer.parseInt(request.get("borrowerId").toString());
            Integer ownerId = Integer.parseInt(request.get("ownerId").toString());
            Integer borrowDays = Integer.parseInt(request.get("borrowDays").toString());
            String applyReason = request.get("applyReason").toString();
            
            // 验证参数有效性
            if (toolId <= 0) {
                throw new IllegalArgumentException("无效的工具ID");
            }
            if (borrowerId <= 0) {
                throw new IllegalArgumentException("无效的借用者ID");
            }
            if (ownerId <= 0) {
                throw new IllegalArgumentException("无效的所有者ID");
            }
            if (borrowDays <= 0) {
                throw new IllegalArgumentException("借用天数必须大于0");
            }
            if (applyReason.trim().isEmpty()) {
                throw new IllegalArgumentException("借用原因不能为空");
            }
            
            BorrowRecord record = borrowService.applyBorrow(toolId, borrowerId, ownerId, borrowDays, applyReason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "借用申请提交成功");
            response.put("data", record);
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (NumberFormatException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "参数格式错误: 请确保数字参数为有效整数");
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
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
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
            response.put("message", "工具归还申请已提交！等待物品所有者确认。");
            response.put("data", record);
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 通过toolId归还工具（方便前端调用）
    @PostMapping("/return")
    public ResponseEntity<Map<String, Object>> returnToolByToolId(@RequestBody Map<String, Object> request) {
        try {
            if (!request.containsKey("toolId") || request.get("toolId") == null) {
                throw new IllegalArgumentException("缺少必需参数: toolId");
            }
            
            Integer toolId = Integer.parseInt(request.get("toolId").toString());
            BorrowRecord record = borrowService.returnToolByToolId(toolId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工具归还申请已提交！等待物品所有者确认。");
            response.put("data", record);
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取借用申请失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取用户收到的待审批申请
     */
    @GetMapping("/pending-applications/{userId}")
    public ResponseEntity<Map<String, Object>> getPendingApplications(@PathVariable Integer userId) {
        try {
            List<BorrowRecord> applications = borrowService.getBorrowApplicationsByOwnerId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", applications);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取待审批申请失败：" + e.getMessage());
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
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
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
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 拒绝归还工具（所有者拒绝）
    @PostMapping("/reject-return/{borrowRecordId}")
    public ResponseEntity<Map<String, Object>> rejectReturn(@PathVariable Integer borrowRecordId) {
        try {
            BorrowRecord record = borrowService.rejectReturn(borrowRecordId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "工具归还拒绝成功");
            response.put("data", record);
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
