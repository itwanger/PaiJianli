package com.yizhaoqi.pairesume.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 简历模块表实体
 */
@Data
@Entity
@Table(name = "resume_module")
public class ResumeModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    @Column(name = "module_type", nullable = false)
    private String moduleType;

    /**
     * 使用 @Column(columnDefinition = "json") 来告诉 JPA 这是一个 JSON 类型的字段
     */
    @Column(columnDefinition = "json", nullable = false)
    private String content;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
