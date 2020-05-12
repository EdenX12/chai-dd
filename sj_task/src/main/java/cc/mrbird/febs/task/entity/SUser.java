package cc.mrbird.febs.task.entity;

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

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 用户名（先写手机号）
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户头像
     */
    private String userImg;

    /**
     * 邀请码（随机生成 大写字母+数字 4位 ）
     */
    private String inviteCode;

    /**
     * 微信openId(打款时需要)
     */
    private String openId;

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
     * 领取任务次数
     */
    private Integer taskCount;

    /**
     * 猎豆数量
     */
    private Integer rewardBean;

    /**
     * 0 可用  1 不可用
     */
    private Integer userStatus;

    /**
     * 用户等级 0：新人 1：见习猎人 2：初级猎人 3：中级猎人 4：高级猎人
     */
    private Integer userLevelType;

    /**
     * 可以使用的猎豆数量
     */
    private Integer canuseBean;

    /**
     * 上级id
     */
    private String parentId;

    /**
     * 1、普通客户；2-商家；；3-拆家；4-即是拆家也是商家
     */
    private Integer userType;

    /**
     * 微信unionid
     */
    private String unionId;

    /**
     * appopenId(打款时需要)
     */
    private String app_open_id;

    /**
     * 最后一次登录时间
     */
    private Date lastLogin;

    /**
     * 0-APP,1-微信公众号,2-小程序
     */
    private Integer channel;

    /**
     * 创建日期
     */
    private Date createTime;
}
