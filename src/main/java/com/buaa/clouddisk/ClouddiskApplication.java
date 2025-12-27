package com.buaa.clouddisk;

import com.buaa.clouddisk.module.file.mapper.SysFileMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.buaa.clouddisk.module")
public class ClouddiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClouddiskApplication.class, args);
    }

    @Bean
    public CommandLineRunner testSysFileMapper(SysFileMapper sysFileMapper) {
        return args -> {
            System.out.println("====== 启动时测试查询 sys_file 表 ======");
            sysFileMapper.selectList(null)
                    .forEach(System.out::println);
        };
    }
}
