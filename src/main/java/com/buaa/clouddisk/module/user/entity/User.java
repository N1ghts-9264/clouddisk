package com.buaa.clouddisk.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user") // 必须指定表名，SQL Server 关键字 user 不可用
public class User {

    @TableId(type = IdType.AUTO) // SQL Server 自增主键
    private Long userId;

    private String username;

    // 序列化时忽略密码，防止返回给前端
    private String password;

    private String nickname;

    private Long totalSize;

    private Long usedSize;

    private LocalDateTime createTime;
}