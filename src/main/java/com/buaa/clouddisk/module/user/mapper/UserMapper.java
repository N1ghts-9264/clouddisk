package com.buaa.clouddisk.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa.clouddisk.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus 已经提供了基本的 CRUD，无需手写 SQL
}