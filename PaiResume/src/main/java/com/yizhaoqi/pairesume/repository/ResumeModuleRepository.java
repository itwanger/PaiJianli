package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.ResumeModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ResumeModule 数据访问仓库
 */
public interface ResumeModuleRepository extends JpaRepository<ResumeModule, Long> {

    /**
     * 根据简历ID查询所有模块，并按 sortOrder 升序排序
     * @param resumeId 简历ID
     * @return 模块列表
     */
    List<ResumeModule> findByResumeIdOrderBySortOrderAsc(Long resumeId);

    /**
     * 根据简历ID删除所有模块
     * @param resumeId 简历ID
     */
    void deleteByResumeId(Long resumeId);
}
