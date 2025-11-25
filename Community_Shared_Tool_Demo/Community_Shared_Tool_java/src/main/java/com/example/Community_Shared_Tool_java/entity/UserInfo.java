package com.example.Community_Shared_Tool_java.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_info")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "password")
    private String password;

    @Column(name = "username")
    private String username;

    @Column(name = "user_basicinfo")
    private String userBasicinfo;  // 小写 i，这里是关键

    // 添加新的信用分字段
    @Column(name = "credit_score")
    private Integer creditScore = 100; // 默认100分

    @Column(name = "deposit_amount")
    private java.math.BigDecimal depositAmount = java.math.BigDecimal.ZERO;

    @Column(name = "is_deposit_paid")
    private Boolean isDepositPaid = false;
}