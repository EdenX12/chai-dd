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
 * 购买订单类
 * @author MrBird
 */
@Data
@TableName("s_order_detail")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SOrderDetail implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 批量订单ID
     */
    private String orderId;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 支付方式
     */
    private Integer paymentType;

    /**
     * 付款状态:0:未付款;1:已付款
     */
    private Integer paymentState;

    /**
     * 支付(付款)时间
     */
    private Date paymentTime;

    /**
     * 订单应付金额
     */
    private BigDecimal orderAmount;

    /**
     * 运费价格
     */
    private BigDecimal shippingFee;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 配送公司编号
     */
    private String shippingExpressCode;

    /**
     * 快递公司ID
     */
    private Long shippingExpressId;

    /**
     * 物流单号
     */
    private String shippingCode;

    /**
     * 发货备注
     */
    private String deliverExplain;

    /**
     * 发货时间
     */
    private Date shippingTime;

    /**
     * 收货地址id
     */
    private String addressId;

    /**
     * 订单留言
     */
    private String orderMessage;

    /**
     * 订单状态  0未付款  1已付款待发货  2已发货  3已确认收货 4 已退款 -1 已取消
     */
    private Integer orderStatus;

    /**
     * 收货人
     */
    private String addressName;

    /**
     * 收货电话
     */
    private String addressPhone;

    /**
     * 收货地址
     */
    private String addressDetail;

    /**
     * 渠道 0-APP,1-微信公众号,2-小程序
     */
    private Integer channel;

    /**
     * 订单生成时间
     */
    private Date createTime;
}
