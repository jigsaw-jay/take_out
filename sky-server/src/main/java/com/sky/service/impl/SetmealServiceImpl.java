package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealDishService;
import com.sky.service.SetmealService;
import com.sky.utils.RedisUtils;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sky.constant.RedisConstants.*;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisUtils  redisUtils;
    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    public Result saveSetmeal(SetmealDTO setmealDTO) {
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        Setmeal one = getOne(lqw.eq(Setmeal::getName, setmealDTO.getName()));
        if (one != null) {
            throw new SetmealEnableFailedException("套餐名称已存在!");
        }
        //存储套餐
        Setmeal setmeal = new Setmeal();
        BeanUtil.copyProperties(setmealDTO, setmeal);
        save(setmeal);
        //清除缓存
        String key=CAHCE_SETMEAL_KEY+setmeal.getCategoryId();
        redisUtils.cleanCache(key);
        //存储套餐相关菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        setmealDishService.saveBatch(setmealDishes);
        return Result.success();
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> queryPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> setmealVOPage = setmealMapper.queryPage(setmealPageQueryDTO);
        return Result.success(new PageResult(setmealVOPage.getTotal(), setmealVOPage.getResult()));
    }

    /**
     * 根据Id查询套餐及菜品信息
     *
     * @param id
     * @return
     */
    @Override
    public Result<SetmealVO> queryById(Integer id) {
        SetmealVO setmealVO = new SetmealVO();
        Setmeal setmeal = getById(id);
        BeanUtil.copyProperties(setmeal, setmealVO);
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        List<SetmealDish> setmealDishes = setmealDishService.list(lqw.eq(SetmealDish::getSetmealId, setmeal.getId()));
        setmealVO.setSetmealDishes(setmealDishes);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    public Result updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtil.copyProperties(setmealDTO, setmeal);
        updateById(setmeal);
        //清除缓存
        String key=CAHCE_SETMEAL_KEY+setmeal.getCategoryId();
        redisUtils.cleanCache(key);
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        setmealDishService.remove(lqw.eq(SetmealDish::getSetmealId, setmealDTO.getId()));
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDTO.getId()));
        }
        setmealDishService.saveBatch(setmealDishes);
        return Result.success();
    }

    /**
     * 套餐启售、停售
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Result setStatus(Integer status, Long id) {
        LambdaUpdateWrapper<Setmeal> luw = new LambdaUpdateWrapper<>();
        luw.eq(Setmeal::getId, id).set(Setmeal::getStatus, status);
        update(luw);
        return Result.success();
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    @Override
    public Result removeSetmeal(List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> lqw = new LambdaUpdateWrapper<>();
        List<Setmeal> setmealList = list(lqw.in(Setmeal::getId, ids));
        for (Setmeal setmeal : setmealList) {
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //清理缓存
        for (Setmeal setmeal : setmealList) {
            String key=CAHCE_SETMEAL_KEY+setmeal.getCategoryId();
            redisUtils.cleanCache(key);
        }
        removeByIds(ids);
        return Result.success();
    }

    /**
     * 根据分类id查询套餐
     *
     * @param categoryId
     * @return
     */
    @Override
    public Result setmealList(Integer categoryId) {
        String key = CAHCE_SETMEAL_KEY + categoryId;
        String setmealJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(setmealJson)) {
            List<Setmeal> list = JSONUtil.toList(setmealJson, Setmeal.class);
            return Result.success(list);
        }
        LambdaUpdateWrapper<Setmeal> lqw = new LambdaUpdateWrapper<>();
        lqw.eq(Setmeal::getCategoryId, categoryId);
        List<Setmeal> list = list(lqw);
        redisUtils.set(key, JSONUtil.toJsonStr(list), CACHE_SETMEA_TTL, TimeUnit.MINUTES);
        return Result.success(list);
    }
}
