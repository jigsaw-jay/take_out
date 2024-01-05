package com.sky.controller.user;

import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/user")
@Tag(name = "C端用户相关接口")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 微信用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO){
        return userService.userLogin(userLoginDTO);
    }
}
