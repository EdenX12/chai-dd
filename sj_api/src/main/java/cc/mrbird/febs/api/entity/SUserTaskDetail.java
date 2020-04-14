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
 * 用户任务明细类
 * @author MrBird
 */
@Data
@TableName("s_user_task_detail")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserTaskDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务线id
     */
    private String taskLineId;

    /**
     * 0 已接任务 1 转让中 2 转让成功 3 任务完结 4 佣金已入账
     */
    private Integer status;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 修改日期
     */
    private Date updateTime;
}
