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
 * 用户分享类
 * @author MrBird
 */
@Data
@TableName("s_user_share")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserShare implements Serializable {

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
     * 上级分享ID
     */
    private String parentId;

    /**
     * 分享状态 0-待分享；1-分享成功；2-分享失败
     */
    private Integer shareStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建日期
     */
    private Date createTime;
}
