package com.example.smartlab_demo_java.repository;

import com.example.smartlab_demo_java.entity.ToolTemplateMain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolTemplateMainRepository extends JpaRepository<ToolTemplateMain, Integer> {
    List<ToolTemplateMain> findByClassId(Integer classId);
}