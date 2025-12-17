package com.buaa.clouddisk.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buaa.clouddisk.module.user.dto.UserLoginDTO;
import com.buaa.clouddisk.module.user.dto.UserRegisterDTO;
import com.buaa.clouddisk.module.user.entity.User;
import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {
    void register(UserRegisterDTO registerDTO);
    User login(UserLoginDTO loginDTO, HttpSession session);
    void logout(HttpSession session);
}