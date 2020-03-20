package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 收货地址类
 * @author MrBird
 */
@Data
@TableName("s_user_address")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String trueName;

    /**
     * 省级id
     */
    private Long provinceId;

    /**
     * 省级名称
     */
    private String provinceName;

    /**
     * 市级ID
     */
    private Long cityId;

    /**
     * 市级名称
     */
    private String cityName;

    /**
     * 地区ID
     */
    private Long areaId;

    /**
     * 地区名称
     */
    private String areaName;

    /**
     * 地区内容
     */
    private String areaInfo;

    /**
     * 座机电话
     */
    private String telPhone;

    /**
     * 1默认收货地址
     */
    private String isDefault;

    /**
     * 邮编
     */
    private Integer zipCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}
