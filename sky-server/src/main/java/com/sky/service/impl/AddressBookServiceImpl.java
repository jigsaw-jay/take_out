package com.sky.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RedisConstants.CACHE_ADDRESSBOOK_KEY;
import static com.sky.constant.RedisConstants.CACHE_ADDRESSBOOK_TTL;

@Service
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return
     */
    @Override
    public Result<List<AddressBook>> listAddressBook() {
        Long userId = BaseContext.getCurrentId();
        String key = CACHE_ADDRESSBOOK_KEY + userId;
        String addressJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(addressJson)) {
            List<AddressBook> addressBookList = JSONUtil.toList(addressJson, AddressBook.class);
            return Result.success(addressBookList);
        }
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        List<AddressBook> addressBooks = list(lqw.eq(AddressBook::getUserId, userId));
        redisUtils.set(key, JSONUtil.toJsonStr(addressBooks), CACHE_ADDRESSBOOK_TTL, TimeUnit.MINUTES);
        return Result.success(addressBooks);
    }

    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public Result addAddressBook(AddressBook addressBook) {
        log.info("address:{}", addressBook);
        Long userId = BaseContext.getCurrentId();
        String key = CACHE_ADDRESSBOOK_KEY + userId;
        addressBook.setUserId(userId);
        save(addressBook);
        redisUtils.cleanCache(key);
        return Result.success();
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public Result setDefaultAddress(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        String key = CACHE_ADDRESSBOOK_KEY + userId;
        LambdaUpdateWrapper<AddressBook> luw = new LambdaUpdateWrapper<>();
        luw.eq(AddressBook::getUserId, userId).eq(AddressBook::getIsDefault, 1);
        luw.set(AddressBook::getIsDefault, 0);
        update(luw);
        addressBook.setIsDefault(1);
        updateById(addressBook);
        redisUtils.cleanCache(key);
        return Result.success("设置成功！");
    }

    /**
     * 根据Id获取地址
     *
     * @param id
     * @return
     */
    @Override
    public Result<AddressBook> getAddressById(Long id) {
        AddressBook addressBook = getById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook
     * @return
     */
    @Override
    public Result updateAddress(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        log.info("userId:{}", userId);
        String key = CACHE_ADDRESSBOOK_KEY + userId;
        updateById(addressBook);
        redisUtils.cleanCache(key);
        return Result.success();
    }

    /**
     * 根据Id删除地址
     *
     * @param id
     * @return
     */
    @Override
    public Result deleteAddress(Long id) {
        Long userId = BaseContext.getCurrentId();
        String key = CACHE_ADDRESSBOOK_KEY + userId;
        removeById(id);
        redisUtils.cleanCache(key);
        return Result.success();
    }

    /**
     * 获取默认地址
     *
     * @return
     */
    @Override
    public Result<AddressBook> getDefaultAddress() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        AddressBook addressBook = getOne(lqw.eq(AddressBook::getUserId, userId).eq(AddressBook::getIsDefault, 1));
        if (addressBook == null) {
            return Result.error("没有设置默认地址！");
        }
        return Result.success(addressBook);
    }

}
