package com.example.smartlab_demo_java.service;

import com.example.smartlab_demo_java.dto.ToolClassDto;
import com.example.smartlab_demo_java.entity.ToolClass;
import com.example.smartlab_demo_java.repository.ToolClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ToolClassService {

    @Autowired
    private ToolClassRepository toolClassRepository;

    /**
     * 获取所有设备类别，以扁平结构返回。
     */
    public List<ToolClassDto> getFlatDeviceClasses() {
        return toolClassRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取设备类别，以树状结构返回。
     */
    public List<ToolClassDto> getTreeDeviceClasses() {
        List<ToolClassDto> dtos = getFlatDeviceClasses();

        // 创建 ID -> DTO 的映射
        Map<Integer, ToolClassDto> dtoMap = dtos.stream()
                .collect(Collectors.toMap(ToolClassDto::getDeviceClassId, dto -> dto));

        List<ToolClassDto> tree = new ArrayList<>();

        // 遍历所有设备类别，将子节点添加到父节点
        for (ToolClassDto dto : dtos) {
            Integer parentId = dto.getParentDeviceClassId();

            if (parentId == null || parentId == 0) {
                // 作为根节点
                tree.add(dto);
            } else {
                // 找到父节点并添加子节点
                ToolClassDto parentDto = dtoMap.get(parentId);
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return tree;
    }

    /**
     * 根据名称查找设备类别。
     */
    public ToolClass findByDeviceClassName(String name) {
        return toolClassRepository.findByDeviceClassName(name);
    }

    /**
     * 将实体转换为 DTO。
     */
    private ToolClassDto convertToDto(ToolClass toolClass) {
        ToolClassDto dto = new ToolClassDto();
        dto.setDeviceClassId(toolClass.getId());
        dto.setDeviceClassName(toolClass.getClassName());
        dto.setParentDeviceClassId(toolClass.getParentClassId());
        dto.setCreateTime(toolClass.getCreateTime());
        return dto;
    }

    /**
     * 获取设备类别下所有设备
     */
}
