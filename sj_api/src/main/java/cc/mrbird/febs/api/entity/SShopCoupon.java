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
 * 商铺优惠券类
 * @author MrBird
 */
@Data
@TableName("s_shop_coupon")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SShopCoupon implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 商铺id
     */
    private String shopId;

    /**
     * 券名称
     */
    private String couponName;

    /**
     * 券面值
     */
    private BigDecimal couponAmount;

    /**
     * 券开始日期
     */
    private Date startDate;

    /**
     * 券截止日期
     */
    private Date endDate;

    /**
     * 券数量
     */
    private Integer couponQuantity;

    /**
     * 每个人可以领取的最大数量
     */
    private Integer perLimit;

    /**
     * 最低消费金额
     */
    private BigDecimal minConsumeAmount;

    /**
     * 使用条件 0-立减 1-满减
     */
    private Integer useCon;

    /**
     * 状态 0-创建 1-发布 2-下架 3-删除
     */
    private Integer couponStatus;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 更新日期
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;
}
