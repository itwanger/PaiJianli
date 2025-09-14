package com.yizhaoqi.pairesume.controller;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.dto.UserUpdateProfileDTO;
import com.yizhaoqi.pairesume.service.IUserService;
import com.yizhaoqi.pairesume.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 获取当前登录用户的信息
     */
    @GetMapping("/me")
    public R<UserInfoVO> getCurrentUserInfo() {
        UserInfoVO userInfo = userService.getCurrentUserInfo();
        return R.ok(userInfo);
    }

    /**
     * 更新当前登录用户的信息
     */
    @PostMapping("/me")
    public R<?> updateCurrentUserInfo(@Valid @RequestBody UserUpdateProfileDTO userUpdateProfileDTO) {
        userService.updateCurrentUserInfo(userUpdateProfileDTO);
        return R.ok(null, "用户信息更新成功");
    }
}
