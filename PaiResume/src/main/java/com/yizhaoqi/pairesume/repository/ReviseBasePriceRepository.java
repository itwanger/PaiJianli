package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.ReviseBasePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 简历修改基础价格表的数据访问仓库
 */
public interface ReviseBasePriceRepository extends JpaRepository<ReviseBasePrice, Long> {

    /**
     * 根据使用次数查询价格
     * @param usageCount 使用次数
     * @return 价格实体
     */
    Optional<ReviseBasePrice> findByUsageCount(Integer usageCount);
}
