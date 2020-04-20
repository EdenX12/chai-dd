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
 * 微信用户类
 * @author MrBird
 */
@Data
@TableName("s_user_wechat")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserWechat implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户手机号码
     */
    private String userPhone;

    /**
     * 用户头像
     */
    private String userImg;

    /**
     * 微信openId(打款时需要)
     */
    private String openId;

    /**
     * 微信unionid
     */
    private String unionId;

    /**
     * 上级id
     */
    private String parentId;

    /**
     * 邀请码（随机生成 大写字母+数字 4位 ）
     */
    private String inviteCode;

    /**
     * 0-APP,1-微信公众号,2-小程序
     */
    private Integer channel;

    /**
     * 最后一次登录时间
     */
    private Date lastLogin;

    /**
     * 创建日期
     */
    private Date createTime;
}
