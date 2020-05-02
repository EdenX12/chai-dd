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
 * 订单商品类
 * @author MrBird
 */
@Data
@TableName("s_order_product")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SOrderProduct implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单ID
     */
    private String orderDetailId;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 任务线ID
     */
    private String taskLineId;

    /**
     * 商品规格名称（下划线连接多个）
     */
    private String productSpecValueName;

    /**
     * 购买数量
     */
    private Integer productNumber;

    /**
     * 商品价格
     */
    private BigDecimal productPrice;

    /**
     * 划线价格
     */
    private BigDecimal scribingPrice;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImg;

    /**
     * 商品简介
     */
    private String productDes;

    /**
     * 商品详情
     */
    private String productDetail;

    /**
     * 商品订单生成时间
     */
    private Date createTime;
}
