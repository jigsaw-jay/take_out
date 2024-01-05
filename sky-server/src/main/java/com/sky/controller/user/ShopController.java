package com.sky.controller.user;

import com.sky.constant.RedisConstants;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 查询营业状态
     * @return
     */
    @GetMapping("/status")
    @Operation(summary = "获取营业状态|")
    public Result<Integer> getStatus() {
        int status = Integer.parseInt(stringRedisTemplate.opsForValue().get(RedisConstants.SHOP_STATUS_KEY));
        return Result.success(status);
    }
}
