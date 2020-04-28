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
 * 用户使用优惠券流水类
 * @author MrBird
 */
@Data
@TableName("s_user_coupon_log")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserCouponLog implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 券id
     */
    private String couponId;

    /**
     * 用户券id
     */
    private String userCouponId;

    /**
     * 券类型 0-任务金 1-商铺券
     */
    private Integer couponType;

    /**
     * 本次使用数量
     */
    private Integer usedQuantity;

    /**
     * 本次使用金额
     */
    private BigDecimal usedAmount;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 更新日期
     */
    private Date updateTime;
}
