package com.buaa.clouddisk.module.share.controller;

import com.buaa.clouddisk.common.result.Result;
import com.buaa.clouddisk.module.share.dto.ShareCheckDTO;
import com.buaa.clouddisk.module.share.dto.ShareCreateDTO;
import com.buaa.clouddisk.module.share.dto.ShareInfoVO;
import com.buaa.clouddisk.module.share.entity.Share;
import com.buaa.clouddisk.module.share.service.IShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final IShareService shareService;

    /**
     * 创建分享链接
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createShare(@RequestBody ShareCreateDTO createDTO) {
        Share share = shareService.createShare(createDTO);

        Map<String, Object> data = new HashMap<>();
        data.put("shareId", share.getShareId());
        data.put("code", share.getCode());
        data.put("expireTime", share.getExpireTime());
        // 前端可以根据 shareId 拼接完整 URL: http://domain/share/{shareId}
        return Result.success(data);
    }

    /**
     * 获取分享基本信息 (游客打开链接时调用)
     */
    @GetMapping("/info/{shareId}")
    public Result<ShareInfoVO> getShareInfo(@PathVariable Long shareId) {
        // 此接口在 Interceptor 白名单中，无需登录
        return Result.success(shareService.getShareInfo(shareId));
    }

    @GetMapping("/my")
    public Result<java.util.List<ShareInfoVO>> myShares() {
        // 返回当前登录用户的所有分享
        return Result.success(shareService.listMyShares());
    }

    /**
     * 校验提取码
     */
    @PostMapping("/check")
    public Result<String> checkShare(@RequestBody ShareCheckDTO checkDTO) {
        // 校验通过返回 Token
        String token = shareService.checkShareCode(checkDTO);
        return Result.success(token);
    }

    /**
     * 删除分享（仅允许分享创建者删除，软删除）
     */
    @DeleteMapping("/{shareId}")
    public Result<?> deleteShare(@PathVariable Long shareId) {
        shareService.deleteShare(shareId);
        return Result.success(null);
    }
}