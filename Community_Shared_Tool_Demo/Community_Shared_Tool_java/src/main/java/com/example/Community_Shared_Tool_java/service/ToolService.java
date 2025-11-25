package com.example.Community_Shared_Tool_java.service;


import com.example.Community_Shared_Tool_java.entity.Tool;
import com.example.Community_Shared_Tool_java.repository.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ToolService {

    @Autowired
    private ToolRepository toolRepository;

    // 获取某类别下的设备列表
    public List<Tool> getDevicesByClassId(Integer classId) {
        return toolRepository.findByDeviceClassId(classId);
    }

}
