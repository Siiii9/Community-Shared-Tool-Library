package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.PublishedTool;
import com.example.Community_Shared_Tool_java.repository.PublishedToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PublishedToolService {
    
    @Autowired
    private PublishedToolRepository publishedToolRepository;
    
    // 发布新工具
    @Transactional
    public PublishedTool publishTool(PublishedTool tool) {
        tool.setStatus("available"); // 默认状态为可借用
        return publishedToolRepository.save(tool);
    }
    
    // 获取用户发布的所有工具
    public List<PublishedTool> getToolsByOwnerId(Integer ownerId) {
        return publishedToolRepository.findByOwnerId(ownerId);
    }
    
    // 根据ID获取工具
    public Optional<PublishedTool> getToolById(Integer id) {
        return publishedToolRepository.findById(id);
    }
    
    // 更新工具信息
    @Transactional
    public PublishedTool updateTool(PublishedTool tool) {
        return publishedToolRepository.save(tool);
    }
    
    // 更新工具状态
    @Transactional
    public PublishedTool updateToolStatus(Integer id, String status) {
        PublishedTool tool = publishedToolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("工具不存在"));
        tool.setStatus(status);
        return publishedToolRepository.save(tool);
    }
    
    // 删除工具
    @Transactional
    public void deleteTool(Integer id) {
        publishedToolRepository.deleteById(id);
    }
    
    // 根据条件搜索工具
    public List<PublishedTool> searchTools(Integer ownerId, String toolName, String status) {
        if (ownerId != null && toolName != null && !toolName.isEmpty() && status != null && !status.isEmpty()) {
            return publishedToolRepository.findByOwnerIdAndToolNameContaining(ownerId, toolName)
                    .stream()
                    .filter(tool -> tool.getStatus().equals(status))
                    .collect(Collectors.toList());
        } else if (ownerId != null && toolName != null && !toolName.isEmpty()) {
            return publishedToolRepository.findByOwnerIdAndToolNameContaining(ownerId, toolName);
        } else if (ownerId != null && status != null && !status.isEmpty()) {
            return publishedToolRepository.findByOwnerIdAndStatus(ownerId, status);
        } else if (ownerId != null) {
            return publishedToolRepository.findByOwnerId(ownerId);
        } else if (toolName != null && !toolName.isEmpty()) {
            return publishedToolRepository.findByToolNameContaining(toolName);
        } else if (status != null && !status.isEmpty()) {
            return publishedToolRepository.findByStatus(status);
        } else {
            return publishedToolRepository.findAll();
        }
    }
}