package com.agritrace.controller;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.agritrace.config.JwtUtils;
import com.agritrace.dto.LoginRequest;
import com.agritrace.dto.Result;
import com.agritrace.entity.User;
import com.agritrace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest req) {
        Optional<User> opt = userRepository.findByUsername(req.getUsername());
        if (opt.isPresent()) {
            User user = opt.get();
            BCrypt.Result result = BCrypt.verifyer().verify(req.getPassword().toCharArray(), user.getPassword());
            if (result.verified) {
                if (!Integer.valueOf(1).equals(user.getEnabled())) {
                    return Result.error(403, "账号已被禁用，请联系系统管理员");
                }
                String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("role", user.getRole());
                data.put("username", user.getUsername());
                data.put("realName", user.getRealName());
                return Result.success(data);
            }
        }
        return Result.error(401, "用户名或密码错误");
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody com.agritrace.dto.RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return Result.error(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        // default to USER if not specified or invalid
        if (req.getRole() == null || !req.getRole().matches("^(USER|FARMER|LOGS_ADMIN)$")) {
            user.setRole("USER"); 
        } else {
            user.setRole(req.getRole());
        }
        user.setPassword(BCrypt.withDefaults().hashToString(10, req.getPassword().toCharArray()));
        user.setRealName(req.getRealName());
        user.setPhone(req.getPhone());
        user.setEnabled(1);
        userRepository.save(user);
        return Result.success("注册成功");
    }
}
