package com.sky.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.sky.constant.RedisConstants;
import com.sky.context.BaseContext;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Tag(name = "Shop")
public class ShopController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置营业状态
     *
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @Operation(summary = "设置营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置营业状态为:{}", status == 1 ? "营业中" : "打烊");
        stringRedisTemplate.opsForValue().set(RedisConstants.SHOP_STATUS_KEY, status.toString());
        return Result.success();
    }

    /**
     * 查询营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @Operation(summary = "获取营业状态|")
    public Result<Integer> getStatus() {
        String statusStr = stringRedisTemplate.opsForValue().get(RedisConstants.SHOP_STATUS_KEY);
        if (StrUtil.isNotBlank(statusStr)) {
            int status = Integer.parseInt(statusStr);
            return Result.success(status);
        }
        return Result.success();
    }
}
