// src/main/java/com/example/Community_Shared_Tool_java/repository/CreditLogRepository.java
package com.example.Community_Shared_Tool_java.repository;

import com.example.Community_Shared_Tool_java.entity.CreditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditLogRepository extends JpaRepository<CreditLog, Long> {
    List<CreditLog> findByUserIdOrderByCreateTimeDesc(Integer userId);
}