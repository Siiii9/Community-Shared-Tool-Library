package com.example.smartlab_demo_java.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "\"PROPERTY_TYPE\"", schema = "public")
public class PropertyType {

    @Id
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"PROPERTY_NAME\"", length = 50)
    private String propertyName;

    @Column(name = "\"QUOTE\"", length = 4)
    private String quote;

    @Column(name = "\"DEFAULT_VALUE\"", length = 254)
    private String defaultValue;

    @Column(name = "\"CREATE_TIME\"", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createTime;


    // 默认构造函数
    public PropertyType() {}

    // Getter 和 Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}