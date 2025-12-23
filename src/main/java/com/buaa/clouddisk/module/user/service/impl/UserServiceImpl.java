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
        // 昵称：如果用户填写了昵称就用用户的，否则走默认规则
        if (StringUtils.hasText(registerDTO.getNickname())) {
            user.setNickname(registerDTO.getNickname().trim());
        } else {
            user.setNickname("新用户_" + System.currentTimeMillis() % 10000); // 默认昵称
        }
        user.setTotalSize(1024L * 1024L * 1024L); // 默认 1GB
        user.setUsedSize(0L);
        user.setCreateTime(LocalDateTime.now());

        // 4. 保存到数据库
        save(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public User login(UserLoginDTO loginDTO, HttpSession session) {
        // === 新增验证码校验逻辑 START ===

        // 1. 从 Session 获取生成的验证码
        String sessionCode = (String) session.getAttribute("CAPTCHA_CODE");

        // 2. 校验是否过期
        if (sessionCode == null) {
            throw new RuntimeException("验证码已过期，请刷新重试");
        }

        // 3. 校验是否正确 (忽略大小写)
        if (!sessionCode.equalsIgnoreCase(loginDTO.getCaptcha())) {
            throw new RuntimeException("验证码错误");
        }

        // 4. 验证通过后，立刻移除 Session 中的验证码（防止重复使用）
        session.removeAttribute("CAPTCHA_CODE");

        // === 新增验证码校验逻辑 END ===

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

    @Override
    public void updateNickname(String nickname, HttpSession session) {
        if (!StringUtils.hasText(nickname)) {
            throw new RuntimeException("昵称不能为空");
        }
        nickname = nickname.trim();
        if (nickname.length() > 20) {
            throw new RuntimeException("昵称长度不能超过20个字符");
        }

        Object userObj = session.getAttribute("user");
        if (userObj == null || !(userObj instanceof User)) {
            throw new RuntimeException("未登录或会话已过期");
        }
        User sessionUser = (User) userObj;

        User user = getById(sessionUser.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setNickname(nickname);
        updateById(user);

        // 同步更新 Session 中的用户信息，保证首页显示最新昵称
        sessionUser.setNickname(nickname);
        session.setAttribute("user", sessionUser);

        log.info("用户昵称修改成功: {} -> {}", user.getUsername(), nickname);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword, HttpSession session) {
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new RuntimeException("原密码和新密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 32) {
            throw new RuntimeException("新密码长度需在6-32位之间");
        }

        Object userObj = session.getAttribute("user");
        if (userObj == null || !(userObj instanceof User)) {
            throw new RuntimeException("未登录或会话已过期");
        }
        User sessionUser = (User) userObj;

        User user = getById(sessionUser.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String oldMd5 = Md5Util.encrypt(oldPassword);
        if (!oldMd5.equals(user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }

        String newMd5 = Md5Util.encrypt(newPassword);
        if (newMd5.equals(user.getPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        user.setPassword(newMd5);
        updateById(user);

        // 出于安全考虑，修改密码后强制用户重新登录
        session.invalidate();

        log.info("用户密码修改成功: {}", user.getUsername());
    }

    @Override
    public boolean isUsernameTaken(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username.trim());
        return count(queryWrapper) > 0;
    }
}