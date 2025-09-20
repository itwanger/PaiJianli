package com.yizhaoqi.pairesume.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviseFeedbackSaveDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "专家ID不能为空")
    private Long expertId;

    @NotEmpty(message = "反馈内容不能为空")
    private String content;
}
