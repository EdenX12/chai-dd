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
 * 出价类
 * @author MrBird
 */
@Data
@TableName("s_offer_price")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SOfferPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 出价金额
     */
    private BigDecimal amount;

    /**
     * 0 未生效（没支付时）1 竞标中 2 已出局 3 已成交
     */
    private Integer status;

    /**
     * 任务转让主键
     */
    private Integer taskOrderId;

    /**
     * 竞标人
     */
    private Long userId;

    /**
     * 0 未支付 1 已支付
     */
    private Integer payStatus;

    /**
     * 出价时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
