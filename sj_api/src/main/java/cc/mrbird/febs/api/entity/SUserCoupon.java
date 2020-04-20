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
 * 用户优惠券类
 * @author MrBird
 */
@Data
@TableName("s_user_coupon")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserCoupon implements Serializable {

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
     * 券类型 0-任务金 1-商铺券
     */
    private Integer couponType;

    /**
     * 券可用数量
     */
    private Integer avaQuantity;

    /**
     * 券可用金额
     */
    private BigDecimal avaAmount;

    /**
     * 已用数量
     */
    private Integer usedQuantity;

    /**
     * 已用金额
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
