package com.yizhaoqi.pairesume.service.impl;

import com.yizhaoqi.pairesume.common.exception.ServiceException;
import com.yizhaoqi.pairesume.common.utils.BeanUtil;
import com.yizhaoqi.pairesume.dto.UserUpdateProfileDTO;
import com.yizhaoqi.pairesume.entity.User;
import com.yizhaoqi.pairesume.repository.UserRepository;
import com.yizhaoqi.pairesume.service.IUserService;
import com.yizhaoqi.pairesume.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;

    @Override
    public UserInfoVO getCurrentUserInfo() {
        User currentUser = getCurrentUser();
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(currentUser, userInfoVO);
        return userInfoVO;
    }

    @Override
    @Transactional
    public void updateCurrentUserInfo(UserUpdateProfileDTO userUpdateProfileDTO) {
        User currentUser = getCurrentUser();

        // 使用自定义工具类，仅复制非空属性
        BeanUtil.copyNonNullProperties(userUpdateProfileDTO, currentUser);
        
        userRepository.save(currentUser);
    }

    /**
     * 从 Spring Security 上下文获取当前登录的用户实体
     *
     * @return User 实体
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        // 如果 principal 是 UserDetails 但不是我们的 User 实例（理论上不应该发生）
        // 或者根本没有认证信息，则抛出异常
        throw new ServiceException("无法获取当前用户信息", 401);
    }
}
