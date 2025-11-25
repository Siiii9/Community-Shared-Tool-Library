package com.example.smartlab_demo_java.repository;

import com.example.smartlab_demo_java.entity.ToolTemplateDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolTemplateDetailRepository extends JpaRepository<ToolTemplateDetail, Integer> {
    List<ToolTemplateDetail> findByDeviceTemplateId(Integer deviceTemplateId);
}