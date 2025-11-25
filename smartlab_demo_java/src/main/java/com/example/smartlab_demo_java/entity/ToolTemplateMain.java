package com.example.smartlab_demo_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "\"DEVICE_TEMPLATE_MAIN\"", schema = "public")
public class ToolTemplateMain {

    @Id
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"CLASS_ID\"", nullable = false)
    private Integer classId;

    @Column(name = "\"TEMPLATE_NAME\"", length = 50)
    private String templateName;

    @Column(name = "\"TEMPLATE_DESC\"", length = 255)
    private String templateDesc;

    @Column(name = "\"CREATE_TIME\"", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createTime;

    // 默认构造函数
    public ToolTemplateMain() {}

    // Getter 和 Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDesc() {
        return templateDesc;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDesc = templateDesc;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}