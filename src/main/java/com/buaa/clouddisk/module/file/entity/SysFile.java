package com.buaa.clouddisk.module.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_file")
public class SysFile {
    /** * 1. 修正主键自增逻辑 * value 指定数据库字段名，type = IdType.AUTO 告诉 MyBatis-Plus 别手动插这个字段，让数据库自增 */
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    private Long userId;
    private Long parentId;
    private String filename;

    /** * 2. 确保 BIT 类型对应 Boolean * SQL Server 的 BIT 字段 (1/0) 在 Java 中最佳实践是 Boolean (true/false) */
    private Boolean isFolder;

    private Long fileSize;
    private String fileType;
    private String filePath;
    private String identifier;

    /** * 3. 修正逻辑删除字段类型 * 数据库设计中 is_deleted 是 BIT，这里应改回 Boolean 确保 MyBatis-Plus 正确识别 */
    private Boolean isDeleted;

    /** * 4. 时间字段处理 * 使用 fill 属性可以让 MyBatis-Plus 在插入/更新时自动填充时间（需配置 MetaObjectHandler， * 如果没配也没关系，我们先手动在 Service 里赋值也可以） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}