package com.example.smartlab_demo_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "\"DEVICE_INDEX\"", schema = "public")
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"DEVICE_TEMPLATE_ID\"", nullable = false)
    private Integer deviceTemplateId;

    @Column(name = "\"DEVICE_CLASS_ID\"", nullable = false)
    private Integer deviceClassId;

    @Column(name = "\"DEVICE_TABLE\"")
    private String deviceTable;

    @Column(name = "\"DEVICE_TABLE_ID\"", nullable = false)//设备在设备表中的id
    private Integer deviceTableID;

    @Column(name = "\"DEVICE_NAME\"")
    private String deviceName;

    @Column(name = "\"PICTURE\"")
    private String devicePicture;

    @Column(name = "\"CREATE_TIME\"", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createTime;

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

    public Integer getDeviceClassId() {
        return deviceClassId;
    }

    public void setDeviceClassId(Integer deviceClassId) {
        this.deviceClassId = deviceClassId;
    }

    public String getDeviceTable() {
        return deviceTable;
    }

    public void setDeviceTable(String deviceTable) {
        this.deviceTable = deviceTable;
    }

    public Integer getDeviceTableID() {
        return deviceTableID;
    }

    public void setDeviceTableID(Integer deviceTableID) {
        this.deviceTableID = deviceTableID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDevicePicture() {
        return devicePicture;
    }

    public void setDevicePicture(String devicePicture) {
        this.devicePicture = devicePicture;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}