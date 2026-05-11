package com.fruitshop.interceptor;

import com.fruitshop.common.JwtUtil;
import com.fruitshop.common.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 鉴权入口：从 Authorization: Bearer <token> 解析身份，
        // 并写入 ThreadLocal(UserContext) 供后续业务层读取。
        // 每次请求前先清除UserContext，确保不会使用上一次请求的数据
        UserContext.clear();
        
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                if (!jwtUtil.isTokenExpired(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    // 验证userId和role是否有效
                    if (userId != null && userId > 0 && role != null) {
                        UserContext.setUserId(userId);
                        UserContext.setRole(role);
                    }
                }
            } catch (Exception e) {
                // Token无效，确保UserContext为空
                UserContext.clear();
            }
        } else {
            // 没有token，确保UserContext为空
            UserContext.clear();
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}

