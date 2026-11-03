package com.example.gamecompanion.config;

import com.example.gamecompanion.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器。
 * 除登录页和静态资源外，所有请求都必须先登录，否则跳转到 /login。
 * 登录成功后，用户对象会以 "loginUser" 为键保存在 Session 中。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            // 未登录：重定向到登录页
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
