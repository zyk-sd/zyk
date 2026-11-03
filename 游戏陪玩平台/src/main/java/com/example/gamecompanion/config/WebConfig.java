package com.example.gamecompanion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类：注册登录拦截器，并放行登录页、静态资源、H2 控制台。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",          // 登录页
                        "/css/**",         // 样式
                        "/js/**",          // 脚本
                        "/images/**",      // 图片
                        "/h2-console/**",  // H2 数据库控制台
                        "/error"           // 错误页
                );
    }
}
