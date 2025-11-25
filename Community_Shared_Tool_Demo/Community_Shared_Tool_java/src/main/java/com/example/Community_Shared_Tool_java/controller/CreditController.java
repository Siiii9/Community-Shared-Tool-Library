// src/main/java/com/example/Community_Shared_Tool_java/controller/CreditController.java
package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.entity.CreditLog;
import com.example.Community_Shared_Tool_java.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;

    // 获取信用信息
    @GetMapping("/info")
    public CreditService.CreditInfo getCreditInfo(@RequestParam Integer userId) {
        return creditService.getCreditInfo(userId);
    }

    // 获取信用日志
    @GetMapping("/logs")
    public List<CreditLog> getCreditLogs(@RequestParam Integer userId) {
        return creditService.getCreditLogs(userId);
    }

    // 模拟逾期一天（演示用）
    @PostMapping("/simulate-overdue")
    public void simulateOverdueOneDay(@RequestParam Integer userId) {
        creditService.simulateOverdueOneDay(userId);
    }

    // 缴纳押金（演示：只改状态）
    @PostMapping("/pay-deposit")
    public void payDeposit(@RequestParam Integer userId) {
        // 实际项目：对接支付
        // 演示：直接设置为已缴纳
        var user = creditService.getCreditInfo(userId);
        // 这里简化：实际应调用 UserService 更新
        // 为简化，我们假设前端只关心状态，后端不持久化押金金额
    }

    @PostMapping("/restore-credit")
    public void restoreCredit(@RequestParam Integer userId) {
        creditService.addCreditScore(userId, 5, "演示：手动恢复信用");
    }
}