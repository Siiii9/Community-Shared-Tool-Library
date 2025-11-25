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
        INCORRECT_PASSWORD  // 密码错误
    }

    public LoginResult login(String username, String password) {
        Optional<UserInfo> userOptional = userinfoRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return LoginResult.USERNAME_NOT_FOUND;
        } else {
            UserInfo user = userOptional.get();
            if (!user.getPassword().equals(password)) {
                return LoginResult.INCORRECT_PASSWORD;
            } else {
                return LoginResult.SUCCESS;
            }
        }
    }
}