package cc.mrbird.febs.api.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PayUserTaskDto implements Serializable {
    /**
     * 商品ID
     */
    @NotEmpty(message = "商品ID不可为空")
    private String productId;

    /**
     * 购买的任务数量
     */
    @NotEmpty(message = "购买数量")
    private Integer taskNumber;

    /**
     * 使用的优惠券金额
     */
    private BigDecimal couponAmt;

    /**
     * 总支付金额
     */
    @NotEmpty(message = "")
    private BigDecimal totalAmt;
    /**
     * 赠送猎豆
     */
    @NotEmpty(message = "")
    private Integer orderBeanCnt;

}
