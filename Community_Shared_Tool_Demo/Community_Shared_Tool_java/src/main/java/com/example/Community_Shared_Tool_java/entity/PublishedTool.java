package com.example.Community_Shared_Tool_java.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotBlank(message = "工具名称不能为空")
    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @NotBlank(message = "工具类型不能为空")
    @Column(name = "tool_type", nullable = false, length = 50)
    private String toolType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "工具位置不能为空")
    @Column(name = "location", nullable = false, length = 100)
    private String location;

    // 新增经纬度字段
    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @NotBlank(message = "工具状态不能为空")
    @Column(name = "status", nullable = false, length = 20)
    private String status; // available: 可借用, borrowed: 已借出, borrowing: 借用中, maintenance: 维护中, pending: 申请中

    @NotNull(message = "工具所有者ID不能为空")
    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "publish_time")
    @CreationTimestamp
    private Timestamp publishTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    private Timestamp updateTime;

    @Column(name = "image_url", length = 100000)
    private String imageUrl;

    @NotNull(message = "最大借用天数不能为空")
    @Positive(message = "最大借用天数必须为正数")
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

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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