package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.RevisePriorityPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 简历修改加塞价格表的数据访问仓库
 */
public interface RevisePriorityPriceRepository extends JpaRepository<RevisePriorityPrice, Long> {
    
    /**
     * 根据加塞等级查询价格
     * @param priorityLevel 加塞等级
     * @return 价格实体
     */
    Optional<RevisePriorityPrice> findByPriorityLevel(Integer priorityLevel);
}
