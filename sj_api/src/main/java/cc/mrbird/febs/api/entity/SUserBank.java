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
    private String userId;//用户id
    private Integer bankId;//银行id
    private String realName;//真实姓名
    private String idCard;//身份证号码
    private String cardNum;//银行卡号',
    private String status;// '0 可用 1 删除',
}
