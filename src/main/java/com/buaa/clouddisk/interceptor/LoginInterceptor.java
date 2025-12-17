package com.buaa.clouddisk.interceptor;

import com.buaa.clouddisk.common.result.Result;
import com.buaa.clouddisk.common.result.ResultCode;
import com.buaa.clouddisk.common.util.UserContext;
import com.buaa.clouddisk.module.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * 登录检查拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 Session
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");

        // 2. 判断是否登录
        if (userObj != null && userObj instanceof User) {
            // 已登录，将用户信息放入 ThreadLocal，方便 Service 层获取
            UserContext.setUser((User) userObj);
            return true;
        }

        // 3. 未登录，拦截并返回 JSON 格式的 401 错误
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        // 手动序列化 Result 对象返回
        String jsonResult = new ObjectMapper().writeValueAsString(Result.error(ResultCode.UNAUTHORIZED.code, "请先登录系统"));
        writer.print(jsonResult);
        writer.flush();
        writer.close();

        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束，务必清理 ThreadLocal，防止内存泄漏
        UserContext.remove();
    }
}