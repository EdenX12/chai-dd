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
 * 用户余额变动流水类
 * @author MrBird
 */
@Data
@TableName("s_user_amount_log")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserAmountLog implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 关联的用户
     */
    private String userId;

    /**
     * 变动类型 1 充值 2 提现 3 独赢奖励 4 躺赢奖励 5 提现驳回 6任务转让收入 7 其他
     */
    private Integer changeType;

    /**
     * 变动金额（减少前面加-）
     */
    private BigDecimal changeAmount;

    /**
     * 变动时间
     */
    private Date changeTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 暂时不用  0 正常1 异常
     */
    private Integer status;

    /**
     * 关联的id 充值关联的充值记录 转让关联的转让记录 赢关联任务 提现关联提现申请
     */
    private String relationId;

    /**
     * 变动之前的金额
     */
    private BigDecimal oldAmount;
}
