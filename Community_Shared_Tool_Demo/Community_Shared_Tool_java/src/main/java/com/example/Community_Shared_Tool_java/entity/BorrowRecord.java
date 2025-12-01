package com.example.Community_Shared_Tool_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "borrow_record")
public class BorrowRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "tool_id", nullable = false)
    private Integer toolId;
    
    @Column(name = "borrower_id", nullable = false)
    private Integer borrowerId;
    
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;
    
    @Column(name = "status", nullable = false)
    private String status; // PENDING: 待同意, APPROVED: 已同意, TAKEN: 已取用, RETURNED: 已归还, REJECTED: 已拒绝
    
    @Column(name = "apply_time")
    @CreationTimestamp
    private Timestamp applyTime;
    
    @Column(name = "approve_time")
    private Timestamp approveTime;
    
    @Column(name = "take_time")
    private Timestamp takeTime;
    
    @Column(name = "return_time")
    private Timestamp returnTime;
    
    @Column(name = "expected_return_time")
    private Timestamp expectedReturnTime;
    
    @Column(name = "borrow_days")
    private Integer borrowDays;
    
    @Column(name = "apply_reason")
    private String applyReason;
    
    @Column(name = "reject_reason")
    private String rejectReason;
    
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getApplyTime() {
        return applyTime;
    }
    
    public void setApplyTime(Timestamp applyTime) {
        this.applyTime = applyTime;
    }
    
    public Timestamp getApproveTime() {
        return approveTime;
    }
    
    public void setApproveTime(Timestamp approveTime) {
        this.approveTime = approveTime;
    }
    
    public Timestamp getTakeTime() {
        return takeTime;
    }
    
    public void setTakeTime(Timestamp takeTime) {
        this.takeTime = takeTime;
    }
    
    public Timestamp getReturnTime() {
        return returnTime;
    }
    
    public void setReturnTime(Timestamp returnTime) {
        this.returnTime = returnTime;
    }
    
    public Timestamp getExpectedReturnTime() {
        return expectedReturnTime;
    }
    
    public void setExpectedReturnTime(Timestamp expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }
    
    public Integer getBorrowDays() {
        return borrowDays;
    }
    
    public void setBorrowDays(Integer borrowDays) {
        this.borrowDays = borrowDays;
    }
    
    public String getApplyReason() {
        return applyReason;
    }
    
    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }
    
    public String getRejectReason() {
        return rejectReason;
    }
    
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
    
    public Timestamp getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
