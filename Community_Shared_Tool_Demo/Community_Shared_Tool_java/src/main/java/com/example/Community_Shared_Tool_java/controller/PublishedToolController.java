package com.example.Community_Shared_Tool_java.controller;

import com.example.Community_Shared_Tool_java.entity.PublishedTool;
import com.example.Community_Shared_Tool_java.service.PublishedToolService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/published-tools")
public class PublishedToolController {

    @Autowired
    private PublishedToolService publishedToolService;

    // 发布新工具
    @PostMapping
    public ResponseEntity<Map<String, Object>> publishTool(@Valid @RequestBody PublishedTool tool, BindingResult bindingResult) {
        try {
            // 🔹 修复：处理验证错误
            if (bindingResult.hasErrors()) {
                Map<String, Object> response = new HashMap<>();
                StringBuilder errorMsg = new StringBuilder();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errorMsg.append(error.getDefaultMessage()).append("\n");
                }
                response.put("success", false);
                response.put("message", errorMsg.toString());
                return ResponseEntity.badRequest().body(response);
            }
            
            tool.setStatus("available"); // 默认状态为可借用
            PublishedTool publishedTool = publishedToolService.publishTool(tool);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", publishedTool);
            response.put("message", "工具发布成功");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发布工具失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 获取用户发布的所有工具
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Map<String, Object>> getToolsByOwnerId(@PathVariable Integer ownerId) {
        try {
            List<PublishedTool> tools = publishedToolService.getToolsByOwnerId(ownerId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tools);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取工具列表失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 根据ID获取工具
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getToolById(@PathVariable Integer id) {
        try {
            Optional<PublishedTool> tool = publishedToolService.getToolById(id);
            Map<String, Object> response = new HashMap<>();
        if (tool.isPresent()) {
            response.put("success", true);
            response.put("data", tool.get());
            response.put("message", "工具获取成功");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response.put("success", false);
            response.put("message", "工具不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取工具详情失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 更新工具信息
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTool(@PathVariable Integer id, @Valid @RequestBody PublishedTool tool, @RequestHeader("X-User-Id") Integer userId, BindingResult bindingResult) {
        try {
            // 🔹 修复：处理验证错误
            if (bindingResult.hasErrors()) {
                Map<String, Object> response = new HashMap<>();
                StringBuilder errorMsg = new StringBuilder();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errorMsg.append(error.getDefaultMessage()).append("\n");
                }
                response.put("success", false);
                response.put("message", errorMsg.toString());
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查权限：用户只能编辑自己发布的工具
            Optional<PublishedTool> existingTool = publishedToolService.getToolById(id);
            Map<String, Object> response = new HashMap<>();
            if (existingTool.isEmpty()) {
                response.put("success", false);
                response.put("message", "工具不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!existingTool.get().getOwnerId().equals(userId)) {
                response.put("success", false);
                response.put("message", "您没有权限编辑此工具！");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 确保ID一致
            tool.setId(id);
            PublishedTool updatedTool = publishedToolService.updateTool(tool);
            response.put("success", true);
            response.put("data", updatedTool);
            response.put("message", "工具更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新工具失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 更新工具状态
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateToolStatus(@PathVariable Integer id, @RequestParam String status, @RequestHeader("X-User-Id") Integer userId) {
        try {
            // 检查权限：用户只能修改自己发布的工具
            Optional<PublishedTool> existingTool = publishedToolService.getToolById(id);
            Map<String, Object> response = new HashMap<>();
            if (existingTool.isEmpty()) {
                response.put("success", false);
                response.put("message", "工具不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!existingTool.get().getOwnerId().equals(userId)) {
                response.put("success", false);
                response.put("message", "您没有权限修改此工具的状态！");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            PublishedTool updatedTool = publishedToolService.updateToolStatus(id, status);
            response.put("success", true);
            response.put("data", updatedTool);
            response.put("message", "工具状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新工具状态失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 删除工具
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTool(@PathVariable Integer id, @RequestHeader("X-User-Id") Integer userId) {
        try {
            // 检查权限：用户只能删除自己发布的工具
            Optional<PublishedTool> existingTool = publishedToolService.getToolById(id);
            Map<String, Object> response = new HashMap<>();
            if (existingTool.isEmpty()) {
                response.put("success", false);
                response.put("message", "工具不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!existingTool.get().getOwnerId().equals(userId)) {
                response.put("success", false);
                response.put("message", "您没有权限删除此工具！");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            publishedToolService.deleteTool(id);
            response.put("success", true);
            response.put("message", "工具删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除工具失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 搜索工具
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTools(
            @RequestParam(required = false) Integer ownerId,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String status) {
        try {
            List<PublishedTool> tools = publishedToolService.searchTools(ownerId, toolName, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tools);
            response.put("message", "搜索工具成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索工具失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}