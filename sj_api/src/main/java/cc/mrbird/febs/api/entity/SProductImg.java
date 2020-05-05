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
 * 商品图片
 * @author MrBird
 */
@Data
@TableName("s_product_img")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProductImg implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品图片
     */
    private String imgUrl;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 创建日期
     */
    private Date createTime;
}
