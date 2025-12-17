package com.buaa.clouddisk.module.user.dto;
import lombok.Data;

@Data
public class UserLoginDTO {
    private String username;
    private String password;
    // 后续可扩展 private String captcha;
    private String captcha;
}