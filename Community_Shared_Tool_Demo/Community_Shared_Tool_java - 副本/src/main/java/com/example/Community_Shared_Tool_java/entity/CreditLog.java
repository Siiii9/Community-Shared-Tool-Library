// src/main/java/com/example/Community_Shared_Tool_java/entity/CreditLog.java
package com.example.Community_Shared_Tool_java.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "credit_log")
public class CreditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "change_score")
    private Integer changeScore; // 正为加分，负为扣分

    @Column(name = "reason")
    private String reason;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();
}