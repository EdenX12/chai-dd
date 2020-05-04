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
     * 商品ID
     */
    private String productId;

    /**
     * 用户任务线ID
     */
    private String userTaskLineId;

    /**
     * 任务线ID
     */
    private String taskLineId;

    /**
     * 购买订单ID
     */
    private String orderDetailId;

    /**
     * 类型 1-独赢（买家立返）;2-任务躺赢;3-横向躺赢;4-纵向躺赢;5-平台返回任务金;6-已转出;7-已收购;
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
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 0-冻结 1-结算完成 2-取消结算(商品购买者退货)
     */
    private Integer status;

}
