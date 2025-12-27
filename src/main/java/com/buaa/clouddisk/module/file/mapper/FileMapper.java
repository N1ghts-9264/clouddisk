package com.buaa.clouddisk.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buaa.clouddisk.module.file.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 文件数据库操作接口
 * 继承 BaseMapper<SysFile> 简化开发
 * 
 * 注意：当前未使用此Mapper，FileServiceImpl使用的是SysFileMapper
 * 保留此接口供后续扩展使用
 */
@Mapper
public interface FileMapper extends BaseMapper<SysFile> {
    /** * 检查文件名是否已存在（同级目录下） * @param userId 用户ID * @param parentId 父目录ID * @param filename 文件名 * @return 是否存在 */
    @Select("SELECT COUNT(*) FROM sys_file WHERE user_id = #{userId} AND parent_id = #{parentId} AND filename = #{filename} AND is_deleted = 0")
    boolean existsFileName(@Param("userId") Long userId, @Param("parentId") Long parentId, @Param("filename") String filename);

    /** * 检查目录是否存在 * @param userId 用户ID * @param folderId 目录ID * @return 是否存在 */
    @Select("SELECT COUNT(*) FROM sys_file WHERE user_id = #{userId} AND file_id = #{folderId} AND is_folder = 1 AND is_deleted = 0")
    boolean existsFolder(@Param("userId") Long userId, @Param("folderId") Long folderId);

    /** * 计算用户已使用空间 * @param userId 用户ID * @return 已使用空间大小(字节) */
    @Select("SELECT SUM(file_size) FROM sys_file WHERE user_id = #{userId} AND is_deleted = 0")
    Long sumUsedSizeByUserId(@Param("userId") Long userId);

    /** * 检查文件是否是目标目录的子目录 * @param fileId 文件ID * @param targetParentId 目标目录ID * @return 是否是子目录 */
    @Select("SELECT COUNT(*) FROM sys_file WHERE user_id = (SELECT user_id FROM sys_file WHERE file_id = #{fileId}) " +
            "AND file_id = #{fileId} AND parent_id = #{targetParentId} " +
            "OR file_id = (SELECT parent_id FROM sys_file WHERE file_id = #{fileId}) " +
            "AND parent_id = #{targetParentId} " +
            "OR file_id = (SELECT parent_id FROM sys_file WHERE file_id = (SELECT parent_id FROM sys_file WHERE file_id = #{fileId})))")
    boolean isDescendant(@Param("fileId") Long fileId, @Param("targetParentId") Long targetParentId);
}