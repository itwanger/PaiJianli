package com.yizhaoqi.pairesume.repository;

import com.yizhaoqi.pairesume.entity.ReviseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 简历修改记录表的数据访问仓库
 */
public interface ReviseRecordRepository extends JpaRepository<ReviseRecord, Long> {
}
