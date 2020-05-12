package cc.mrbird.febs.task.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户任务批量类
 * @author MrBird
 */
@Data
@TableName("s_user_task")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserTask implements Serializable {

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
     * 产品id
     */
    private String productId;

    /**
     * 状态 1 已支付 2 未支付 0 锁定
     */
    private Integer payStatus;

    /**
     * 用户优惠券id
     */
    private String userCouponId;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 0-APP,1-微信公众号,2-小程序
     */
    private Integer channel;

    /**
     * 任务数量
     */
    private  Integer taskNumber;
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 修改日期
     */
    private Date updateTime;
}
