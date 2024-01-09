package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Tag(name = "员工管理接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "登录功能")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<String> logout() {
        BaseContext.removeCurrentId();
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping()
    @Operation(summary = "新增员工")
    public Result saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.saveEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @Operation(summary = "员工分页查询")
    public Result<PageResult> page(@ParameterObject EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("传入信息:{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用/禁用员工
     *
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @Operation(summary = "启用/禁用员工")
    public Result setStatus(@PathVariable Integer status, @RequestParam(value = "id") Long id) {

        return employeeService.setStatus(status, id);
    }

    /**
     * 根据Id查询员工
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据Id查询员工DTO信息")
    public Result<EmployeeDTO> queryById(@PathVariable Long id) {
        return employeeService.queryById(id);
    }

    /**
     * 根据Id更新员工
     *
     * @param employeeDTO
     * @return
     */
    @PutMapping()
    @Operation(summary = "更新员工信息")
    public Result<EmployeeDTO> updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        return employeeService.updateEmployee(employeeDTO);
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    @Operation(summary = "修改员工密码")
    public Result updatePassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        return employeeService.updatePassword(passwordEditDTO);
    }
}
