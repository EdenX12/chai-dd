package cc.mrbird.febs.api.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户猎豆变动流水类
 * @author MrBird
 */
@Data
@TableName("s_user_bean_log")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserBeanLog implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 变动类型 1 下线奖励 2 。。
     */
    private Integer changeType;

    /**
     * 变动金额（减少前面加-）
     */
    private Integer changeAmount;

    /**
     * 变更时间
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
     * 关联的id
     */
    private String relationId;

    /**
     * 变动之前的猎豆
     */
    private Integer oldAmount;
}
