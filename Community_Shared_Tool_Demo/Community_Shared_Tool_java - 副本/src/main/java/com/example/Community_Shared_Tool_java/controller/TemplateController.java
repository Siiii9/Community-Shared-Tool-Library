package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.entity.ToolTemplateMain;
import com.example.Community_Shared_Tool_java.entity.ToolTemplateDetail;
import com.example.Community_Shared_Tool_java.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping("/templates")
    public List<ToolTemplateMain> getTemplates(@RequestParam("classId") Integer classId) {
        return templateService.getTemplatesByClassId(classId);
    }

    @GetMapping("/template-details")
    public List<ToolTemplateDetail> getTemplateDetails(@RequestParam("deviceTemplateId") Integer deviceTemplateId) {
        return templateService.getTemplateDetails(deviceTemplateId);
    }
}