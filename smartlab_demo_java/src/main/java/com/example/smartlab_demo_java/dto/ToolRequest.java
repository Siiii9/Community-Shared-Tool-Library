package com.example.smartlab_demo_java.dto;

import java.util.Map;

public class ToolRequest {
    private Integer classId;
    private Integer templateId;
    private Map<String, Object> properties;

    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }
    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }
}