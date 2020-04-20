package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 活动产品类
 * @author MrBird
 */
@Data
@TableName("s_activity_product")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SActivityProduct implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 活动id
     */
    private String actId;

    /**
     * 商品id
     */
    private String productId;

    /**
     * 任务金促销价
     */
    private BigDecimal price;

    /**
     * 商品划线价格
     */
    private BigDecimal scribingPrice;

    /**
     * 开始时间
     */
    private Date startDate;

    /**
     * 结束时间
     */
    private Date endDate;

    /**
     * 显示顺序
     */
    private Integer sOrder;

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
