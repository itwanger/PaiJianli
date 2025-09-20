package com.yizhaoqi.pairesume.service;

import com.yizhaoqi.pairesume.dto.ResumeCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleUpdateDTO;
import com.yizhaoqi.pairesume.entity.Resume;
import com.yizhaoqi.pairesume.entity.ResumeModule;

import java.util.List;

/**
 * 简历业务逻辑服务接口
 */
public interface IResumeService {

    /**
     * 获取当前用户的所有简历列表
     * @param userId 当前用户ID
     * @return 简历列表
     */
    List<Resume> getResumeList(Long userId);

    /**
     * 创建一份新简历
     * @param createDTO 简历创建信息
     * @param userId 当前用户ID
     * @return 创建成功后的简历实体
     */
    Resume createResume(ResumeCreateDTO createDTO, Long userId);

    /**
     * 删除简历
     * @param resumeId 简历ID
     * @param userId 当前用户ID
     */
    void deleteResume(Long resumeId, Long userId);

    /**
     * 获取指定简历的所有模块
     * @param resumeId 简历ID
     * @param userId 当前用户ID
     * @return 模块列表
     */
    List<ResumeModule> getResumeModules(Long resumeId, Long userId);

    /**
     * 为简历新增一个模块
     * @param resumeId 简历ID
     * @param createDTO 模块创建信息
     * @param userId 当前用户ID
     * @return 创建成功后的模块实体
     */
    ResumeModule createResumeModule(Long resumeId, ResumeModuleCreateDTO createDTO, Long userId);

    /**
     * 更新指定模块的内容
     * @param resumeId 简历ID
     * @param moduleId 模块ID
     * @param updateDTO 模块更新信息
     * @param userId 当前用户ID
     * @return 更新成功后的模块实体
     */
    ResumeModule updateResumeModule(Long resumeId, Long moduleId, ResumeModuleUpdateDTO updateDTO, Long userId);

    /**
     * 删除指定模块
     * @param resumeId 简历ID
     * @param moduleId 模块ID
     * @param userId 当前用户ID
     */
    void deleteResumeModule(Long resumeId, Long moduleId, Long userId);

    // AI 优化和导出 PDF 的方法可以后续添加
}
