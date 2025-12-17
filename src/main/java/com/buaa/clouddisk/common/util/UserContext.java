package com.buaa.clouddisk.common.util;

import com.buaa.clouddisk.module.user.entity.User;

/**
 * 用户上下文工具类 (基于 ThreadLocal)
 * 作用：在一次请求的任何地方获取当前登录用户信息，无需传递 Session
 */
public class UserContext {
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();

    public static void setUser(User user) {
        userHolder.set(user);
    }

    public static User getUser() {
        return userHolder.get();
    }

    /**
     * 获取当前登录用户的ID
     * 给 Role B/C/D 的组员使用：UserContext.getUserId()
     */
    public static Long getUserId() {
        User user = getUser();
        return user != null ? user.getUserId() : null;
    }

    public static void remove() {
        userHolder.remove();
    }
}