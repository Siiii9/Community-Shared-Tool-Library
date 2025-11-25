package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.ToolTemplateMain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolTemplateMainRepository extends JpaRepository<ToolTemplateMain, Integer> {
    List<ToolTemplateMain> findByClassId(Integer classId);
}