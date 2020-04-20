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
 * 用户浏览分享页类
 * @author MrBird
 */
@Data
@TableName("s_user_browser")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserBrowser implements Serializable {

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
     * 分享id
     */
    private String shareId;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 微信unionid
     */
    private String unionId;

    /**
     * 0-APP,1-微信公众号,2-小程序
     */
    private Integer channel;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 更新日期
     */
    private Date updateTime;
}
