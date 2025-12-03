package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.entity.UserInfo;
import com.example.Community_Shared_Tool_java.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @GetMapping("/check-admin/{username}")
    public ResponseEntity<?> checkAdmin(@PathVariable String username) {
        System.out.println("检查管理员权限: " + username);
        Optional<UserInfo> userOptional = userInfoRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            UserInfo user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("isAdmin", Boolean.TRUE.equals(user.getIsAdmin()));
            response.put("success", true);
            return ResponseEntity.ok(response);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("isAdmin", false);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam String currentAdmin) {
        try {
            System.out.println("获取所有用户，当前管理员: " + currentAdmin);
            List<UserInfo> users = userInfoRepository.findAll();
            System.out.println("数据库查询到用户数: " + users.size());

            // 过滤掉管理员自己
            users.removeIf(user -> "admin".equals(user.getUsername()));
            System.out.println("过滤后用户数: " + users.size());

            // 返回用户列表
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            System.err.println("获取用户列表失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取用户列表失败");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/users/{userId}/freeze")
    public ResponseEntity<?> freezeUser(@PathVariable Integer userId) {
        try {
            System.out.println("冻结用户: " + userId);
            Optional<UserInfo> userOptional = userInfoRepository.findById(userId);
            if (userOptional.isPresent()) {
                UserInfo user = userOptional.get();
                if (Boolean.TRUE.equals(user.getIsAdmin())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "不能冻结管理员账户");
                    return ResponseEntity.badRequest().body(error);
                }
                user.setIsFrozen(true);
                userInfoRepository.save(user);
                System.out.println("用户 " + userId + " 已冻结");

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "用户已冻结");
                response.put("userId", userId);
                response.put("isFrozen", user.getIsFrozen());
                return ResponseEntity.ok(response);
            }

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);

        } catch (Exception e) {
            System.err.println("冻结用户失败: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "冻结失败");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/users/{userId}/unfreeze")
    public ResponseEntity<?> unfreezeUser(@PathVariable Integer userId) {
        try {
            System.out.println("解封用户: " + userId);
            Optional<UserInfo> userOptional = userInfoRepository.findById(userId);
            if (userOptional.isPresent()) {
                UserInfo user = userOptional.get();
                user.setIsFrozen(false);
                userInfoRepository.save(user);

                System.out.println("用户 " + userId + " 已解封，当前状态: " + user.getIsFrozen());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "用户已解封");
                response.put("userId", userId);
                response.put("isFrozen", user.getIsFrozen());
                return ResponseEntity.ok(response);
            }

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);

        } catch (Exception e) {
            System.err.println("解封用户失败: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "解封失败");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PutMapping("/users/{userId}/reset-credit")
    public ResponseEntity<?> resetCredit(@PathVariable Integer userId) {
        try {
            System.out.println("重置信誉分: " + userId);
            Optional<UserInfo> userOptional = userInfoRepository.findById(userId);
            if (userOptional.isPresent()) {
                UserInfo user = userOptional.get();
                if (Boolean.TRUE.equals(user.getIsAdmin())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "不能重置管理员信誉分");
                    return ResponseEntity.badRequest().body(error);
                }
                user.setCreditScore(100);
                userInfoRepository.save(user);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "信誉分已重置为100");
                response.put("userId", userId);
                response.put("creditScore", user.getCreditScore());
                return ResponseEntity.ok(response);
            }

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户不存在");
            return ResponseEntity.status(404).body(error);

        } catch (Exception e) {
            System.err.println("重置信誉分失败: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "重置失败");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            long totalUsers = userInfoRepository.count();
            long frozenUsers = userInfoRepository.findAll().stream()
                    .filter(user -> Boolean.TRUE.equals(user.getIsFrozen()))
                    .count();
            long adminCount = userInfoRepository.findAll().stream()
                    .filter(user -> Boolean.TRUE.equals(user.getIsAdmin()))
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("success", true);
            stats.put("totalUsers", totalUsers - adminCount);
            stats.put("frozenUsers", frozenUsers);
            stats.put("activeUsers", totalUsers - frozenUsers - adminCount);

            System.out.println("统计数据 - 总用户: " + stats.get("totalUsers") +
                    ", 冻结: " + stats.get("frozenUsers") +
                    ", 活跃: " + stats.get("activeUsers"));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("获取统计数据失败: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取统计数据失败");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}