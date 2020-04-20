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
 * 广告类
 * @author MrBird
 */
@Data
@TableName("s_banner")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SBanner implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 广告名称
     */
    private String bannerName;

    /**
     * 类型：0-APP引导页,1-首页轮播图
     */
    private Integer bannerType;

    /**
     * banner图片url
     */
    private String bannerUrl;

    /**
     * banner跳转url
     */
    private String jumpUrl;

    /**
     * 排序
     */
    private Integer sOrder;

    /**
     * 状态 0-创建 1-发布 2-下架 3-删除
     */
    private Integer bannerStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建者
     */
    private Long createUser;

    /**
     * 更新者
     */
    private Long updateUser;
}
