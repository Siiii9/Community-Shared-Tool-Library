package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyTypeRepository extends JpaRepository<PropertyType, Integer> {
    // 可根据需要添加自定义查询方法，例如：
    PropertyType findByPropertyName(String propertyName);
}