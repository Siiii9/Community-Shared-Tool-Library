package com.example.smartlab_demo_java.repository;

import com.example.smartlab_demo_java.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    //    UserInfo findByUsername(String username);
    Optional<UserInfo> findByUsername(String username);
    boolean existsByUsername(String username);
}
