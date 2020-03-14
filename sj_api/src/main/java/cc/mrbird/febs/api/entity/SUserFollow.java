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
 * 用户产品关注类
 * @author MrBird
 */
@Data
@TableName("s_user_follow")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 转让任务id
     */
    private Long taskOrderId;

    /**
     * 关注类别
     */
    private Integer followType;

    /**
     * 关注状态
     */
    private Integer status;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 更新日期
     */
    private Date updateTime;
}
