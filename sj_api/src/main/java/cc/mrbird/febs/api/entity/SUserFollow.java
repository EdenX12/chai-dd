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
     * 转让任务id
     */
    private String taskOrderId;

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
