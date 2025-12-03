package com.example.Community_Shared_Tool_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "borrow_info")
public class BorrowInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "tool_id", nullable = false)
    private Integer toolId;
    
    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;
    
    @Column(name = "tool_type", nullable = false, length = 50)
    private String toolType;
    
    @Column(name = "borrower_id", nullable = false)
    private Integer borrowerId;
    
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;
    
    @Column(name = "borrow_time")
    @CreationTimestamp
    private Timestamp borrowTime;
    
    @Column(name = "expected_return_time", nullable = false)
    private Timestamp expectedReturnTime;
    
    @Column(name = "actual_return_time")
    private Timestamp actualReturnTime;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // borrowing: 借用中, returned: 已归还, overdue: 已逾期
    
    @Column(name = "update_time")
    @UpdateTimestamp
    private Timestamp updateTime;
    
    // Getter 和 Setter
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getToolId() {
        return toolId;
    }
    
    public void setToolId(Integer toolId) {
        this.toolId = toolId;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    public String getToolType() {
        return toolType;
    }
    
    public void setToolType(String toolType) {
        this.toolType = toolType;
    }
    
    public Integer getBorrowerId() {
        return borrowerId;
    }
    
    public void setBorrowerId(Integer borrowerId) {
        this.borrowerId = borrowerId;
    }
    
    public Integer getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }
    
    public Timestamp getBorrowTime() {
        return borrowTime;
    }
    
    public void setBorrowTime(Timestamp borrowTime) {
        this.borrowTime = borrowTime;
    }
    
    public Timestamp getExpectedReturnTime() {
        return expectedReturnTime;
    }
    
    public void setExpectedReturnTime(Timestamp expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }
    
    public Timestamp getActualReturnTime() {
        return actualReturnTime;
    }
    
    public void setActualReturnTime(Timestamp actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}