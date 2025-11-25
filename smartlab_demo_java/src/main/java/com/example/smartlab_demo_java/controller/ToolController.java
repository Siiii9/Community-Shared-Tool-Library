package com.example.smartlab_demo_java.controller;

import com.example.smartlab_demo_java.service.ToolManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class ToolController {

    @Autowired
    private ToolManagementService toolManagementService;

    @PostMapping("/add-device")
    public void addDeviceWithTable(@RequestBody Map<String, Object> deviceData) {
        Integer classId = (Integer) deviceData.get("classId");
        Integer deviceTemplateId = (Integer) deviceData.get("deviceTemplateId");
        Map<String, Object> dynamicFields = (Map<String, Object>) deviceData.get("dynamicFields");

        toolManagementService.addDeviceIntoTable(classId, deviceTemplateId, dynamicFields);
    }
}