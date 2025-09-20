package com.yizhaoqi.pairesume.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviseTaskCreateDTO {

    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    private String background;

    private String target;

    @NotNull(message = "加塞等级不能为空")
    private Integer priorityLevel;
}
