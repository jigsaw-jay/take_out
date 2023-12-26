package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分页查询时传递的数据模型")
public class EmployeePageQueryDTO implements Serializable {

    //员工姓名
    @Schema(description = "员工姓名")
    private String name;

    //页码
    @Schema(description = "页码")
    private int page;

    //每页显示记录数
    @Schema(description = "每页显示记录数")
    private int pageSize;

}
