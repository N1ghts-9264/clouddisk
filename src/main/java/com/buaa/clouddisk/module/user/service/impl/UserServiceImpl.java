package com.buaa.clouddisk.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buaa.clouddisk.common.result.Result;
import com.buaa.clouddisk.common.util.Md5Util;
import com.buaa.clouddisk.module.user.dto.UserLoginDTO;
import com.buaa.clouddisk.module.user.dto.UserRegisterDTO;
import com.buaa.clouddisk.module.user.entity.User;
import com.buaa.clouddisk.module.user.mapper.UserMapper;
import com.buaa.clouddisk.module.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public void register(UserRegisterDTO registerDTO) {
        // 1. 校验参数
        if (!StringUtils.hasText(registerDTO.getUsername()) || !StringUtils.hasText(registerDTO.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        // 2. 校验用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("该用户名已被注册");
        }

        // 3. 构建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 密码加密存储
        user.setPassword(Md5Util.encrypt(registerDTO.getPassword()));
        user.setNickname("新用户_" + System.currentTimeMillis() % 10000); // 默认昵称
        user.setTotalSize(1024L * 1024L * 1024L); // 默认 1GB
        user.setUsedSize(0L);
        user.setCreateTime(LocalDateTime.now());

        // 4. 保存到数据库
        save(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public User login(UserLoginDTO loginDTO, HttpSession session) {
        // 1. 根据用户名查询
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = getOne(queryWrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 校验密码 (将用户输入的密码加密后与数据库比对)
        String inputMd5 = Md5Util.encrypt(loginDTO.getPassword());
        if (!inputMd5.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 4. 登录成功，保存 Session
        // 注意：敏感信息如密码，最好在放入 Session 前置空，或者只放 safe 的对象
        user.setPassword(null);
        session.setAttribute("user", user);

        return user;
    }

    @Override
    public void logout(HttpSession session) {
        session.invalidate(); // 销毁 Session
    }
}