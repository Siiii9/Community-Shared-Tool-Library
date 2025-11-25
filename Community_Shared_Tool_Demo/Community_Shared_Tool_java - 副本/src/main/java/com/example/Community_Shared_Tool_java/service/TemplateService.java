package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.ToolTemplateMain;
import com.example.Community_Shared_Tool_java.entity.ToolTemplateDetail;
import com.example.Community_Shared_Tool_java.repository.ToolTemplateMainRepository;
import com.example.Community_Shared_Tool_java.repository.ToolTemplateDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    @Autowired
    private ToolTemplateMainRepository templateMainRepository;

    @Autowired
    private ToolTemplateDetailRepository templateDetailRepository;

    // 获取类别对应的模板
    public List<ToolTemplateMain> getTemplatesByClassId(Integer classId) {
        return templateMainRepository.findByClassId(classId);
    }

    // 获取模板的详细字段
    public List<ToolTemplateDetail> getTemplateDetails(Integer deviceTemplateId) {
        return templateDetailRepository.findByDeviceTemplateId(deviceTemplateId);
    }
}