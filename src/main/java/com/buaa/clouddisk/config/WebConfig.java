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
                .addPathPatterns("/**") // 拦截所有接口
                // 放行白名单
                .excludePathPatterns(
                        "/api/user/login",      // 登录
                        "/api/user/register",   // 注册
                        "/api/share/info/**",   // 分享信息(游客可看)
                        "/api/share/check",     // 提取码校验
                        "/error",               // 系统错误页
                        "/", "/index.html",     // 静态页面
                        "/**/*.html", "/**/*.css", "/**/*.js", "/**/*.ico", // 静态资源
                        // 在 excludePathPatterns 里追加：
                        "/login",
                        "/register",
                        "/api/user/captcha" // 确保验证码接口也放行
                );
    }
}