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
@TableName("s_order")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 支付方式
     */
    private Integer paymentType;

    /**
     * 付款状态:0:未付款;1:已付款
     */
    private Integer paymentState;

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
     * 运费价格
     */
    private BigDecimal shippingFee;

    /**
     * 发货备注
     */
    private String deliverExplain;

    /**
     * 收货地址id
     */
    private Long addressId;

    /**
     * 订单应付金额
     */
    private BigDecimal orderAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

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
    private String address_name;

    /**
     * 收货电话
     */
    private String addressPhone;

    /**
     * 收货地址
     */
    private String addressDetail;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer productNumber;

    /**
     * 发货时间
     */
    private Date shippingTime;

    /**
     * 支付(付款)时间
     */
    private Date paymentTime;

    /**
     * 订单生成时间
     */
    private Date createTime;


    /**
     * 商品名称
     */
    private transient String productName;

    /**
     * 商品图片
     */
    private transient String productImg;

    /**
     * 商品简介
     */
    private transient String productDes;

    /**
     * 商品详情（html）
     */
    private transient String productDetail;

    /**
     * 商品价格（单位元）
     */
    private transient BigDecimal productPrice;

    /**
     * 单位 只 座
     */
    private transient String priceUnit;

    /**
     * 物流公司名称
     */
    private transient String shippingExpressName;

}
