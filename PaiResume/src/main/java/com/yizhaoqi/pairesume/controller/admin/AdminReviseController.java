package com.yizhaoqi.pairesume.controller.admin;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.dto.ReviseFeedbackSaveDTO;
import com.yizhaoqi.pairesume.service.IReviseService;
import com.yizhaoqi.pairesume.vo.AdminReviseTaskDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/revise/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminReviseController {

    private final IReviseService reviseService;

    @GetMapping
    public R<Page<Object>> getReviseTaskList(@RequestParam(required = false) Integer status,
                                             @PageableDefault(sort = "createdAt,desc") Pageable pageable) {
        return R.ok(reviseService.getReviseTaskList(status, pageable));
    }

    @GetMapping("/{id}")
    public R<AdminReviseTaskDetailVO> getReviseTaskDetail(@PathVariable Long id) {
        // TODO: 从SecurityContext获取expertId
        Long expertId = 1L; // 假设专家ID为1
        return R.ok(reviseService.getReviseTaskDetail(id, expertId));
    }

    @PostMapping("/feedback/save")
    public R<Void> saveFeedback(@Valid @RequestBody ReviseFeedbackSaveDTO saveDTO) {
        // TODO: 从SecurityContext获取expertId并设置到DTO中
        // saveDTO.setExpertId(expertId);
        reviseService.saveFeedback(saveDTO);
        return R.ok();
    }

    @PostMapping("/{id}/finish")
    public R<Void> finishReviseTask(@PathVariable Long id) {
        // TODO: 从SecurityContext获取expertId
        Long expertId = 1L; // 假设专家ID为1
        reviseService.finishReviseTask(id, expertId);
        return R.ok();
    }
}
