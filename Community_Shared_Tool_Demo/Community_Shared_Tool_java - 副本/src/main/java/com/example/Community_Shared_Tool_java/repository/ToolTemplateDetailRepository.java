package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.ToolTemplateDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolTemplateDetailRepository extends JpaRepository<ToolTemplateDetail, Integer> {
    List<ToolTemplateDetail> findByDeviceTemplateId(Integer deviceTemplateId);
}