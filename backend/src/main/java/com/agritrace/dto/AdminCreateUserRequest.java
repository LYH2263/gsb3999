package com.agritrace.dto;

import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String username;
    private String password;
    private String role;
    private String realName;
    private String phone;
}
