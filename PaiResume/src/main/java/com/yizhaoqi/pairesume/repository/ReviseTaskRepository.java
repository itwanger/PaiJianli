package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.ReviseTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 简历修改任务表的数据访问仓库
 */
public interface ReviseTaskRepository extends JpaRepository<ReviseTask, Long> {

    /**
     * 根据用户ID统计其完成的任务数量
     * @param userId 用户ID
     * @return 已完成的任务数
     */
    long countByUserIdAndStatus(Long userId, Integer status);
    
    /**
     * 悲观锁：根据用户ID查询并锁定任务记录
     * 这会生成类似 SELECT ... FROM resume_revise_task WHERE user_id = ? FOR UPDATE 的SQL
     * @param userId 用户ID
     * @return 任务列表
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ReviseTask> findByUserId(Long userId);

    /**
     * 根据状态分页查询任务
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 任务分页结果
     */
    Page<ReviseTask> findByStatus(Integer status, Pageable pageable);
}
