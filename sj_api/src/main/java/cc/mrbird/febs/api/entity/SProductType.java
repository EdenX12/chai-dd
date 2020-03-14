package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品分类类
 * @author MrBird
 */
@Data
@TableName("s_product_type")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProductType implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 创建日期
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 更新日期
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private Long updateUser;
}
