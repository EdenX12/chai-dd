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
 * 用户消息类
 * @author MrBird
 */
@Data
@TableName("s_user_msg")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserMsg implements Serializable {

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
     * 消息日期
     */
    private Date msgTime;

    /**
     * 0 未读 1 已读
     */
    private Integer status;

    /**
     * 0 赏金到账
     */
    private Integer msgType;

    /**
     * 消息标题
     */
    private String msgTitle;

    /**
     * 消息内容
     */
    private String msgInfo;
}
