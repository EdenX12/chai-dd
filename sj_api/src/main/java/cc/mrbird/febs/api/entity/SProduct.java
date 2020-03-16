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
 * 产品类
 * @author MrBird
 */
@Data
@TableName("s_product")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 创建日期
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Integer createUser;

    /**
     * 更新日期
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private Integer updateUser;

    /**
     * 商品状态  0 未发布 1 已发布 未成交 2 已成交 3 已下架
     */
    private Integer productStatus;

    /**
     * 总任务份数
     */
    private Integer taskNumber;

    /**
     * 商品价格（单位元）
     */
    private BigDecimal productPrice;

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
     * 独赢奖励（页面展示，真正分钱另有规则）
     */
    private String successReward;

    /**
     * 躺赢奖励（页面展示，真正分钱另有规则）
     */
    private String everyReward;

    /**
     * 产品类型 1 新手标 2 正常标
     */
    private Integer productType;


    /**
     * 商品已领任务数量
     */
    private transient Integer taskPayCount;

    /**
     * 商品关注数量
     */
    private transient Integer followCount;

    /**
     * 用户ID
     */
    private transient Long userId;

    /**
     * 是否已关注商品（1：已关注  0：未关注）
     */
    private transient Integer followStatus;

    /**
     * 购买上限
     */
    private transient Integer buyNumber;
}
