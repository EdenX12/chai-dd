package cc.mrbird.febs.api.entity;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户支付记录（任务支付 充值）
 * @author MrBird
 */
@Data
@TableName("s_user_pay")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserPay implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 订单号
     */
    private String paySn;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 1 任务网上付款 2 充值
     */
    private Integer payType;

    /**
     * 关联得到任务id
     */
    private String relationId;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 券id
     */
    private String couponId;

    /**
     * 状态 0 未支付 1 已支付
     */
    private Integer payStatus;

    /**
     * 支付成功时间
     */
    private Date payTime;

    /**
     * 支付流水号
     */
    private String transSn;

    /**
     * 创建日期
     */
    private Date createTime;
}
