package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.PasswordConstant.DEFAULT_PASSWORD;

@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {


    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = getOne(new LambdaQueryWrapper<Employee>().eq(Employee::getUsername, username));

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void saveEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtil.copyProperties(employeeDTO, employee);
        //设置其余属性
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(DEFAULT_PASSWORD.getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //从线程中获取登录用户ID
        Long currentId = BaseContext.getCurrentId();
        employee.setCreateUser(currentId);
        employee.setUpdateUser(currentId);
        save(employee);
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        String name = employeePageQueryDTO.getName();
        int page = employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize();
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        lqw.orderByDesc(Employee::getUpdateTime);
        Page<Employee> employeePage = page(pageInfo, lqw);
        List<Employee> records = employeePage.getRecords();
        long total = employeePage.getTotal();
        return new PageResult(total, records);
    }

    /**
     * 启用/禁用员工
     *
     * @param status
     * @param id
     * @return
     */
    @Override
    public Result setStatus(Integer status, Long id) {
        Employee employee = getById(id);
        if (employee == null) {
            return Result.error("查无此人");
        }
        employee.setStatus(status);
        updateById(employee);
        return Result.success();
    }

    /**
     * 根据Id查询员工
     *
     * @param id
     * @return
     */
    @Override
    public Result<EmployeeDTO> queryById(Long id) {
        Employee employee = getById(id);
        if (employee == null) {
            return Result.error("查无此人");
        }
        EmployeeDTO employeeDTO = new EmployeeDTO();
        BeanUtil.copyProperties(employee, employeeDTO);
        return Result.success(employeeDTO);
    }

    /**
     * 更新员工
     *
     * @param employeeDTO
     * @return
     */
    @Override
    public Result<EmployeeDTO> updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtil.copyProperties(employeeDTO, employee);
        Long currentId = BaseContext.getCurrentId();
        employee.setUpdateUser(currentId);
        employee.setUpdateUser(currentId);
        employee.setUpdateTime(LocalDateTime.now());
        updateById(employee);
        return Result.success();
    }
}
