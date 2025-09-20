package com.yizhaoqi.pairesume.service.impl;

import com.yizhaoqi.pairesume.common.exception.BusinessException;
import com.yizhaoqi.pairesume.dto.ResumeCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleOptimizeDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleUpdateDTO;
import com.yizhaoqi.pairesume.entity.Resume;
import com.yizhaoqi.pairesume.entity.ResumeModule;
import com.yizhaoqi.pairesume.repository.ResumeModuleRepository;
import com.yizhaoqi.pairesume.repository.ResumeRepository;
import com.yizhaoqi.pairesume.service.IAiOptimizeService;
import com.yizhaoqi.pairesume.service.IResumeService;
import com.yizhaoqi.pairesume.vo.ResumeModuleOptimizeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements IResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeModuleRepository resumeModuleRepository;
    private final IAiOptimizeService aiOptimizeService;


    private static final long MAX_RESUME_COUNT_FOR_FREE_USER = 1;
    private static final int MAX_CONTENT_LENGTH = 5000; // 定义最大长度常量


    @Override
    public List<Resume> getResumeList(Long userId) {
        return resumeRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Resume createResume(ResumeCreateDTO createDTO, Long userId) {
        // 校验简历数量是否达到上限
        long resumeCount = resumeRepository.countByUserId(userId);
        if (resumeCount >= MAX_RESUME_COUNT_FOR_FREE_USER) {
            throw new BusinessException("免费用户最多只能创建一份简历");
        }

        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(createDTO.getTitle());
        resume.setStatus(1); // 默认为草稿状态
        return resumeRepository.save(resume);
    }

    @Override
    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        Resume resume = getResumeAndCheckPermission(resumeId, userId);

        // 使用事务，先删除模块，再删除简历
        resumeModuleRepository.deleteByResumeId(resume.getId());
        resumeRepository.delete(resume);
    }

    @Override
    public List<ResumeModule> getResumeModules(Long resumeId, Long userId) {
        getResumeAndCheckPermission(resumeId, userId);
        return resumeModuleRepository.findByResumeIdOrderBySortOrderAsc(resumeId);
    }

    @Override
    @Transactional
    public ResumeModule createResumeModule(Long resumeId, ResumeModuleCreateDTO createDTO, Long userId) {
        getResumeAndCheckPermission(resumeId, userId);

        ResumeModule module = new ResumeModule();
        module.setResumeId(resumeId);
        module.setModuleType(createDTO.getModuleType());
        module.setContent(createDTO.getContent());
        module.setSortOrder(createDTO.getSortOrder());

        return resumeModuleRepository.save(module);
    }

    @Override
    @Transactional
    public ResumeModule updateResumeModule(Long resumeId, Long moduleId, ResumeModuleUpdateDTO updateDTO, Long userId) {
        getResumeAndCheckPermission(resumeId, userId);

        ResumeModule module = resumeModuleRepository.findById(moduleId)
                .orElseThrow(() -> new BusinessException("模块不存在"));

        // 校验模块是否属于该简历
        if (!Objects.equals(module.getResumeId(), resumeId)) {
            throw new BusinessException("模块与简历不匹配");
        }

        module.setContent(updateDTO.getContent());
        module.setSortOrder(updateDTO.getSortOrder());

        return resumeModuleRepository.save(module);
    }

    @Override
    @Transactional
    public void deleteResumeModule(Long resumeId, Long moduleId, Long userId) {
        getResumeAndCheckPermission(resumeId, userId);

        ResumeModule module = resumeModuleRepository.findById(moduleId)
                .orElseThrow(() -> new BusinessException("模块不存在"));
        
        // 校验模块是否属于该简历
        if (!Objects.equals(module.getResumeId(), resumeId)) {
            throw new BusinessException("模块与简历不匹配");
        }

        resumeModuleRepository.delete(module);
    }

    @Override
    public ResumeModuleOptimizeVO optimizeResumeModule(Long resumeId, Long moduleId, ResumeModuleOptimizeDTO optimizeDTO, Long userId) {
        // 步骤1: 使用 resumeId 和 userId 进行权限校验
        getResumeAndCheckPermission(resumeId, userId);

        // 步骤2: 使用 moduleId 校验模块是否存在且属于该简历
        ResumeModule module = resumeModuleRepository.findById(moduleId)
                .orElseThrow(() -> new BusinessException("模块不存在"));
        if (!Objects.equals(module.getResumeId(), resumeId)) {
            throw new BusinessException("模块与简历不匹配");
        }

        // 步骤3: 对前端传来的新内容进行长度校验
        String contentToOptimize = optimizeDTO.getContent();
        if (contentToOptimize == null || contentToOptimize.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("您输入的内容过长，请删减后再进行优化");
        }

        // 步骤4: 调用 AI 服务
        String optimizedContent = aiOptimizeService.optimize(contentToOptimize);

        // 步骤5: 处理 AI 服务失败的情况 (容错)
        if (optimizedContent == null) {
            // 尝试一次重试
            optimizedContent = aiOptimizeService.optimize(contentToOptimize);
            if (optimizedContent == null) {
                // 如果重试依然失败，则返回 null，由 Controller 统一处理
                return null;
            }
        }

        // 步骤6: 组装并返回 VO
        return new ResumeModuleOptimizeVO(contentToOptimize, optimizedContent, "AI 优化建议");
    }


    /**
     * 内部方法：获取简历并校验权限
     * @param resumeId 简历ID
     * @param userId   当前用户ID
     * @return 简历实体
     * @throws BusinessException 如果简历不存在或用户无权访问
     */
    private Resume getResumeAndCheckPermission(Long resumeId, Long userId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException("简历不存在"));
        if (!Objects.equals(resume.getUserId(), userId)) {
            throw new BusinessException("无权访问该简历");
        }
        return resume;
    }
}
