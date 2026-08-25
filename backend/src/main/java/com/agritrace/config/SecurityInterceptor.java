package com.agritrace.config;

import com.agritrace.entity.User;
import com.agritrace.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) return true;
        
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register") || path.startsWith("/api/public") || path.startsWith("/api/trace/query")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtils.parseToken(token.substring(7));
                Long userId = ((Number) claims.get("userId")).longValue();
                User currentUser = userRepository.findById(userId).orElse(null);
                if (currentUser == null || !Integer.valueOf(1).equals(currentUser.getEnabled())) {
                    response.setStatus(401);
                    return false;
                }

                request.setAttribute("userId", userId);
                request.setAttribute("role", currentUser.getRole());
                
                String role = currentUser.getRole();
                if (path.startsWith("/api/farmer") && !("FARMER".equals(role) || "SYS_ADMIN".equals(role))) {
                    response.setStatus(403);
                    return false;
                }
                // 农事档案模块：物流管理员与消费者一律禁止进入
                if (path.startsWith("/api/farming") && !("FARMER".equals(role) || "SYS_ADMIN".equals(role))) {
                    response.setStatus(403);
                    return false;
                }
                if (path.startsWith("/api/logistics") && !("LOGS_ADMIN".equals(role) || "SYS_ADMIN".equals(role))) {
                    response.setStatus(403);
                    return false;
                }
                if (path.startsWith("/api/admin") && !"SYS_ADMIN".equals(role)) {
                    response.setStatus(403);
                    return false;
                }
                
                return true;
            } catch (Exception e) {
                response.setStatus(401);
                return false;
            }
        }
        response.setStatus(401);
        return false;
    }
}
