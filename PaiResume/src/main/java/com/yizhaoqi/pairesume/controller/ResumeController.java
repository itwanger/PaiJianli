package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.dto.ResumeCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleCreateDTO;
import com.yizhaoqi.pairesume.dto.ResumeModuleUpdateDTO;
import com.yizhaoqi.pairesume.entity.Resume;
import com.yizhaoqi.pairesume.entity.ResumeModule;
import com.yizhaoqi.pairesume.service.IResumeService;
import com.yizhaoqi.pairesume.common.utils.JwtUtil;
import com.yizhaoqi.pairesume.common.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final IResumeService resumeService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public R<List<Resume>> getResumeList(HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        List<Resume> list = resumeService.getResumeList(userIdOptional.get());
        return R.ok(list);
    }

    @PostMapping
    public R<Resume> createResume(@RequestBody ResumeCreateDTO createDTO, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        Resume resume = resumeService.createResume(createDTO, userIdOptional.get());
        return R.ok(resume);
    }

    @PostMapping("/{id}/delete")
    public R<Void> deleteResume(@PathVariable("id") Long resumeId, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        resumeService.deleteResume(resumeId, userIdOptional.get());
        return R.ok();
    }

    @GetMapping("/{id}/modules")
    public R<List<ResumeModule>> getResumeModules(@PathVariable("id") Long resumeId, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        List<ResumeModule> modules = resumeService.getResumeModules(resumeId, userIdOptional.get());
        return R.ok(modules);
    }

    @PostMapping("/{id}/modules/save")
    public R<ResumeModule> createResumeModule(@PathVariable("id") Long resumeId, @RequestBody ResumeModuleCreateDTO createDTO, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        ResumeModule module = resumeService.createResumeModule(resumeId, createDTO, userIdOptional.get());
        return R.ok(module);
    }

    @PostMapping("/{id}/modules/{moduleId}/update")
    public R<ResumeModule> updateResumeModule(@PathVariable("id") Long resumeId,
                                              @PathVariable("moduleId") Long moduleId,
                                              @RequestBody ResumeModuleUpdateDTO updateDTO, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        ResumeModule module = resumeService.updateResumeModule(resumeId, moduleId, updateDTO, userIdOptional.get());
        return R.ok(module);
    }

    @PostMapping("/{id}/modules/{moduleId}/delete")
    public R<Void> deleteResumeModule(@PathVariable("id") Long resumeId, @PathVariable("moduleId") Long moduleId, HttpServletRequest request) {
        Optional<Long> userIdOptional = RequestUtils.getUserIdFromRequest(request, jwtUtil);
        if (userIdOptional.isEmpty()) {
            return R.fail("未授权的访问");
        }
        resumeService.deleteResumeModule(resumeId, moduleId, userIdOptional.get());
        return R.ok();
    }
}
