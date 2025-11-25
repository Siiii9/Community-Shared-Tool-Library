package com.example.smartlab_demo_java.service;

import com.example.smartlab_demo_java.entity.PropertyType;
import com.example.smartlab_demo_java.repository.PropertyTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyTypeService {

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    // 获取所有属性类型
    public List<PropertyType> getAllPropertyTypes() {
        return propertyTypeRepository.findAll();
    }

    // 根据 ID 获取属性类型
    public PropertyType getPropertyTypeById(Integer id) {
        Optional<PropertyType> optional = propertyTypeRepository.findById(id);
        return optional.orElse(null);
    }

    // 根据 PROPERTY_NAME 获取属性类型
    public PropertyType getPropertyTypeByName(String propertyName) {
        return propertyTypeRepository.findByPropertyName(propertyName);
    }

    // 生成字段的 SQL 类型定义
    public String generateColumnDefinition(String columnName, Integer propertyTypeId, Integer columnLength) {
        PropertyType propertyType = getPropertyTypeById(propertyTypeId);
        if (propertyType == null) {
            throw new IllegalArgumentException("无效的 PROPERTY_TYPE_ID: " + propertyTypeId);
        }

        StringBuilder columnDef = new StringBuilder();
        columnDef.append("\"").append(columnName).append("\" ").append(propertyType.getPropertyName());

        // 处理引用符
        if (propertyType.getQuote() != null && !propertyType.getQuote().isEmpty()) {
            if (propertyType.getQuote().equals("(n)") && columnLength != null) {
                columnDef.append("(").append(columnLength).append(")");
            } else {
                columnDef.append(propertyType.getQuote());
            }
        }

        // 添加默认值
        if (propertyType.getDefaultValue() != null && !propertyType.getDefaultValue().isEmpty()) {
            columnDef.append(" DEFAULT ").append(propertyType.getDefaultValue());
        }

        return columnDef.toString();
    }

    // 保存属性类型
    public PropertyType savePropertyType(PropertyType propertyType) {
        return propertyTypeRepository.save(propertyType);
    }
}