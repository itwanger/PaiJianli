package com.yizhaoqi.pairesume.service;

import com.yizhaoqi.pairesume.dto.ReviseFeedbackSaveDTO;
import com.yizhaoqi.pairesume.dto.ReviseTaskCreateDTO;
import com.yizhaoqi.pairesume.vo.AdminReviseTaskDetailVO;
import com.yizhaoqi.pairesume.vo.ReviseTaskCreateVO;
import com.yizhaoqi.pairesume.vo.ReviseTaskStatusVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IReviseService {

    // === 用户端接口 ===

    /**
     * 用户发起修改任务
     */
    ReviseTaskCreateVO createReviseTask(ReviseTaskCreateDTO createDTO, Long userId);

    /**
     * 用户查看修改任务进度
     */
    ReviseTaskStatusVO getReviseTaskStatus(Long taskId, Long userId);

    // === 管理端接口 ===

    /**
     * 专家查询任务列表
     */
    Page<Object> getReviseTaskList(Integer status, Pageable pageable);

    /**
     * 专家查看任务详情（并初始化修改记录）
     */
    AdminReviseTaskDetailVO getReviseTaskDetail(Long taskId, Long expertId);

    /**
     * 专家填写修改意见（自动保存）
     */
    void saveFeedback(ReviseFeedbackSaveDTO saveDTO);

    /**
     * 专家完成修改任务
     */
    void finishReviseTask(Long taskId, Long expertId);
}
