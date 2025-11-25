package com.example.Community_Shared_Tool_java.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolClassDto {
    private Integer id;
    private String className;
    private Integer parentClassId;
    private Timestamp createTime;
    private Integer templateId;

    // 避免前端报错，确保 children 默认是空列表
    private List<ToolClassDto> children = new ArrayList<>();

    public Integer getDeviceClassId() { return id; }
    public void setDeviceClassId(Integer id) { this.id = id; }

    public String getDeviceClassName() { return className; }
    public void setDeviceClassName(String className) { this.className = className; }

    public Integer getParentDeviceClassId() { return parentClassId; }
    public void setParentDeviceClassId(Integer parentClassId) { this.parentClassId = parentClassId; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public List<ToolClassDto> getChildren() { return children; }
    public void setChildren(List<ToolClassDto> children) { this.children = children; }
}
