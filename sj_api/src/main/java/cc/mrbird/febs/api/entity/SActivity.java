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
 * 活动类
 * @author MrBird
 */
@Data
@TableName("s_activity")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SActivity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 活动名称
     */
    private String actName;

    /**
     * 活动类型
     */
    private String actType;

    /**
     * 广告图片
     */
    private String pictureUrl;

    /**
     * 状态 0-创建 1-发布 2-下架 3-删除
     */
    private Integer actStatus;

    /**
     * 显示顺序
     */
    private Integer sOrder;

    /**
     * 倒计时标记0-不需要倒计时 1-需要
     */
    private Integer flag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建者
     */
    private Long createUser;

    /**
     * 更新者
     */
    private Long updateUser;
}
