package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import com.example.Community_Shared_Tool_java.service.BorrowInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/borrow-infos")
public class BorrowInfoController {
    
    @Autowired
    private BorrowInfoService borrowInfoService;
    
    // 创建借用记录
    @PostMapping
    public ResponseEntity<BorrowInfo> createBorrowInfo(@RequestBody BorrowInfo borrowInfo) {
        BorrowInfo createdBorrowInfo = borrowInfoService.createBorrowInfo(borrowInfo);
        return new ResponseEntity<>(createdBorrowInfo, HttpStatus.CREATED);
    }
    
    // 获取用户的借用记录
    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<Map<String, Object>> getBorrowInfosByBorrowerId(@PathVariable Integer borrowerId) {
        try {
            List<BorrowInfo> borrowInfos = borrowInfoService.getBorrowInfosByBorrowerId(borrowerId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", borrowInfos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据ID获取借用记录
    @GetMapping("/{id}")
    public ResponseEntity<BorrowInfo> getBorrowInfoById(@PathVariable Integer id) {
        Optional<BorrowInfo> borrowInfo = borrowInfoService.getBorrowInfoById(id);
        return borrowInfo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    // 归还工具
    @PatchMapping("/{id}/return")
    public ResponseEntity<Map<String, Object>> returnTool(@PathVariable Integer id) {
        try {
            BorrowInfo updatedBorrowInfo = borrowInfoService.returnTool(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBorrowInfo);
            response.put("message", "工具归还成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 更新借用记录
    @PutMapping("/{id}")
    public ResponseEntity<BorrowInfo> updateBorrowInfo(@PathVariable Integer id, @RequestBody BorrowInfo borrowInfo) {
        // 确保ID一致
        borrowInfo.setId(id);
        BorrowInfo updatedBorrowInfo = borrowInfoService.updateBorrowInfo(borrowInfo);
        return ResponseEntity.ok(updatedBorrowInfo);
    }
    
    // 搜索借用记录
    @GetMapping("/search")
    public ResponseEntity<List<BorrowInfo>> searchBorrowInfos(
            @RequestParam(required = false) Integer borrowerId,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String status) {
        List<BorrowInfo> borrowInfos = borrowInfoService.searchBorrowInfos(borrowerId, toolName, status);
        return ResponseEntity.ok(borrowInfos);
    }
    
    // 删除借用记录
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBorrowInfo(@PathVariable Integer id) {
        try {
            borrowInfoService.deleteBorrowInfo(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "借用记录删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 确认归还工具（所有者确认）
    @PatchMapping("/{id}/confirm-return")
    public ResponseEntity<Map<String, Object>> confirmReturn(@PathVariable Integer id) {
        try {
            BorrowInfo updatedBorrowInfo = borrowInfoService.confirmReturn(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBorrowInfo);
            response.put("message", "工具归还确认成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 拒绝归还工具（所有者拒绝）
    @PatchMapping("/{id}/reject-return")
    public ResponseEntity<Map<String, Object>> rejectReturn(@PathVariable Integer id) {
        try {
            BorrowInfo updatedBorrowInfo = borrowInfoService.rejectReturn(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBorrowInfo);
            response.put("message", "工具归还拒绝成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
