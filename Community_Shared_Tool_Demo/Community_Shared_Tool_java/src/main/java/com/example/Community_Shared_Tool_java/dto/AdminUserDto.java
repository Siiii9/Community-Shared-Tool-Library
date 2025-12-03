package com.example.Community_Shared_Tool_java.dto;

import lombok.Data;

@Data
public class AdminUserDto {
    private Integer userId;
    private String username;
    private String userBasicinfo;
    private Integer creditScore;
    private Boolean isDepositPaid;
    private Boolean isFrozen;
    private String status; // 用于前端显示
}