package com.sky.result;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用于存储和返回Redis数据
 */
@Data
public class RedisResult {
    //逻辑过期时间
    private LocalDateTime expireTime;
    //Redis数据
    private Object data;
}
