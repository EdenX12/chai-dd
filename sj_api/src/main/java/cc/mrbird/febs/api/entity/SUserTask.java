package cc.mrbird.febs.api.entity;

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
 * 用户任务类
 * @author MrBird
 */
@Data
@TableName("s_user_task")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 上级id
     */
    private Long parentId;

    /**
     * 状态 1 已支付 2 不支付
     */
    private Integer payStatus;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 任务份数
     */
    private Integer taskNumber;

    /**
     * 0 已接任务 （任务完成时联合支付状态分佣金 ） 1 转让中 2 转让成功 3 任务完结 4 佣金已入账
     */
    private Integer status;

    /**
     * 是否已转发朋友圈（0：未 1：已）
     */
    private Integer shareFlag;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 修改日期
     */
    private Date updateTime;
}
