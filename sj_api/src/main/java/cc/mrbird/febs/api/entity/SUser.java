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
 * 用户类
 * @author MrBird
 */
@Data
@TableName("s_user")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（先写手机号）
     */
    private String userName;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 微信openId(打款时需要)
     */
    private String openId;

    /**
     * 用户头像
     */
    private String userImg;

    /**
     * 用户手机号码
     */
    private String userPhone;

    /**
     * 余额
     */
    private BigDecimal totalAmount;

    /**
     * 锁定金额
     */
    private BigDecimal lockAmount;

    /**
     * 猎豆数量
     */
    private Integer rewardBean;

    /**
     * 0 可用  1 不可用
     */
    private Integer userStatus;

    /**
     * 用户等级 用户等级表主键  1 初级猎人 2。。。
     */
    private Long userLevelId;

    /**
     * 可以使用的猎豆数量
     */
    private Integer canuseBean;

    /**
     * 上级id
     */
    private Long parentId;
}
