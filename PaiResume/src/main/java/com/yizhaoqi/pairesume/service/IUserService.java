package com.yizhaoqi.pairesume.service;

import com.yizhaoqi.pairesume.vo.UserInfoVO;
import com.yizhaoqi.pairesume.dto.UserUpdateProfileDTO;

public interface IUserService {

    /**
     * 获取当前登录用户的信息
     *
     * @return 用户信息VO
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 更新当前登录用户的信息
     *
     * @param userUpdateProfileDTO 包含要更新信息的数据
     */
    void updateCurrentUserInfo(UserUpdateProfileDTO userUpdateProfileDTO);
}
