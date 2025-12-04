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
    public ResponseEntity<?> publishTool(@Valid @RequestBody PublishedTool tool, BindingResult bindingResult) {
        try {
            // 🔹 修复：处理验证错误
            if (bindingResult.hasErrors()) {
                StringBuilder errorMsg = new StringBuilder();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errorMsg.append(error.getDefaultMessage()).append("\n");
                }
                return ResponseEntity.badRequest().body(errorMsg.toString());
            }
            
            tool.setStatus("available"); // 默认状态为可借用
            PublishedTool publishedTool = publishedToolService.publishTool(tool);
            return new ResponseEntity<>(publishedTool, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("发布工具失败：" + e.getMessage());
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
    public ResponseEntity<PublishedTool> getToolById(@PathVariable Integer id) {
        try {
            Optional<PublishedTool> tool = publishedToolService.getToolById(id);
            return tool.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 更新工具信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTool(@PathVariable Integer id, @Valid @RequestBody PublishedTool tool, @RequestHeader("X-User-Id") Integer userId, BindingResult bindingResult) {
        try {
            // 🔹 修复：处理验证错误
            if (bindingResult.hasErrors()) {
                StringBuilder errorMsg = new StringBuilder();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errorMsg.append(error.getDefaultMessage()).append("\n");
                }
                return ResponseEntity.badRequest().body(errorMsg.toString());
            }
            
            // 检查权限：用户只能编辑自己发布的工具
            Optional<PublishedTool> existingTool = publishedToolService.getToolById(id);
            if (existingTool.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!existingTool.get().getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您没有权限编辑此工具！");
            }

            // 确保ID一致
            tool.setId(id);
            PublishedTool updatedTool = publishedToolService.updateTool(tool);
            return ResponseEntity.ok(updatedTool);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新工具失败：" + e.getMessage());
        }
    }

    // 更新工具状态
    @PatchMapping("/{id}/status")
    public ResponseEntity<PublishedTool> updateToolStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            PublishedTool updatedTool = publishedToolService.updateToolStatus(id, status);
            return ResponseEntity.ok(updatedTool);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 删除工具
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTool(@PathVariable Integer id, @RequestHeader("X-User-Id") Integer userId) {
        try {
            // 检查权限：用户只能删除自己发布的工具
            Optional<PublishedTool> existingTool = publishedToolService.getToolById(id);
            if (existingTool.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (!existingTool.get().getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            publishedToolService.deleteTool(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 搜索工具
    @GetMapping("/search")
    public ResponseEntity<List<PublishedTool>> searchTools(
            @RequestParam(required = false) Integer ownerId,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String status) {
        try {
            List<PublishedTool> tools = publishedToolService.searchTools(ownerId, toolName, status);
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}