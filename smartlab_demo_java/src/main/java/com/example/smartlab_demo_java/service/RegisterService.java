package com.example.smartlab_demo_java.service;

import com.example.smartlab_demo_java.entity.UserInfo;
import com.example.smartlab_demo_java.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterService {
    @Autowired
    private UserInfoRepository userInfoRepository;

    // 内部枚举 RegisterResult
    public enum RegisterResult {
        SUCCESS,          // 注册成功
        USERNAME_EXISTS,  // 用户名已存在
        INVALID_DATA      // 数据无效
    }

    public RegisterResult registerUser(String username, String password, String basicInfo) {
        // 检查输入是否有效
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return RegisterResult.INVALID_DATA;
        }

        // 检查用户名是否已存在
        if (userInfoRepository.findByUsername(username).isPresent()) {
            return RegisterResult.USERNAME_EXISTS;
        }

        // 创建新用户
        UserInfo newUser = new UserInfo();
        newUser.setUsername(username);
        newUser.setPassword(password); //建议加密密码
        newUser.setUserBasicinfo(basicInfo != null ? basicInfo : ""); // 已改成小写 i
        userInfoRepository.save(newUser);

        return RegisterResult.SUCCESS;
    }
}