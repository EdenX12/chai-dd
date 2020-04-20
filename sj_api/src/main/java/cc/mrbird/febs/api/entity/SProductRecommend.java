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
 * 商品推荐关联类
 * @author MrBird
 */
@Data
@TableName("s_product_recommend")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProductRecommend implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 商品id
     */
    private String productId;

    /**
     * 推荐分类id
     */
    private String recommendTypeId;

    /**
     * 是否在首页显示 0-部显示 1-显示
     */
    private Integer isOnFace;

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
