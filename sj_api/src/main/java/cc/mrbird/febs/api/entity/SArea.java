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
 * 省市区类
 * @author MrBird
 */
@Data
@TableName("s_area")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SArea implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地区名称
     */
    private String areaName;

    /**
     * 地区父ID
     */
    private Long areaParentId;

    /**
     * 排序
     */
    private Integer areaSort;

    /**
     * 地区深度
     */
    private Integer areaDeep;

    /**
     * 是否删除0:未删除;1:已删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}
