package cc.mrbird.febs.api.entity;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商品类
 * @author MrBird
 */
@Data
@TableName("s_product")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProduct implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 所属店铺ID
     */
    private String shopId;

    /**
     * 商品分类
     */
    private String typeId;

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
     * 商品详情（html）
     */
    private String productDetail;

    /**
     * 商品数量也是任务线总数
     */
    private Integer totalNumber;

    /**
     * 总任务份数
     */
    private Integer taskNumber;

    /**
     * 商品价格（单位元）
     */
    private BigDecimal productPrice;

    /**
     * 划线价格
     */
    private BigDecimal scribingPrice;

    /**
     * 快递费（0元 包邮）
     */
    private BigDecimal expressFee;

    /**
     * 总佣金（单位元）
     */
    private BigDecimal totalReward;

    /**
     * 任务单价（单位元）
     */
    private BigDecimal taskPrice;

    /**
     * 单位 只 座
     */
    private String priceUnit;

    /**
     * 商品类型 1 新手商品 2 正常商品
     */
    private Integer productType;

    /**
     * 显示顺序
     */
    private Integer sOrder;

    /**
     * 商品标签,限时商品|特价商品|精选商品|优惠商品
     */
    private String productTag;

    /**
     * 商品状态  0 未发布 1 已发布 未成交 2 已成交 3 已下架
     */
    private Integer productStatus;

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
    private Long createUser;

    /**
     * 更新人
     */
    private Long updateUser;
}
