package com.buaa.clouddisk.config;

import com.buaa.clouddisk.module.user.entity.User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpSession;

/**
 * 全局 Model 属性注入
 * 自动为所有使用 @Controller 的页面注入用户信息
 */
@ControllerAdvice(basePackages = "com.buaa.clouddisk.module.user.controller")
public class GlobalModelAttributeAdvice {

    @ModelAttribute("user")
    public User addUserToModel(HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            return (User) userObj;
        }
        // 未登录时返回默认用户对象（防止模板报错）
        User guest = new User();
        guest.setNickname("游客");
        guest.setUsername("guest");
        return guest;
    }
}
