package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户任务线类
 * @author MrBird
 */
@Data
@TableName("s_task_line")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class STaskLine implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 该条任务线总任务数
     */
    private Integer totalTask;

    /**
     * 该条任务线已领取的任务数
     */
    private Integer receivedTask;

    /**
     * 该条任务线已锁定待支付完成的任务数
     */
    private Integer lockTask;

    /**
     * 0-未满 1-已满
     */
    private Integer lineStatus;

    /**
     * 结算状态  0：未完成  1：结算中  2： 已分润
     */
    private Integer settleStatus;

    /**
     * 排序
     */
    private Integer lineOrder;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 修改日期
     */
    private Date updateTime;
}
