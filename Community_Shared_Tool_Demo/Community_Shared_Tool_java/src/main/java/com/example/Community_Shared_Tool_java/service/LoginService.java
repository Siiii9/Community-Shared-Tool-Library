package com.example.Community_Shared_Tool_java.service;

import com.example.Community_Shared_Tool_java.entity.UserInfo;
import com.example.Community_Shared_Tool_java.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    @Autowired
    private UserInfoRepository userinfoRepository;

    // 内部枚举 LoginResult
    public enum LoginResult {
        SUCCESS,           // 登录成功
        USERNAME_NOT_FOUND, // 用户名不存在
        INCORRECT_PASSWORD, // 密码错误
        USER_FROZEN         // 用户被冻结
    }

    public LoginResult login(String username, String password) {
        // 硬编码管理员账号
        if ("admin".equals(username) && "123456".equals(password)) {
            // 检查管理员用户是否存在，不存在则创建
            Optional<UserInfo> adminOptional = userinfoRepository.findByUsername("admin");
            if (adminOptional.isEmpty()) {
                // 创建管理员账户
                UserInfo admin = new UserInfo();
                admin.setUsername("admin");
                admin.setPassword("123456");
                admin.setIsAdmin(true);
                admin.setIsFrozen(false);
                admin.setCreditScore(999); // 管理员信誉分设为999
                userinfoRepository.save(admin);
            } else {
                // 更新现有管理员用户
                UserInfo admin = adminOptional.get();
                admin.setIsAdmin(true);
                admin.setPassword("123456"); // 确保密码正确
                userinfoRepository.save(admin);
            }
            return LoginResult.SUCCESS;
        }

        // 普通用户登录逻辑
        Optional<UserInfo> userOptional = userinfoRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return LoginResult.USERNAME_NOT_FOUND;
        } else {
            UserInfo user = userOptional.get();
            // 检查用户是否被冻结
            if (Boolean.TRUE.equals(user.getIsFrozen())) {
                return LoginResult.USER_FROZEN;
            }
            if (!user.getPassword().equals(password)) {
                return LoginResult.INCORRECT_PASSWORD;
            } else {
                return LoginResult.SUCCESS;
            }
        }
    }

    // 获取用户信息（包括管理员信息）
    public Optional<UserInfo> getUserInfo(String username) {
        return userinfoRepository.findByUsername(username);
    }
}