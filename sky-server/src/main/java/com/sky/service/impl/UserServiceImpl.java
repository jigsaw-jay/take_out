package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisConstants;
import com.sky.constant.WechatConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 微信用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public Result<UserLoginVO> userLogin(UserLoginDTO userLoginDTO) {
        //用HttpClient调用微信接口服务，获取当前微信用户的openId
        String code = userLoginDTO.getCode();
        String openId = getOpenId(code);
        //判断openId是否为空，为空抛出异常
        if (openId == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断是否为新用户
        LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
        User user = getOne(lqw.eq(User::getOpenid, openId));
        if (user == null) {
            //是新用户->自动完成注册，保存用户信息
            user = User.builder().openid(openId).createTime(LocalDateTime.now()).build();
            save(user);
        }
        //生成claims
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        //生成token=secret+ttl+claims
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        //构建UserLoginVO
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }

    /**
     * HttpClient获取openId
     *
     * @param code
     * @return
     */
    private String getOpenId(String code) {
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put(WechatConstant.APPID, weChatProperties.getAppid()); //appid
        userMap.put(WechatConstant.SECRET, weChatProperties.getSecret()); //secret
        userMap.put(WechatConstant.JS_CODE, code); //js_code
        userMap.put(WechatConstant.GRANT_TYPE, "authorization_code"); //grant_type
        String openIdJson = HttpClientUtil.doGet(WechatConstant.WECHAT_LOGIN, userMap);
        JSONObject jsonObject = JSONUtil.parseObj(openIdJson);
        String openid = jsonObject.getStr("openid");
        return openid;
    }
}
