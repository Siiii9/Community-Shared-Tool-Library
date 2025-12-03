package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.entity.BorrowInfo;
import com.example.Community_Shared_Tool_java.service.BorrowInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<List<BorrowInfo>> getBorrowInfosByBorrowerId(@PathVariable Integer borrowerId) {
        List<BorrowInfo> borrowInfos = borrowInfoService.getBorrowInfosByBorrowerId(borrowerId);
        return ResponseEntity.ok(borrowInfos);
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
    public ResponseEntity<BorrowInfo> returnTool(@PathVariable Integer id) {
        BorrowInfo updatedBorrowInfo = borrowInfoService.returnTool(id);
        return ResponseEntity.ok(updatedBorrowInfo);
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
}
