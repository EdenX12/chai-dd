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
 * 任务转让类
 * @author MrBird
 */
@Data
@TableName("s_task_order")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class STaskOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 转让分数
     */
    private Integer taskNumber;

    /**
     * 0 一口价 1 无底价拍卖 2 有底价拍卖
     */
    private Integer priceType;

    /**
     * 一口价 或者底价
     */
    private BigDecimal orderPrice;

    /**
     * 拍卖结束时间
     */
    private Date endTime;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 状态
     */
    private Integer status;
}
