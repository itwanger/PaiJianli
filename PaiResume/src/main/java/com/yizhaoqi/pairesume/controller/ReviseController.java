package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.common.utils.RequestUtils;
import com.yizhaoqi.pairesume.dto.ReviseTaskCreateDTO;
import com.yizhaoqi.pairesume.service.IReviseService;
import com.yizhaoqi.pairesume.vo.ReviseTaskCreateVO;
import com.yizhaoqi.pairesume.vo.ReviseTaskStatusVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/revise/tasks")
@RequiredArgsConstructor
public class ReviseController {

    private final IReviseService reviseService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public R<ReviseTaskCreateVO> createReviseTask(@Valid @RequestBody ReviseTaskCreateDTO createDTO, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("用户未登录");
        }
        return R.ok(reviseService.createReviseTask(createDTO, userIdOptional.get()));
    }

    @GetMapping("/{id}/status")
    public R<ReviseTaskStatusVO> getReviseTaskStatus(@PathVariable Long id, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("用户未登录");
        }
        return R.ok(reviseService.getReviseTaskStatus(id, userIdOptional.get()));
    }
}
