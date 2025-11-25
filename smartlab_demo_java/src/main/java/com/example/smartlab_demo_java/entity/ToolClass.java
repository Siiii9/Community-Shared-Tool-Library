package com.example.smartlab_demo_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Entity
@Table(name = "\"DEVICE_CLASS\"", schema = "public")
public class ToolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"CLASS_NAME\"", nullable = false, length = 50)
    private String deviceClassName;

    @Column(name = "\"DEVICES_TABLE\"", length = 50)
    private String devicesTable;

    @Column(name = "\"PARENT_CLASS_ID\"")
    private Integer parentClassId;

    @Column(name = "\"CREATE_TIME\"", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createTime;

    // 标准化的 getter 和 setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getClassName() { return deviceClassName; }
    public void setClassName(String className) { this.deviceClassName = className; }
    public String getDevicesTable() { return devicesTable; }
    public void setDevicesTable(String devicesTable) { this.devicesTable = devicesTable; }
    public Integer getParentClassId() { return parentClassId; }
    public void setParentClassId(Integer parentClassId) { this.parentClassId = parentClassId; }
    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}