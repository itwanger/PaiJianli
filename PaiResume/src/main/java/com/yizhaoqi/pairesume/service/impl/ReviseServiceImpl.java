package com.yizhaoqi.pairesume.service.impl;

import com.yizhaoqi.pairesume.common.exception.BusinessException;
import com.yizhaoqi.pairesume.dto.PayCreateDTO;
import com.yizhaoqi.pairesume.dto.ReviseFeedbackSaveDTO;
import com.yizhaoqi.pairesume.dto.ReviseTaskCreateDTO;
import com.yizhaoqi.pairesume.entity.ReviseRecord;
import com.yizhaoqi.pairesume.entity.ReviseTask;
import com.yizhaoqi.pairesume.repository.ReviseBasePriceRepository;
import com.yizhaoqi.pairesume.repository.RevisePriorityPriceRepository;
import com.yizhaoqi.pairesume.repository.ReviseRecordRepository;
import com.yizhaoqi.pairesume.repository.ReviseTaskRepository;
import com.yizhaoqi.pairesume.service.INotificationService;
import com.yizhaoqi.pairesume.service.IPayService;
import com.yizhaoqi.pairesume.service.IReviseService;
import com.yizhaoqi.pairesume.vo.AdminReviseTaskDetailVO;
import com.yizhaoqi.pairesume.vo.PayCreateVO;
import com.yizhaoqi.pairesume.vo.ReviseTaskCreateVO;
import com.yizhaoqi.pairesume.vo.ReviseTaskStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviseServiceImpl implements IReviseService {

    private final ReviseTaskRepository reviseTaskRepository;
    private final ReviseRecordRepository reviseRecordRepository;
    private final ReviseBasePriceRepository basePriceRepository;
    private final RevisePriorityPriceRepository priorityPriceRepository;
    private final IPayService payService;
    private final INotificationService notificationService;

    // TODO: 注入通知服务 INotificationService

    @Override
    @Transactional
    public ReviseTaskCreateVO createReviseTask(ReviseTaskCreateDTO createDTO, Long userId) {
        // 1. 使用悲观锁查询并锁定该用户的所有任务记录，以确保后续操作的原子性
        List<ReviseTask> userTasks = reviseTaskRepository.findByUserId(userId);

        // 2. 从锁定的结果中计算已完成的任务数，以确定本次是第几次修改
        long completedTasks = userTasks.stream()
                .filter(task -> Integer.valueOf(3).equals(task.getStatus())) // 3=已完成
                .count();

        // 3. 计算价格
        BigDecimal baseAmount = basePriceRepository.findByUsageCount((int) completedTasks)
                .orElseThrow(() -> new BusinessException("未找到对应的基础价格配置"))
                .getAmount();

        BigDecimal priorityAmount = BigDecimal.ZERO;
        if (createDTO.getPriorityLevel() != null && createDTO.getPriorityLevel() > 0) {
            priorityAmount = priorityPriceRepository.findByPriorityLevel(createDTO.getPriorityLevel())
                    .orElseThrow(() -> new BusinessException("未找到对应的加塞价格配置"))
                    .getAmount();
        }
        BigDecimal totalAmount = baseAmount.add(priorityAmount);

        // 4. 创建任务实体
        ReviseTask task = new ReviseTask();
        task.setUserId(userId);
        task.setResumeId(createDTO.getResumeId());
        task.setBackground(createDTO.getBackground());
        task.setTarget(createDTO.getTarget());
        task.setPriorityLevel(createDTO.getPriorityLevel());
        task.setAmount(totalAmount);
        task.setBizOrderId("R" + UUID.randomUUID().toString().replace("-", ""));

        // 5. 判断是否需要支付
        Map<String, String> payParams = null;
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            task.setStatus(0); // 待支付
            // 调用支付模块创建支付订单
            PayCreateDTO payCreateDTO = new PayCreateDTO();
            payCreateDTO.setBizOrderId(task.getBizOrderId());
            payCreateDTO.setBizType("REVISE_TASK");
            payCreateDTO.setAmount(totalAmount);
            payCreateDTO.setSubject("简历修改服务");
            PayCreateVO payCreateVO = payService.createPayment(payCreateDTO, userId);
            payParams = payCreateVO.getPayParams();
        } else {
            task.setStatus(1); // 待处理（免费任务）
        }
        
        ReviseTask savedTask = reviseTaskRepository.save(task);

        return ReviseTaskCreateVO.builder()
                .taskId(savedTask.getId())
                .bizOrderId(savedTask.getBizOrderId())
                .amount(savedTask.getAmount())
                .status(savedTask.getStatus())
                .payParams(payParams)
                .build();
    }

    @Override
    public ReviseTaskStatusVO getReviseTaskStatus(Long taskId, Long userId) {
        ReviseTask task = reviseTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 权限校验：确保用户只能查询自己的任务
        if (!Objects.equals(task.getUserId(), userId)) {
            throw new BusinessException("无权查看该任务");
        }

        ReviseTaskStatusVO.ReviseTaskStatusVOBuilder builder = ReviseTaskStatusVO.builder()
                .taskId(task.getId())
                .status(task.getStatus())
                .statusText(convertStatusToText(task.getStatus()));

        // 如果任务已完成，则附带修改记录
        if (task.getStatus() == 3 && task.getActiveRecordId() != null) {
            reviseRecordRepository.findById(task.getActiveRecordId()).ifPresent(record -> {
                // TODO: 优雅地获取专家姓名
                builder.expertName("简历专家");
                ReviseTaskStatusVO.FeedbackRecordVO feedback = ReviseTaskStatusVO.FeedbackRecordVO.builder()
                        .id(record.getId())
                        .content(record.getContent())
                        .updatedAt(record.getUpdatedAt())
                        .build();
                builder.feedbackHistory(Collections.singletonList(feedback));
            });
        }

        return builder.build();
    }

    @Override
    public Page<ReviseTask> getReviseTaskList(Integer status, Pageable pageable) {
        // TODO: 使用 Specification 或 Querydsl 进行动态查询
        // 为简化，此处只实现按状态查询
        if (status != null) {
            return reviseTaskRepository.findByStatus(status, pageable);
        }
        return reviseTaskRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public AdminReviseTaskDetailVO getReviseTaskDetail(Long taskId, Long expertId) {
        ReviseTask task = reviseTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        ReviseRecord record;
        if (task.getActiveRecordId() == null) {
            // 首次处理，创建空的记录
            record = new ReviseRecord();
            record.setTaskId(taskId);
            record.setExpertId(expertId);
            record.setContent(""); // 初始为空
            ReviseRecord savedRecord = reviseRecordRepository.save(record);
            
            // 关联回 task 表
            task.setActiveRecordId(savedRecord.getId());
            reviseTaskRepository.save(task);
            record = savedRecord;
        } else {
            record = reviseRecordRepository.findById(task.getActiveRecordId())
                    .orElseThrow(() -> new BusinessException("关联的修改记录不存在"));
        }

        // 组装 VO
        AdminReviseTaskDetailVO.FeedbackVO feedbackVO = AdminReviseTaskDetailVO.FeedbackVO.builder()
                .recordId(record.getId())
                .content(record.getContent())
                .updatedAt(record.getUpdatedAt())
                .build();
        
        return AdminReviseTaskDetailVO.builder()
                .taskId(task.getId())
                .userId(task.getUserId())
                .resumeId(task.getResumeId())
                .background(task.getBackground())
                .target(task.getTarget())
                .priorityLevel(task.getPriorityLevel())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .feedback(feedbackVO)
                .build();
    }

    @Override
    @Transactional
    public void saveFeedback(ReviseFeedbackSaveDTO saveDTO) {
        ReviseTask task = reviseTaskRepository.findById(saveDTO.getTaskId())
                .orElseThrow(() -> new BusinessException("任务不存在"));
        
        if (task.getActiveRecordId() == null) {
            throw new BusinessException("请先查看任务详情以初始化修改记录");
        }

        ReviseRecord record = reviseRecordRepository.findById(task.getActiveRecordId())
                .orElseThrow(() -> new BusinessException("关联的修改记录不存在"));

        // 权限校验：可以简单校验 expertId 是否匹配
        if (!record.getExpertId().equals(saveDTO.getExpertId())) {
            throw new BusinessException("无权修改该记录");
        }
        
        record.setContent(saveDTO.getContent());
        reviseRecordRepository.save(record);
    }

    @Override
    @Transactional
    public void finishReviseTask(Long taskId, Long expertId) {
        ReviseTask task = reviseTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 1. 状态校验：只有待处理(1)或优化中(2)的任务才能被完成
        if (task.getStatus() != 1 && task.getStatus() != 2) {
            throw new BusinessException("该任务当前状态无法被完成");
        }
        
        // 2. 权限校验（简化）：校验操作的专家是否是处理该任务的专家
        // 首先需要确保修改记录已创建
        if (task.getActiveRecordId() == null) {
            throw new BusinessException("任务尚未开始处理，无法直接完成");
        }
        reviseRecordRepository.findById(task.getActiveRecordId()).ifPresent(record -> {
            if (!record.getExpertId().equals(expertId)) {
                throw new BusinessException("您不是该任务的处理专家，无权操作");
            }
        });

        // 3. 更新任务状态
        task.setStatus(3); // 3=已完成
        reviseTaskRepository.save(task);

        // 4. 发送用户通知
        String content = "您的简历修改任务已完成，快去看看专家给出的建议吧！";
        // 这里我们假设通知服务需要一个 DTO，或者可以直接传参数
        // notificationService.sendUserNotification(task.getUserId(), "简历修改完成", content);
        
        log.info("任务ID {} 已被专家 {} 操作完成。", taskId, expertId);
    }

    private String convertStatusToText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "待处理";
            case 2: return "优化中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }
}
