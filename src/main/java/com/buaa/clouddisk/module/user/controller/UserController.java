package com.buaa.clouddisk.module.user.controller;

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
}