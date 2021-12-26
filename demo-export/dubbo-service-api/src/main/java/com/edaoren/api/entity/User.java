package com.edaoren.api.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 *
 * @author EDaoren
 */
@Data
@TableName(value = "t_user")
public class User implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)//指定自增策略
    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("用户ID")
    private String cUserId;

    @ExcelProperty("用户名称")
    private String cName;

    @ExcelProperty("省份ID")
    private Long cProvinceId;

    @ExcelProperty("城市ID")
    private Long cCityId;

    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
    private Date createTime;
}
