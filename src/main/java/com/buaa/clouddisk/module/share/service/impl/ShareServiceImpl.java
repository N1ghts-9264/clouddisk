package com.buaa.clouddisk.module.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.buaa.clouddisk.common.util.UserContext;
import com.buaa.clouddisk.module.file.entity.File;
import com.buaa.clouddisk.module.file.mapper.FileMapper;
import com.buaa.clouddisk.module.share.dto.ShareCheckDTO;
import com.buaa.clouddisk.module.share.dto.ShareCreateDTO;
import com.buaa.clouddisk.module.share.dto.ShareInfoVO;
import com.buaa.clouddisk.module.share.entity.Share;
import com.buaa.clouddisk.module.share.mapper.ShareMapper;
import com.buaa.clouddisk.module.share.service.IShareService;
import com.buaa.clouddisk.module.user.entity.User;
import com.buaa.clouddisk.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements IShareService {

    private final FileMapper fileMapper; // 需要查询文件是否存在
    private final UserMapper userMapper; // 需要查询分享者名字

    @Override
    public Share createShare(ShareCreateDTO createDTO) {
        Long userId = UserContext.getUserId();

        // 1. 校验文件归属
        File file = fileMapper.selectById(createDTO.getFileId());
        if (file == null || !file.getUserId().equals(userId)) {
            throw new RuntimeException("文件不存在或无权分享");
        }

        // 2. 生成随机4位提取码 (数字+字母)
        String code = generateRandomCode(4);

        // 3. 构建对象
        Share share = new Share();
        share.setFileId(createDTO.getFileId());
        share.setUserId(userId);
        share.setCode(code);
        share.setCreateTime(LocalDateTime.now());
        share.setVisitCount(0);
        share.setStatus(0); // 正常

        // 4. 设置过期时间
        if (createDTO.getValidDays() != null && createDTO.getValidDays() > 0) {
            share.setExpireTime(LocalDateTime.now().plusDays(createDTO.getValidDays()));
        } else {
            share.setExpireTime(null); // 永久有效
        }

        this.save(share);
        return share;
    }

    @Override
    public ShareInfoVO getShareInfo(Long shareId) {
        // 1. 获取分享记录
        Share share = this.getById(shareId);
        if (share == null || share.getStatus() == 1) {
            throw new RuntimeException("分享链接不存在或已失效");
        }

        // 2. 检查是否过期
        boolean isExpired = false;
        if (share.getExpireTime() != null && LocalDateTime.now().isAfter(share.getExpireTime())) {
            isExpired = true;
            // 可以在此逻辑删除分享，或者仅标记前端展示
        }

        // 3. 获取文件详情
        File file = fileMapper.selectById(share.getFileId());
        User user = userMapper.selectById(share.getUserId());

        // 4. 组装VO
        ShareInfoVO vo = new ShareInfoVO();
        vo.setShareId(share.getShareId());
        vo.setFileId(share.getFileId());
        vo.setFilename(file != null ? file.getFilename() : "未知文件");
        vo.setFileSize(file != null ? file.getFileSize() : 0L);
        vo.setShareUser(user != null ? user.getNickname() : "神秘用户");
        vo.setExpired(isExpired);

        // 更新浏览次数
        share.setVisitCount(share.getVisitCount() + 1);
        this.updateById(share);

        return vo;
    }

    @Override
    public String checkShareCode(ShareCheckDTO checkDTO) {
        Share share = this.getById(checkDTO.getShareId());
        if (share == null) {
            throw new RuntimeException("分享不存在");
        }

        // 1. 校验过期
        if (share.getExpireTime() != null && LocalDateTime.now().isAfter(share.getExpireTime())) {
            throw new RuntimeException("该分享已过期");
        }

        // 2. 校验提取码 (忽略大小写)
        if (!share.getCode().equalsIgnoreCase(checkDTO.getCode())) {
            throw new RuntimeException("提取码错误");
        }

        // 3. 返回一个临时令牌 (这里简单处理，返回 "OK" 或者 UUID，前端下载时带上)
        // 实际项目中可以生成一个 Redis Token，这里简化处理
        return UUID.randomUUID().toString();
    }

    @Override
    public List<ShareInfoVO> listMyShares() {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Share> qw = new LambdaQueryWrapper<>();
        qw.eq(Share::getUserId, userId).eq(Share::getStatus, 0).orderByDesc(Share::getCreateTime);
        List<Share> shares = this.list(qw);
        return shares.stream().map(share -> {
            File file = fileMapper.selectById(share.getFileId());
            User user = userMapper.selectById(share.getUserId());
            ShareInfoVO vo = new ShareInfoVO();
            vo.setShareId(share.getShareId());
            vo.setFileId(share.getFileId());
            vo.setFilename(file != null ? file.getFilename() : "未知文件");
            vo.setFileSize(file != null ? file.getFileSize() : 0L);
            vo.setShareUser(user != null ? user.getNickname() : "我");
            boolean isExpired = share.getExpireTime() != null && LocalDateTime.now().isAfter(share.getExpireTime());
            vo.setExpired(isExpired);
            return vo;
        }).collect(Collectors.toList());
    }

    // 辅助方法：生成随机码
    private String generateRandomCode(int length) {
        String str = "abcdefghjkmnpqrstuvwxyz23456789"; // 去掉容易混淆的 i,l,1,o,0
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(str.length());
            sb.append(str.charAt(index));
        }
        return sb.toString();
    }

    @Override
    public void deleteShare(Long shareId) {
        Share share = this.getById(shareId);
        if (share == null) {
            throw new RuntimeException("分享不存在或已被删除");
        }
        Long userId = UserContext.getUserId();
        if (!share.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该分享");
        }
        // 软删除：标记 status 为 1
        share.setStatus(1);
        this.updateById(share);
    }
}