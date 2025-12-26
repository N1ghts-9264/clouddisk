package com.buaa.clouddisk.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa.clouddisk.module.file.entity.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<File> {
}
