package com.buaa.clouddisk.module.user.dto;
import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    // 新增：注册时允许用户填写昵称（可重复，可为空）
    private String nickname;
}