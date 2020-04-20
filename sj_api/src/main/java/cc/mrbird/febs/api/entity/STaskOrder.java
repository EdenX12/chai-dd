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

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 用户id
     */
    private String userId;

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
     * 状态
     */
    private Integer status;

    /**
     * 创建日期
     */
    private Date createTime;
}
