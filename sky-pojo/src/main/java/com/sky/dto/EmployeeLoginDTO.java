package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
//@ApiModel(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    //@ApiModelProperty("用户名")
    private String username;

    //@ApiModelProperty("密码")
    private String password;

}
