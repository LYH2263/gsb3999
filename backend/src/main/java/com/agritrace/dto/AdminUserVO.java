package com.agritrace.dto;

import com.agritrace.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String role;
    private Integer enabled;
    private String realName;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserVO from(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setEnabled(user.getEnabled());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
