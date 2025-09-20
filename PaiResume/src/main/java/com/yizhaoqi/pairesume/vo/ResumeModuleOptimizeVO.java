package com.yizhaoqi.pairesume.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 优化模块响应视图 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeModuleOptimizeVO {

    /**
     * 原始 JSON 内容
     */
    private String originalContent;

    /**
     * AI 优化后的 JSON 内容
     */
    private String optimizedContent;

    /**
     * AI 提供的优化建议文字 (可选)
     */
    private String suggestion;
}
