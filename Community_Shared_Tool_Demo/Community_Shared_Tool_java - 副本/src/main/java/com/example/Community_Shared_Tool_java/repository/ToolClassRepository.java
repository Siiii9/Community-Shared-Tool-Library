package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.ToolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolClassRepository extends JpaRepository<ToolClass, Integer> {

    ToolClass findByDeviceClassName(String name);

    // 按 parent_id 排序，提高树构建时的性能
    @Query("SELECT d FROM ToolClass d ORDER BY d.parentClassId ASC, d.id ASC")
    List<ToolClass> findAllSorted();
}
