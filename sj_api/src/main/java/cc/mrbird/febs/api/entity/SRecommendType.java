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
 * 推荐类别类
 * @author MrBird
 */
@Data
@TableName("s_recommend_type")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SRecommendType implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 推荐title
     */
    private String recommend_title;

    /**
     * 状态 0-创建 1-发布 2-下架 3-删除
     */
    private Integer recommendStatus;

    /**
     * 排版风格 0-推荐 1-常规
     */
    private Integer pageStyle;

    /**
     * 显示顺序
     */
    private Integer sOrder;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 更新日期
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;
}
