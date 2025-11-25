package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.dto.ToolClassDto;
import com.example.Community_Shared_Tool_java.entity.Tool;
import com.example.Community_Shared_Tool_java.service.ToolClassService;
import com.example.Community_Shared_Tool_java.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device-classes")
public class ToolClassController {

    @Autowired
    private ToolClassService toolClassService;
    @Autowired
    private ToolService toolService;

    @GetMapping("/tree")
    public ResponseEntity<List<ToolClassDto>> getTreeDeviceClasses() {
        return ResponseEntity.ok(toolClassService.getTreeDeviceClasses());
    }

    @GetMapping("/flat")
    public ResponseEntity<List<ToolClassDto>> getFlatDeviceClasses() {
        return ResponseEntity.ok(toolClassService.getFlatDeviceClasses());
    }

    @GetMapping("/alldevices")
    public ResponseEntity<List<Tool>> getAllDevicesFromClass(@RequestParam(name = "classId", required = true) Integer classId) {
        List<Tool> devices = toolService.getDevicesByClassId(classId);
        if (devices.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(devices); // 200 OK
    }
}