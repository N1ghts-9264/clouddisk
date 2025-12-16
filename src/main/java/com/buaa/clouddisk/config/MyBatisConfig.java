package com.buaa.clouddisk.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
// 扫描所有模块下的 mapper
@MapperScan("com.buaa.clouddisk.module.**.mapper")
public class MyBatisConfig {
}