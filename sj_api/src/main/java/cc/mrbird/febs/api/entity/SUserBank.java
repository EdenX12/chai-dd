package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户类
 * @author MrBird
 */
@Data
@TableName("s_user_bank")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserBank implements Serializable {

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
     * 银行id
     */
    private Integer bankId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 银行卡号
     */
    private String cardNum;

    /**
     * '0 可用 1 删除'
     */
    private Integer status;
}
