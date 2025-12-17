package com.buaa.clouddisk.module.user.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.buaa.clouddisk.common.result.Result;
import com.buaa.clouddisk.common.util.UserContext;
import com.buaa.clouddisk.module.user.dto.UserLoginDTO;
import com.buaa.clouddisk.module.user.dto.UserRegisterDTO;
import com.buaa.clouddisk.module.user.entity.User;
import com.buaa.clouddisk.module.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor // Lombok 自动生成构造函数注入 Service
public class UserController {

    private final IUserService userService;
    private final DefaultKaptcha defaultKaptcha;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO registerDTO) {
        try {
            userService.register(registerDTO);
            return Result.success("注册成功，请登录");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody UserLoginDTO loginDTO, HttpSession session) {
        try {
            User user = userService.login(loginDTO, session);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     * 从 ThreadLocal (UserContext) 中获取，由拦截器自动注入
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        User user = UserContext.getUser();
        if (user == null) {
            return Result.error(401, "未登录或会话已过期");
        }
        // 刷新一下最新的空间使用情况（可选，这里直接返回 Session 中的缓存数据）
        return Result.success(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        userService.logout(session);
        return Result.success("退出成功");
    }

    /**
     * 获取图形验证码
     * 注意：这个接口不返回 JSON，而是直接返回图片流
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        // 1. 生成 4 位随机文本 (例如 "x9z1")
        String text = defaultKaptcha.createText();

        // 2. 存入 Session，用于后续登录校验 (Key = "CAPTCHA_CODE")
        session.setAttribute("CAPTCHA_CODE", text);
        log.info("生成验证码: {}", text);

        // 3. 生成图片
        BufferedImage image = defaultKaptcha.createImage(text);

        // 4. 设置响应头，告诉浏览器这是一张图片，不要缓存
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        // 5. 输出图片流
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        out.flush();
    }
}