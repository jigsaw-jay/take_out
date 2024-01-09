package com.sky.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sky.result.RedisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.sky.constant.RedisConstants.LOCK_TTL;

/**
 * Redis工具类
 */
@Component
public class RedisUtils {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 序列化为JSON存入Redis+设置TTL
     *
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 序列化为JSON存入Redis+设置逻辑过期时间
     *
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void setLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisResult redisResult = new RedisResult();
        redisResult.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        redisResult.setData(value);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisResult));
    }

    /**
     * 清除缓存
     * @param key
     */
    public void cleanCache(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 逻辑过期
     * @param keyPrefix
     * @param lockPrefix
     * @param id
     * @param type
     * @param dbFallback
     * @param time
     * @param unit
     * @return
     * @param <R>
     * @param <ID>
     */
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, String lockPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String lockKey = lockPrefix + id;
        //1.从Redis查询缓存
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        //2.判断缓存是否命中
        if (StrUtil.isBlank(jsonStr)) {
            //3.未命中->返回空
            return null;
        }
        //4.命中->json反序列化->判断缓存是否过期
        RedisResult redisResult = JSONUtil.toBean(jsonStr, RedisResult.class);
        R bean = JSONUtil.toBean((JSONObject) redisResult.getData(), type);
        LocalDateTime expireTime = redisResult.getExpireTime();
        if (expireTime.isAfter(LocalDateTime.now())) {
            //5.未过期->返回信息
            return bean;
        }
        //6.缓存过期->尝试获取互斥锁
        boolean tryLock = tryLock(lockKey);
        //7.判断是否获取成功
        if (tryLock) {
            //8.获取成功->开启独立线程，实现缓存重建->释放锁->返回信息
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    //缓存重建
                    R apply = dbFallback.apply(id);
                    this.setLogicalExpire(key, apply, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unLock(lockKey);
                }
            });
        }
        //9.获取失败->返回过期信息
        return bean;
    }

    /**
     * 创建锁
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     */
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 创建线程池
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
}
