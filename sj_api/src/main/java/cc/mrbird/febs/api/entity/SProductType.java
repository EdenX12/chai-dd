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
 * 商品分类类
 * @author MrBird
 */
@Data
@TableName("s_product_type")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProductType implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 上级分类id
     */
    private String parentId;

    /**
     * 分类名称
     */
    private String typeName;

    /**
     * 分类图片
     */
    private String typeImg;

    /**
     * 分类状态  0 不可用 1 可用
     */
    private Integer typeStatus;

    /**
     * 显示顺序
     */
    private Integer sOrder;

    /**
     * 级别
     */
    private Integer level;

    /**
     * 0-普通; 1-推荐
     */
    private Integer flag;

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
