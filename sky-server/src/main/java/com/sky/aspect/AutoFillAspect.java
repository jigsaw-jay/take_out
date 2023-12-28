package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面
 * 实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     * mapper包下的方法并且加了AutoFill注解
     * 才执行
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {

    }

    /**
     * 定义前置通知
     * 在方法执行前执行注解
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始执行公共字段填充");
        //获取当前被拦截的方法上的数据库操作类型update？insert
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = autoFill.value();//获取数据库操作类型

        //获取当前被拦截的方法的参数->实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据当前操作类型位对应属性赋值->通过反射完成
        if (operationType == OperationType.INSERT) {
            //4个公共字段都需要填充
            try {
                Method setCreateTime = entity.getClass().getMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod("setCreateUser", Long.class);
                Method setUpdateUser = entity.getClass().getMethod("setUpdateUser", Long.class);
                Method setUpdateTime = entity.getClass().getMethod("setUpdateTime", LocalDateTime.class);

                //通过反射来赋值
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setCreateTime.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            //2个公共字段需要填充
            try {
                Method setUpdateUser = entity.getClass().getMethod("setUpdateUser", Long.class);
                Method setUpdateTime = entity.getClass().getMethod("setUpdateTime", LocalDateTime.class);

                //通过反射来赋值
                setUpdateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
