package com.buaa.clouddisk.module.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.buaa.clouddisk.module.share.dto.ShareCheckDTO;
import com.buaa.clouddisk.module.share.dto.ShareCreateDTO;
import com.buaa.clouddisk.module.share.dto.ShareInfoVO;
import com.buaa.clouddisk.module.share.entity.Share;

public interface IShareService extends IService<Share> {
    // 创建分享
    Share createShare(ShareCreateDTO createDTO);

    // 获取分享基础信息（无需提取码，用于展示文件名等）
    ShareInfoVO getShareInfo(Long shareId);

    // 校验提取码
    String checkShareCode(ShareCheckDTO checkDTO);

    // 列出当前用户的所有分享（历史分享）
    java.util.List<com.buaa.clouddisk.module.share.dto.ShareInfoVO> listMyShares();

    // 删除当前用户的分享（软删除）
    void deleteShare(Long shareId);
}