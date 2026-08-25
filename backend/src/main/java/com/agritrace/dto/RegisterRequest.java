package com.agritrace.dto;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // USER, FARMER, LOGS_ADMIN
    private String realName;
    private String phone;
}
