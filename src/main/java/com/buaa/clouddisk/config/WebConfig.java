package com.buaa.clouddisk.config;

import com.buaa.clouddisk.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**") // 拦截所有
                // 放行白名单
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/check-username",
                        "/api/share/info/**",
                        "/api/share/check",
                        "/error",
                        "/", "/index.html",

                        // === 核心修复点 START ===
                        "/images/**",       // 放行 images 目录下所有文件
                        "/**/*.png",        // 放行所有 png 图片
                        "/**/*.jpg",        // 放行所有 jpg 图片
                        "/**/*.jpeg",       // 放行所有 jpeg 图片
                        "/**/*.gif",        // 放行所有 gif 图片
                        // === 核心修复点 END ===
                        // === 新增放行本地静态资源 ===
                        "/css/**",
                        "/js/**",
                        // ========================

                        "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.ico",
                        "/login",
                        "/register",
                        "/api/user/captcha"
                );
    }
}