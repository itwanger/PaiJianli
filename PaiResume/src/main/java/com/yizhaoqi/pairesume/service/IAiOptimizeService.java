package com.yizhaoqi.pairesume.service;

public interface IAiOptimizeService {

    /**
     * 调用 AI 模型优化给定的内容
     *
     * @param originalContent 待优化的原始字符串 (通常是 JSON)
     * @return 优化后的字符串，如果优化失败或返回格式不正确，则返回 null
     */
    String optimize(String originalContent);
}
