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
 * 用户购物车类
 * @author MrBird
 */
@Data
@TableName("s_user_shop_car")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserShopCar implements Serializable {

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
     * 商品ID
     */
    private String productId;

    /**
     * 商品规格ID
     */
    private String productSpecId;

    /**
     * 最初价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 选中状态 0-否 ；1-是
     */
    private Integer checkStatus;

    /**
     * 唯一标识-未登陆客户使用
     */
    private String extendRef;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 更新日期
     */
    private Date updateTime;
}
