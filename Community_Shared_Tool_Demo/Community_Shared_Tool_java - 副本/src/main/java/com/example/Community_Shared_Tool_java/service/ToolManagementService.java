package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.Tool;
import com.example.Community_Shared_Tool_java.entity.ToolTemplateDetail;
import com.example.Community_Shared_Tool_java.entity.PropertyType;
import com.example.Community_Shared_Tool_java.repository.ToolRepository;
import com.example.Community_Shared_Tool_java.repository.ToolTemplateDetailRepository;
import com.example.Community_Shared_Tool_java.repository.PropertyTypeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolManagementService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ToolTemplateDetailRepository templateDetailRepository;

    @Autowired
    private PropertyTypeRepository propertyTypeRepository;

    @Transactional
    public void addDeviceIntoTable(Integer classId, Integer deviceTemplateId, Map<String, Object> dynamicFields) {
        // 查询 DEVICE_INDEX，返回所有匹配记录
        List<Tool> indices = toolRepository.findByDeviceClassIdAndDeviceTemplateId(classId, deviceTemplateId);
        String tableName;

        if (!indices.isEmpty()) {
            // 表已存在，检查表名一致性
            tableName = indices.get(0).getDeviceTable();
            for (Tool index : indices) {
                if (!index.getDeviceTable().equals(tableName)) {
                    throw new IllegalStateException("DEVICE_INDEX 中同一 CLASS_ID 和 TEMPLATE_ID 的 DEVICE_TABLE 不一致: " +
                            tableName + " vs " + index.getDeviceTable());
                }
            }
        } else {
            // 表不存在，创建新表
            tableName = generateTableName(classId, deviceTemplateId);
            createDeviceTable(tableName, deviceTemplateId);
        }

        // 插入设备数据并获取生成的 ID
        Integer deviceId = insertDeviceData(tableName, classId, deviceTemplateId, dynamicFields);

        // 更新 DEVICE_INDEX
        Tool newIndex = new Tool();
        newIndex.setDeviceClassId(classId);
        newIndex.setDeviceTemplateId(deviceTemplateId);
        newIndex.setDeviceTable(tableName);
        newIndex.setDeviceTableID(deviceId); // 使用设备表的 ID
        newIndex.setDeviceName("Device_" + classId + "_" + deviceTemplateId + "_" + deviceId);
        toolRepository.save(newIndex);
    }

    // 生成表名
    private String generateTableName(Integer classId, Integer deviceTemplateId) {
        return "DEVICE_RECORD_" + String.format("%04d_%04d", classId, deviceTemplateId);
    }

    // 创建设备表（PostgreSQL 自增）
    private void createDeviceTable(String tableName, Integer deviceTemplateId) {
        List<ToolTemplateDetail> details = templateDetailRepository.findByDeviceTemplateId(deviceTemplateId);

        StringBuilder sql = new StringBuilder("CREATE TABLE \"" + tableName + "\" (");
        sql.append("\"ID\" INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, "); // PostgreSQL 自增
        sql.append("\"CLASS_ID\" INTEGER NOT NULL, ");
        sql.append("\"DEVICE_TEMPLATE_ID\" INTEGER NOT NULL, ");

        for (ToolTemplateDetail detail : details) {
            String columnDef = generateColumnDefinition(detail);
            sql.append(columnDef).append(", ");
        }
        sql.append("\"CREATE_TIME\" TIMESTAMP NOT NULL)");

        entityManager.createNativeQuery(sql.toString()).executeUpdate();
    }

    // 生成字段定义
    private String generateColumnDefinition(ToolTemplateDetail detail) {
        PropertyType propertyType = propertyTypeRepository.findById(detail.getPropertyTypeId())
                .orElseThrow(() -> new IllegalArgumentException("无效的 PROPERTY_TYPE_ID: " + detail.getPropertyTypeId()));

        StringBuilder columnDef = new StringBuilder();
        columnDef.append("\"").append(detail.getColumnName()).append("\" ").append(propertyType.getPropertyName());

        if (propertyType.getQuote() != null && !propertyType.getQuote().isEmpty()) {
            if (propertyType.getQuote().equals("(n)") && detail.getColumnLength() != null) {
                columnDef.append("(").append(detail.getColumnLength()).append(")");
            } else {
                columnDef.append(propertyType.getQuote());
            }
        }

        if (propertyType.getDefaultValue() != null && !propertyType.getDefaultValue().isEmpty()) {
            columnDef.append(" DEFAULT ").append(propertyType.getDefaultValue());
        }

        return columnDef.toString();
    }

    // 插入设备数据并返回生成的 ID（PostgreSQL）
    private Integer insertDeviceData(String tableName, Integer classId, Integer deviceTemplateId, Map<String, Object> dynamicFields) {
        StringBuilder sql = new StringBuilder("INSERT INTO \"" + tableName + "\" (\"CLASS_ID\", \"DEVICE_TEMPLATE_ID\", \"CREATE_TIME\"");
        StringBuilder values = new StringBuilder("VALUES (:classId, :deviceTemplateId, CURRENT_TIMESTAMP");

        Map<String, Object> params = new HashMap<>();
        params.put("classId", classId);
        params.put("deviceTemplateId", deviceTemplateId);

        List<ToolTemplateDetail> details = templateDetailRepository.findByDeviceTemplateId(deviceTemplateId);
        for (ToolTemplateDetail detail : details) {
            String columnName = detail.getColumnName();
            if (dynamicFields.containsKey(columnName)) {
                sql.append(", \"").append(columnName).append("\"");
                values.append(", :").append(columnName);
                params.put(columnName, dynamicFields.get(columnName));
            }
        }
        sql.append(") ").append(values).append(") RETURNING \"ID\"");

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).intValue();
    }
}