package com.sky.dto;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {
    @Hidden
    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
