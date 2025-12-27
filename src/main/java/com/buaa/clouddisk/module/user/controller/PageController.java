package com.buaa.clouddisk.module.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 注意：这里是 Controller，不是 RestController
public class PageController {

    // 跳转登录页
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // 对应 resources/templates/login.html
    }


    // 跳转注册页
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // 对应 resources/templates/register.html
    }

    // 跳转首页 (需要登录才能看)
    @GetMapping({"/", "/index"})
    public String indexPage() {
        return "index"; // 对应 resources/templates/index.html
    }

    // 跳转个人中心页
    @GetMapping("/profile")
    public String profilePage() {
        return "profile"; // 对应 resources/templates/profile.html
    }

    // 访客访问分享页（示例：/share/123）
    @GetMapping("/share/{shareId}")
    public String sharePage() {
        return "share"; // 对应 resources/templates/share.html
    }
    @GetMapping("/test_upload")
    public String testUploadPage() {
        return "test_upload"; // 对应 resources/templates/test_upload.html
    }
}