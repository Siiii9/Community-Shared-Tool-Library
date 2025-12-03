package com.example.Community_Shared_Tool_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "published_tool")
public class PublishedTool {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;
    
    @Column(name = "tool_type", nullable = false, length = 50)
    private String toolType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "location", nullable = false, length = 100)
    private String location;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // available: 可借用, borrowed: 已借出, maintenance: 维护中
    
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;
    
    @Column(name = "publish_time")
    @CreationTimestamp
    private Timestamp publishTime;
    
    @Column(name = "update_time")
    @UpdateTimestamp
    private Timestamp updateTime;
    
    @Column(name = "image_url", length = 200)
    private String imageUrl;
    
    @Column(name = "borrow_days_limit")
    private Integer borrowDaysLimit; // 最大借用天数
    
    // Getter 和 Setter
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }
    
    public Timestamp getPublishTime() {
        return publishTime;
    }
    
    public void setPublishTime(Timestamp publishTime) {
        this.publishTime = publishTime;
    }
    
    public Timestamp getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getBorrowDaysLimit() {
        return borrowDaysLimit;
    }
    
    public void setBorrowDaysLimit(Integer borrowDaysLimit) {
        this.borrowDaysLimit = borrowDaysLimit;
    }
}