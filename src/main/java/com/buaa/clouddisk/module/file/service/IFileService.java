package com.buaa.clouddisk.module.file.service;

import com.buaa.clouddisk.module.file.vo.FileVO;
import com.buaa.clouddisk.module.file.vo.SpaceInfo;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    Long uploadFile(MultipartFile file, Long parentId, Long userId);
    Long createFolder(Long userId, Long parentId, String folderName);
    void renameFile(Long userId, Long fileId, String newName);
    void moveFile(Long userId, List<Long> fileIds, Long targetParentId);
    void deleteFiles(Long userId, List<Long> fileIds);
    List<FileVO> listFiles(Long userId, Long parentId, String sort, String keyword, String fileType);
    SpaceInfo calculateUsedSpace(Long userId);
    Long copyFile(Long userId, Long fileId, Long targetParentId);
    List<FileVO> listDeletedFiles(Long userId);
    void restoreFiles(Long userId, List<Long> fileIds);
    void permanentlyDeleteFiles(Long userId, List<Long> fileIds);
}