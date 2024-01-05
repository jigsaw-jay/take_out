package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.vo.UserLoginVO;

public interface UserService extends IService<User> {
    /**
     * 微信用户登录
     * @param userLoginDTO
     * @return
     */
    Result<UserLoginVO> userLogin(UserLoginDTO userLoginDTO);
}
