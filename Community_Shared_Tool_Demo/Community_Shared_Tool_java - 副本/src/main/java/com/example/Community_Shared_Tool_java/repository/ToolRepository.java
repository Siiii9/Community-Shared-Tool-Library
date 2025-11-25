package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Integer> {

    // 根据设备类别ID查询设备索引
    //findByDeviceClassId 会被解析为 SELECT * FROM DEVICE_INDEX WHERE DEVICE_CLASS_ID = ?
    List<Tool> findByDeviceClassId(Integer deviceClassId);

    // 根据设备模板ID查询设备索引
    List<Tool> findByDeviceTemplateId(Integer deviceTemplateId);

    // 根据设备表名查询设备索引
    List<Tool> findByDeviceTable(String deviceTable);

//    // 根据设备表ID查询设备索引
//    DeviceIndex findByDeviceTableId(Integer deviceTableId);

    // 根据设备名称模糊查询
    List<Tool> findByDeviceNameContaining(String deviceName);

    // 根据设备类别ID和设备表名查询
    List<Tool> findByDeviceClassIdAndDeviceTable(Integer deviceClassId, String deviceTable);

    //根据设备类别ID和模板ID查询设备
    // 修改为 List 返回，支持多条记录
    List<Tool> findByDeviceClassIdAndDeviceTemplateId(Integer deviceClassId, Integer deviceTemplateId);
}