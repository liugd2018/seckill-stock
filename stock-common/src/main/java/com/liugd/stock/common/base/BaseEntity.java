package com.liugd.stock.common.base;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * 基础entity
 * @author liuguodong
 */
@Data
public class BaseEntity {

    @TableField("create_time")
    private Date createTime = new Date();

    @TableField("create_user")
    private String createUser;

    @TableField("update_time")
    private Date updateTime = new Date();

    @TableField("update_user")
    private String updateUser;

    @TableField("del_flg")
    private String delFlg = "0";
}
