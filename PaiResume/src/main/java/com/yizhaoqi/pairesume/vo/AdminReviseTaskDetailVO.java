package com.yizhaoqi.pairesume.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminReviseTaskDetailVO {

    private Long taskId;
    private Long userId;
    private Long resumeId;
    private String background;
    private String target;
    private Integer priorityLevel;
    private Integer status;
    private LocalDateTime createdAt;
    private FeedbackVO feedback;

    @Data
    @Builder
    public static class FeedbackVO {
        private Long recordId;
        private String content;
        private LocalDateTime updatedAt;
    }
}
