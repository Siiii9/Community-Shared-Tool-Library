// src/main/java/com/example/Community_Shared_Tool_java/service/CreditService.java
package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.CreditLog;
import com.example.Community_Shared_Tool_java.entity.UserInfo;
import com.example.Community_Shared_Tool_java.repository.CreditLogRepository;
import com.example.Community_Shared_Tool_java.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CreditService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private CreditLogRepository creditLogRepository;

    // 扣信用分
    @Transactional
    public void deductCreditScore(Integer userId, int penalty, String reason) {
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        int newScore = Math.max(0, user.getCreditScore() - penalty); // 最低0分
        user.setCreditScore(newScore);
        userInfoRepository.save(user);

        // 记录日志
        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeScore(-penalty);
        log.setReason(reason);
        creditLogRepository.save(log);
    }

    // 加信用分
    @Transactional
    public void addCreditScore(Integer userId, int bonus, String reason) {
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        int newScore = Math.min(100, user.getCreditScore() + bonus); // 最高100分
        user.setCreditScore(newScore);
        userInfoRepository.save(user);

        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeScore(bonus);
        log.setReason(reason);
        creditLogRepository.save(log);
    }

    // 计算当前押金金额（基于信用分）
    public BigDecimal calculateDeposit(Integer creditScore) {
        BigDecimal baseDeposit = new BigDecimal("200"); // 基础押金200元

        if (creditScore >= 90) {
            return baseDeposit.multiply(new BigDecimal("0.5")); // 50%
        } else if (creditScore >= 80) {
            return baseDeposit.multiply(new BigDecimal("0.7")); // 70%
        } else if (creditScore >= 70) {
            return baseDeposit; // 100%
        } else {
            return baseDeposit.multiply(new BigDecimal("1.5")); // 150%
        }
    }

    // 获取用户信用信息
    public CreditInfo getCreditInfo(Integer userId) {
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        CreditInfo info = new CreditInfo();
        info.setUserId(userId);
        info.setCreditScore(user.getCreditScore());
        info.setDepositAmount(calculateDeposit(user.getCreditScore()));
        info.setIsDepositPaid(user.getIsDepositPaid());
        return info;
    }

    // 获取信用日志
    public List<CreditLog> getCreditLogs(Integer userId) {
        return creditLogRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    // 模拟逾期一天（演示用）
    public void simulateOverdueOneDay(Integer userId) {
        // 扣5分（模拟逾期1天）
        deductCreditScore(userId, 5, "模拟逾期1天");
    }

    // 内部类：返回给前端的信用信息
    public static class CreditInfo {
        private Integer userId;
        private Integer creditScore;
        private java.math.BigDecimal depositAmount;
        private Boolean isDepositPaid;

        // Getters and Setters
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public Integer getCreditScore() { return creditScore; }
        public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
        public java.math.BigDecimal getDepositAmount() { return depositAmount; }
        public void setDepositAmount(java.math.BigDecimal depositAmount) { this.depositAmount = depositAmount; }
        public Boolean getIsDepositPaid() { return isDepositPaid; }
        public void setIsDepositPaid(Boolean isDepositPaid) { this.isDepositPaid = isDepositPaid; }
    }
}