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
 * 用户提现类
 * @author MrBird
 */
@Data
@TableName("s_user_withdraw")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserWithdraw implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提现人
     */
    private Long userId;

    /**
     * 申请日期
     */
    private Date createTime;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 状态 0 已申请 1 已打款 2 已驳回
     */
    private Integer status;

    /**
     * 打款sn
     */
    private String paySn;

    /**
     * 处理人
     */
    private Long dealUser;

    /**
     * 处理时间
     */
    private Date dealTime;

    /**
     * 备注
     */
    private String remark;
}
