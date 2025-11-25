package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.dto.LoginRequest;
import com.example.Community_Shared_Tool_java.dto.RegisterRequest;
import com.example.Community_Shared_Tool_java.service.LoginService;
import com.example.Community_Shared_Tool_java.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private RegisterService registerService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        LoginService.LoginResult result = loginService.login(loginRequest.getUsername(), loginRequest.getPassword());
        Map<String, Object> response = new HashMap<>();

        if (result == LoginService.LoginResult.SUCCESS) {
            response.put("success", true);
            response.put("token", "dummy-token-" + loginRequest.getUsername()); // 示例 token
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            switch (result) {
                case USERNAME_NOT_FOUND:
                    response.put("message", "用户名未找到");
                    break;
                case INCORRECT_PASSWORD:
                    response.put("message", "密码错误");
                    break;
                default:
                    response.put("message", "登录失败");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest registerRequest) {
        RegisterService.RegisterResult result = registerService.registerUser(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getBasicInfo());
        Map<String, Object> response = new HashMap<>();

        if (result == RegisterService.RegisterResult.SUCCESS) {
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            switch (result) {
                case USERNAME_EXISTS:
                    response.put("message", "用户名已存在");
                    break;
                case INVALID_DATA:
                    response.put("message", "无效的数据");
                    break;
                default:
                    response.put("message", "注册失败");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}