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
 * 商品规格类
 * @author MrBird
 */
@Data
@TableName("s_product_spec")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProductSpec implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 规格值（用下划线连接）
     */
    private String productSpecValueId;

    /**
     * 规格值名称（颜色 用下划线连接）
     */
    private String productSpecValueType;

    /**
     * 规格值名称（黑色 用下划线连接）
     */
    private String productSpecValueName;

    /**
     * 商品数量
     */
    private Integer productNumber;

    /**
     * 商品库存数量
     */
    private Integer stockNumber;

    /**
     * 商品价格（单位元）
     */
    private BigDecimal productPrice;

    /**
     * 划线价格
     */
    private BigDecimal scribingPrice;

    /**
     * 删除标识：0未删除 1已删除
     */
    private Integer deleteFlag;

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
