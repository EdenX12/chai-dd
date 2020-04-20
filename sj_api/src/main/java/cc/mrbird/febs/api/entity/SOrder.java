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
 * 批量订单类
 * @author MrBird
 */
@Data
@TableName("s_order")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SOrder implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

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
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付(付款)时间
     */
    private Date paymentTime;

    /**
     * 渠道：0-APP,1-微信公众号,2-小程序
     */
    private Integer channel;

    /**
     * 订单生成时间
     */
    private Date createTime;
}
