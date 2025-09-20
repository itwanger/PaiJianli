package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Resume 数据访问仓库
 */
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * 根据用户ID查询其所有简历
     * @param userId 用户ID
     * @return 简历列表
     */
    List<Resume> findByUserId(Long userId);

    /**
     * 根据用户ID统计简历数量
     * @param userId 用户ID
     * @return 该用户的简历总数
     */
    long countByUserId(Long userId);
}
