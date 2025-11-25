package com.example.Community_Shared_Tool_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "\"DEVICE_TEMPLATE_DETAIL\"", schema = "public")
public class ToolTemplateDetail {

    @Id
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"DEVICE_TEMPLATE_ID\"")
    private Integer deviceTemplateId;

    @Column(name = "\"COLUMN_DESC\"", length = 50)
    private String columnDesc;

    @Column(name = "\"PROPERTY_TYPE_ID\"")
    private Integer propertyTypeId;

    @Column(name = "\"COLUMN_NAME\"", length = 50)
    private String columnName;

    @Column(name = "\"COLUMN_LENGTH\"")
    private Integer columnLength;

    @Column(name = "\"CREATE_TIME\"", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createTime;

    // 默认构造函数
    public ToolTemplateDetail() {}

    // Getter 和 Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeviceTemplateId() {
        return deviceTemplateId;
    }

    public void setDeviceTemplateId(Integer deviceTemplateId) {
        this.deviceTemplateId = deviceTemplateId;
    }

    public String getColumnDesc() {
        return columnDesc;
    }

    public void setColumnDesc(String columnDesc) {
        this.columnDesc = columnDesc;
    }

    public Integer getPropertyTypeId() {
        return propertyTypeId;
    }

    public void setPropertyTypeId(Integer propertyTypeId) {
        this.propertyTypeId = propertyTypeId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getColumnLength() {
        return columnLength;
    }

    public void setColumnLength(Integer columnLength) {
        this.columnLength = columnLength;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}