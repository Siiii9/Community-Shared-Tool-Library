package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.PublishedTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublishedToolRepository extends JpaRepository<PublishedTool, Integer> {
    
    // 根据所有者ID查询发布的工具
    List<PublishedTool> findByOwnerId(Integer ownerId);
    
    // 根据工具名称模糊查询
    List<PublishedTool> findByToolNameContaining(String toolName);
    
    // 根据状态查询
    List<PublishedTool> findByStatus(String status);
    
    // 根据所有者ID和状态查询
    List<PublishedTool> findByOwnerIdAndStatus(Integer ownerId, String status);
    
    // 根据所有者ID和工具名称模糊查询
    List<PublishedTool> findByOwnerIdAndToolNameContaining(Integer ownerId, String toolName);
}