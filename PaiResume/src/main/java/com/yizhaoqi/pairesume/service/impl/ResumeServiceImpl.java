package com.yizhaoqi.pairesume.service.impl;

import com.yizhaoqi.pairesume.common.exception.BusinessException;
import com.yizhaoqi.pairesume.dto.ResumeCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleUpdateDTO;
import com.yizhaoqi.pairesume.entity.Resume;
import com.yizhaoqi.pairesume.entity.ResumeModule;
import com.yizhaoqi.pairesume.repository.ResumeModuleRepository;
import com.yizhaoqi.pairesume.repository.ResumeRepository;
import com.yizhaoqi.pairesume.service.IResumeService;
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

    private static final long MAX_RESUME_COUNT_FOR_FREE_USER = 1;

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
