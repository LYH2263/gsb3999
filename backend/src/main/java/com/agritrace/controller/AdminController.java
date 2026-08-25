package com.agritrace.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.agritrace.dto.AdminCreateUserRequest;
import com.agritrace.dto.AdminUpdateUserRoleRequest;
import com.agritrace.dto.AdminUpdateUserStatusRequest;
import com.agritrace.dto.AdminUserVO;
import com.agritrace.dto.Result;
import com.agritrace.entity.User;
import com.agritrace.repository.LogisticsRepository;
import com.agritrace.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Set<String> ROLE_SET = Set.of("USER", "FARMER", "LOGS_ADMIN", "SYS_ADMIN");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogisticsRepository logisticsRepository;

    @GetMapping("/users")
    public Result<List<AdminUserVO>> listUsers() {
        List<AdminUserVO> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(AdminUserVO::from)
                .collect(Collectors.toList());
        return Result.success(users);
    }

    @PostMapping("/users")
    public Result<?> createUser(@RequestBody AdminCreateUserRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (req.getRole() == null || !ROLE_SET.contains(req.getRole())) {
            return Result.error(400, "角色不合法");
        }
        if (userRepository.findByUsername(req.getUsername().trim()).isPresent()) {
            return Result.error(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPassword(BCrypt.withDefaults().hashToString(10, req.getPassword().toCharArray()));
        user.setRole(req.getRole());
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEnabled(1);
        userRepository.save(user);
        return Result.success(AdminUserVO.from(user));
    }

    @PutMapping("/users/{id}/role")
    public Result<?> updateUserRole(HttpServletRequest request, @PathVariable Long id, @RequestBody AdminUpdateUserRoleRequest req) {
        if (req.getRole() == null || !ROLE_SET.contains(req.getRole())) {
            return Result.error(400, "角色不合法");
        }

        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser == null) {
            return Result.error(404, "用户不存在");
        }
        if (isProtectedAdmin(targetUser)) {
            return Result.error(400, "admin 为系统保留账号，不允许修改");
        }

        Long currentUserId = ((Number) request.getAttribute("userId")).longValue();
        if (id.equals(currentUserId) && !"SYS_ADMIN".equals(req.getRole())) {
            return Result.error(400, "不能降低当前登录账号的系统管理员权限");
        }

        targetUser.setRole(req.getRole());
        userRepository.save(targetUser);
        return Result.success(AdminUserVO.from(targetUser));
    }

    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(HttpServletRequest request, @PathVariable Long id, @RequestBody AdminUpdateUserStatusRequest req) {
        if (req.getEnabled() == null || (req.getEnabled() != 0 && req.getEnabled() != 1)) {
            return Result.error(400, "enabled 仅支持 0 或 1");
        }

        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser == null) {
            return Result.error(404, "用户不存在");
        }
        if (isProtectedAdmin(targetUser)) {
            return Result.error(400, "admin 为系统保留账号，不允许修改");
        }

        Long currentUserId = ((Number) request.getAttribute("userId")).longValue();
        if (id.equals(currentUserId) && req.getEnabled() == 0) {
            return Result.error(400, "不能禁用当前登录账号");
        }

        targetUser.setEnabled(req.getEnabled());
        userRepository.save(targetUser);
        return Result.success(AdminUserVO.from(targetUser));
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(HttpServletRequest request, @PathVariable Long id) {
        User targetUser = userRepository.findById(id).orElse(null);
        if (targetUser == null) {
            return Result.error(404, "用户不存在");
        }
        if (isProtectedAdmin(targetUser)) {
            return Result.error(400, "admin 为系统保留账号，不允许修改");
        }

        Long currentUserId = ((Number) request.getAttribute("userId")).longValue();
        if (id.equals(currentUserId)) {
            return Result.error(400, "不能删除当前登录账号");
        }
        if ("SYS_ADMIN".equals(targetUser.getRole()) && Integer.valueOf(1).equals(targetUser.getEnabled())) {
            long adminCount = userRepository.countByRoleAndEnabled("SYS_ADMIN", 1);
            if (adminCount <= 1) {
                return Result.error(400, "系统至少需要保留一个启用状态的系统管理员");
            }
        }

        if (logisticsRepository.existsByLogisticsAdminId(id)) {
            return Result.error(400, "该用户存在物流操作记录，不能删除");
        }

        try {
            userRepository.delete(targetUser);
            return Result.success("删除成功");
        } catch (DataIntegrityViolationException ex) {
            return Result.error(400, "该用户已关联业务数据，不能删除");
        }
    }

    private boolean isProtectedAdmin(User user) {
        if (user == null || user.getUsername() == null) {
            return false;
        }
        return "admin".equalsIgnoreCase(user.getUsername().trim());
    }
}
