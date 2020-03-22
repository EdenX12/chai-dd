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
 * 任务完结后 用户奖励金额暂放类
 * @author MrBird
 */
@Data
@TableName("s_user_bonus_log")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserBonusLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 购买订单ID
     */
    private Long orderId;

    /**
     * 任务份数
     */
    private Integer taskNumber;

    /**
     * 类型 3 独赢奖励 4 躺赢奖励 5 下级奖励
     */
    private Integer bonusType;

    /**
     * 奖励金额
     */
    private BigDecimal bonusAmount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 暂时不用  0 正常1 异常
     */
    private Integer status;

}
