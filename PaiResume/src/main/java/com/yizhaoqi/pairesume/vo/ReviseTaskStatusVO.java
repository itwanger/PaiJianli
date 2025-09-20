package com.yizhaoqi.pairesume.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviseTaskStatusVO {

    private Long taskId;
    private Integer status;
    private String statusText;
    private String expertName;
    private List<FeedbackRecordVO> feedbackHistory;

    @Data
    @Builder
    public static class FeedbackRecordVO {
        private Long id;
        private String content;
        private LocalDateTime updatedAt;
    }
}
